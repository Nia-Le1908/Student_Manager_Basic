package dao;

import model.Khoa;
import model.MonHoc;
import util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class SinhVienDangKyDAO {

    private static int getCurrentHocKy() {
        int month = LocalDate.now().getMonthValue();
        if (month >= 9 && month <= 12) return 1;
        if (month >= 1 && month <= 5) return 2;
        return 3;
    }

    private static int getCurrentNamHoc() {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        return (month >= 9) ? year : year - 1;
    }

    /**
     * Kiểm tra xem sinh viên có đủ điều kiện đăng ký nguyện vọng cho một môn học cụ thể hay không.
     * Điều kiện: Sinh viên chưa học môn này HOẶC điểm tổng kết cao nhất của môn này < 2.0 (Thang 4 - Dưới điểm C).
     * @param maSV Mã sinh viên
     * @param maMon Mã môn học muốn đăng ký
     * @return true nếu đủ điều kiện, false nếu không (đã học và điểm >= 2.0).
     */
    public static boolean checkConditionForWishlist(String maSV, String maMon) {
        // Lấy điểm tổng kết cao nhất của sinh viên cho môn học này
        String sql = "SELECT MAX(kq.diemTB) as maxDiem " +
                     "FROM ket_qua_hoc_tap kq " +
                     "JOIN lop_hoc_phan lhp ON kq.idLopHocPhan = lhp.id " +
                     "WHERE kq.maSV = ? AND lhp.maMon = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, maSV);
            ps.setString(2, maMon);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Lấy điểm từ kết quả truy vấn
                double diemTBHe10 = rs.getDouble("maxDiem");
                
                // Kiểm tra nếu giá trị là null (chưa học lần nào) -> Đủ điều kiện
                if (rs.wasNull()) {
                    return true; 
                }
                
                // Chuyển đổi sang hệ 4 để so sánh
                double diemHe4 = ThongKeDAO.convertToScale4(diemTBHe10);
                
                // Nếu điểm hệ 4 < 2.0 (Dưới C) -> Đủ điều kiện học lại/cải thiện
                return diemHe4 < 2.0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; 
        }
        
        // Nếu không tìm thấy bản ghi nào (chưa học) -> Đủ điều kiện
        return true;
    }


    public static ObservableList<MonHoc> getRegisteredMonHoc(String maSV, int idKyHoc) {
        ObservableList<MonHoc> list = FXCollections.observableArrayList();
        String sql = "SELECT mh.*, k.tenKhoa FROM ket_qua_hoc_tap kq " +
                     "JOIN lop_hoc_phan lhp ON kq.idLopHocPhan = lhp.id " +
                     "JOIN monhoc mh ON lhp.maMon = mh.maMon " +
                     "LEFT JOIN khoa k ON mh.maKhoa = k.maKhoa " +
                     "WHERE kq.maSV = ? AND lhp.idKyHoc = ? " +
                     "ORDER BY mh.maMon";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setInt(2, idKyHoc);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String maKhoa = rs.getString("maKhoa");
                Khoa khoa = new Khoa(maKhoa, rs.getString("tenKhoa"));
                list.add(new MonHoc(
                    rs.getString("maMon"), rs.getString("tenMon"),
                    rs.getInt("soTinChi"), rs.getString("loaiMon"),
                    rs.getInt("hocKy"), rs.getString("monTienQuyet"), khoa
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    
    public static ObservableList<MonHoc> getAvailableMonHoc(String maSV, int idKyHoc) {
        ObservableList<MonHoc> availableList = FXCollections.observableArrayList();
        Set<String> passedCodes = getPassedMonHocCodes(maSV);
        ObservableList<MonHoc> registered = getRegisteredMonHoc(maSV, idKyHoc);
        Set<String> registeredCodes = new HashSet<>();
        for(MonHoc m : registered) registeredCodes.add(m.getMaMon());

        String sql = "SELECT DISTINCT mh.*, k.tenKhoa " +
                     "FROM lop_hoc_phan lhp " +
                     "JOIN monhoc mh ON lhp.maMon = mh.maMon " +
                     "LEFT JOIN khoa k ON mh.maKhoa = k.maKhoa " +
                     "WHERE lhp.idKyHoc = ? " +
                     "AND (lhp.loaiLop = 'HanhChinh' OR (lhp.loaiLop = 'NguyenVong' AND lhp.trangThai = 'DA_DUYET')) " +
                     "ORDER BY mh.tenMon";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idKyHoc);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String maMon = rs.getString("maMon");
                if (!passedCodes.contains(maMon) && !registeredCodes.contains(maMon)) {
                    String maKhoa = rs.getString("maKhoa");
                    Khoa khoa = new Khoa(maKhoa, rs.getString("tenKhoa"));
                    availableList.add(new MonHoc(
                        maMon, rs.getString("tenMon"),
                        rs.getInt("soTinChi"), rs.getString("loaiMon"),
                        rs.getInt("hocKy"), rs.getString("monTienQuyet"), khoa
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return availableList;
    }

    private static Set<String> getPassedMonHocCodes(String maSV) {
        Set<String> passedCodes = new HashSet<>();
        String sql = "SELECT lhp.maMon FROM ket_qua_hoc_tap kq " +
                     "JOIN lop_hoc_phan lhp ON kq.idLopHocPhan = lhp.id " +
                     "WHERE kq.maSV = ? AND kq.diemTB >= 4.0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) passedCodes.add(rs.getString("maMon"));
        } catch (SQLException e) { e.printStackTrace(); }
        return passedCodes;
    }
    
    public static boolean register(String maSV, String maMon, int idKyHoc) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            String findClassSql = "SELECT id FROM lop_hoc_phan " +
                                  "WHERE maMon = ? AND idKyHoc = ? " + 
                                  "AND (loaiLop = 'HanhChinh' OR (loaiLop = 'NguyenVong' AND trangThai = 'DA_DUYET')) " +
                                  "ORDER BY loaiLop ASC LIMIT 1";
            
            int classId = -1;
            try (PreparedStatement psFind = conn.prepareStatement(findClassSql)) {
                psFind.setString(1, maMon);
                psFind.setInt(2, idKyHoc);
                ResultSet rs = psFind.executeQuery();
                if (rs.next()) classId = rs.getInt("id");
            }
            if (classId == -1) return false;

            String checkExist = "SELECT 1 FROM ket_qua_hoc_tap WHERE maSV = ? AND idLopHocPhan = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkExist)) {
                 psCheck.setString(1, maSV);
                 psCheck.setInt(2, classId);
                 if (psCheck.executeQuery().next()) return false;
            }

            String insertSql = "INSERT INTO ket_qua_hoc_tap (maSV, idLopHocPhan, trangThai) VALUES (?, ?, 'Dang_Hoc')";
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setString(1, maSV);
                psInsert.setInt(2, classId);
                return psInsert.executeUpdate() > 0;
            }
        } catch (Exception e) { e.printStackTrace(); return false; } 
        finally { try { if (conn != null) conn.close(); } catch (SQLException e) {} }
    }
    
    public static boolean unregister(String maSV, String maMon, int idKyHoc) {
        String sql = "DELETE kq FROM ket_qua_hoc_tap kq " +
                     "JOIN lop_hoc_phan lhp ON kq.idLopHocPhan = lhp.id " +
                     "WHERE kq.maSV = ? AND lhp.maMon = ? AND lhp.idKyHoc = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, maMon);
            ps.setInt(3, idKyHoc);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    
    // Giữ lại hàm cũ để tương thích
    public static ObservableList<MonHoc> getRegisteredMonHoc(String maSV) {
        return getRegisteredMonHocForSemester(maSV, getCurrentHocKy(), getCurrentNamHoc());
    }
    public static ObservableList<MonHoc> getRegisteredMonHocForSemester(String maSV, int hocKy, int namHoc) {
        return getRegisteredMonHoc(maSV, -1); 
    }
}