package dao;

import model.Khoa;
import model.SinhVien;
import util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SinhVienDAO {

    /**
     * Lấy toàn bộ danh sách sinh viên (Dành cho Admin).
     */
    public static ObservableList<SinhVien> getAllSinhVien() {
        return getSinhVienByCriteria(null);
    }

    /**
     * Logic: Join từ lop_hoc_phan -> ket_qua_hoc_tap -> sinhvien
     */
    public static ObservableList<SinhVien> getStudentsTaughtBy(String maGV, String keyword) {
        ObservableList<SinhVien> list = FXCollections.observableArrayList();
        
        String sql = "SELECT DISTINCT sv.*, k.tenKhoa " +
                     "FROM sinhvien sv " +
                     "JOIN ket_qua_hoc_tap kq ON sv.maSV = kq.maSV " +
                     "JOIN lop_hoc_phan lhp ON kq.idLopHocPhan = lhp.id " +
                     "LEFT JOIN khoa k ON sv.maKhoa = k.maKhoa " +
                     "WHERE lhp.maGV = ? " +
                     "AND (lhp.trangThai = 'DA_DUYET' OR lhp.trangThai = 'DA_KHOA_DIEM') ";

        if (keyword != null && !keyword.isEmpty()) {
            sql += "AND (sv.maSV LIKE ? OR sv.hoTen LIKE ?) ";
        }
        
        sql += "ORDER BY sv.hoTen";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, maGV);
            
            if (keyword != null && !keyword.isEmpty()) {
                String searchKey = "%" + keyword + "%";
                ps.setString(2, searchKey);
                ps.setString(3, searchKey);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Date sqlDate = rs.getDate("ngaySinh");
                LocalDate ngaySinh = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                String maKhoaStr = rs.getString("maKhoa");
                String tenKhoaStr = rs.getString("tenKhoa");
                Khoa khoaObj = new Khoa(maKhoaStr, tenKhoaStr != null ? tenKhoaStr : "N/A");

                SinhVien sv = new SinhVien(
                        rs.getString("maSV"),
                        rs.getString("hoTen"),
                        ngaySinh,
                        rs.getString("gioiTinh"),
                        rs.getString("queQuan"),
                        khoaObj,
                        rs.getString("maLop")
                );
                list.add(sv);
            }
        } catch (Exception e) { e.printStackTrace(); }
        
        // Tính điểm tích lũy cho danh sách này (Optional - có thể bỏ qua để tăng tốc độ nếu list dài)
        for (SinhVien sv : list) {
            sv.setDiemTichLuy(ThongKeDAO.getDiemTichLuy(sv.getMaSV()));
        }
        
        return list;
    }

    /**
     * Hàm chung để lấy danh sách sinh viên (Dùng cho Admin).
     */
    public static ObservableList<SinhVien> getSinhVienByCriteria(String condition, Object... params) {
        ObservableList<SinhVien> list = FXCollections.observableArrayList();
        String baseSql = "SELECT sv.*, k.tenKhoa FROM sinhvien sv LEFT JOIN khoa k ON sv.maKhoa = k.maKhoa";
        String sql = (condition != null) ? baseSql + " WHERE " + condition : baseSql;
         sql += " ORDER BY sv.maLop, sv.maSV"; 

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Date sqlDate = rs.getDate("ngaySinh");
                LocalDate ngaySinh = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                String maKhoaStr = rs.getString("maKhoa");
                String tenKhoaStr = rs.getString("tenKhoa");
                Khoa khoaObj = new Khoa(maKhoaStr, tenKhoaStr != null ? tenKhoaStr : "N/A");

                list.add(new SinhVien(
                        rs.getString("maSV"),
                        rs.getString("hoTen"),
                        ngaySinh,
                        rs.getString("gioiTinh"),
                        rs.getString("queQuan"),
                        khoaObj,
                        rs.getString("maLop") 
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }

        for (SinhVien sv : list) {
            sv.setDiemTichLuy(ThongKeDAO.getDiemTichLuy(sv.getMaSV()));
        }
        return list;
    }

    /** Thêm sinh viên mới (Admin). */
    public static boolean insert(SinhVien sv) {
        String sql = "INSERT INTO sinhvien (maSV, hoTen, ngaySinh, gioiTinh, queQuan, maKhoa, maLop) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sv.getMaSV());
            ps.setString(2, sv.getHoTen());
            if (sv.getNgaySinh() != null) ps.setDate(3, java.sql.Date.valueOf(sv.getNgaySinh())); else ps.setNull(3, Types.DATE);
            ps.setString(4, sv.getGioiTinh());
            ps.setString(5, sv.getQueQuan());
            ps.setString(6, (sv.getKhoa() != null) ? sv.getKhoa().getMaKhoa() : null);
            ps.setString(7, sv.getLop()); 
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Cập nhật sinh viên. */
    public static boolean update(SinhVien sv) {
        String sql = "UPDATE sinhvien SET hoTen=?, ngaySinh=?, gioiTinh=?, queQuan=?, maKhoa=?, maLop=? WHERE maSV=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sv.getHoTen());
            if (sv.getNgaySinh() != null) ps.setDate(2, java.sql.Date.valueOf(sv.getNgaySinh())); else ps.setNull(2, Types.DATE);
            ps.setString(3, sv.getGioiTinh());
            ps.setString(4, sv.getQueQuan());
            ps.setString(5, (sv.getKhoa() != null) ? sv.getKhoa().getMaKhoa() : null);
            ps.setString(6, sv.getLop());
            ps.setString(7, sv.getMaSV());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public static boolean delete(String maSV) {
        String sql = "DELETE FROM sinhvien WHERE maSV=?";
         try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Tìm kiếm sinh viên (Dành cho Admin - tìm toàn bộ). */
    public static ObservableList<SinhVien> search(String keyword, List<String> allowedLops) {
        // Logic cũ này chủ yếu dành cho Admin hoặc tìm theo lớp hành chính
        // Với Giảng viên tín chỉ, ta dùng getStudentsTaughtBy
        return getSinhVienByCriteria("(sv.maSV LIKE ? OR sv.hoTen LIKE ?)", "%" + keyword + "%", "%" + keyword + "%");
    }

    /** Lấy danh sách lớp hành chính */
    public static List<String> getAllLop() {
         List<String> list = new ArrayList<>();
        String sql = "SELECT maLop FROM lop_hanh_chinh ORDER BY maLop";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("maLop"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}