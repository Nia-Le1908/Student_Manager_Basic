package dao;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.LopHocPhan;
import util.DBConnection;

public class LopHocPhanDAO {

    /**
     * Lấy danh sách lớp học phần.
     */
    public static ObservableList<LopHocPhan> getLopHocPhan(String maGV) {
        ObservableList<LopHocPhan> list = FXCollections.observableArrayList();
        StringBuilder sql = new StringBuilder(
            "SELECT lhp.*, gv.hoTen AS tenGV, mh.tenMon, kh.hocKy, kh.namHoc, " +
            "(SELECT COUNT(*) FROM ket_qua_hoc_tap kq WHERE kq.idLopHocPhan = lhp.id) AS siSoThucTe " + 
            "FROM lop_hoc_phan lhp " +
            "LEFT JOIN giangvien gv ON lhp.maGV = gv.maGV " + // Sửa thành LEFT JOIN để lấy cả lớp chưa có GV
            "JOIN monhoc mh ON lhp.maMon = mh.maMon " +
            "JOIN ky_hoc kh ON lhp.idKyHoc = kh.id " +
            "WHERE (lhp.trangThai = 'DA_DUYET' OR lhp.trangThai = 'DA_KHOA_DIEM') "
        );

        if (maGV != null) {
            sql.append("AND lhp.maGV = ? ");
        }
        sql.append("ORDER BY kh.namHoc DESC, kh.hocKy DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            if (maGV != null) ps.setString(1, maGV);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Tạo lớp mới 
     */
    public static boolean createClass(LopHocPhan lhp) {
        int idKyHoc = KyHocDAO.getKyHocId(lhp.getHocKy(), lhp.getNamHoc());
        
        if (idKyHoc == -1) {
            System.err.println("Lỗi: Không tìm thấy ID kỳ học.");
            return false;
        }

        String sql = "INSERT INTO lop_hoc_phan (maGV, maMon, idKyHoc, loaiLop, trangThai, lyDoMoLop, tenLopHocPhan, maLopHanhChinh) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // FIX: Xử lý maGV null
            if (lhp.getMaGV() == null || lhp.getMaGV().isEmpty()) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, lhp.getMaGV());
            }
            
            ps.setString(2, lhp.getMaMon());
            ps.setInt(3, idKyHoc);
            ps.setString(4, lhp.getLoaiLop());
            ps.setString(5, lhp.getTrangThai());
            ps.setString(6, lhp.getLyDoMoLop());
            ps.setString(7, lhp.getTenLopHocPhan());
            ps.setString(8, lhp.getMaLopHanhChinh());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
    public static boolean updateAssignment(LopHocPhan lhp) {
        String sql = "UPDATE lop_hoc_phan SET maGV=?, maMon=?, idKyHoc=?, maLopHanhChinh=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, lhp.getMaGV());
            ps.setString(2, lhp.getMaMon());
            ps.setInt(3, lhp.getIdKyHoc());
            ps.setString(4, lhp.getMaLopHanhChinh());
            ps.setInt(5, lhp.getId());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- MỚI: Xóa phân công ---
    public static boolean deleteAssignment(int id) {
        String sql = "DELETE FROM lop_hoc_phan WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("Không thể xóa lớp vì đã có sinh viên đăng ký hoặc có điểm.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean approveClass(int id) {
        String sql = "UPDATE lop_hoc_phan SET trangThai = 'DA_DUYET', ngayDuyet = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
    public static boolean rejectOrDeleteClass(int id) {
        String sql = "DELETE FROM lop_hoc_phan WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
    public static boolean lockGrade(int id) {
        String sql = "UPDATE lop_hoc_phan SET trangThai = 'DA_KHOA_DIEM' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // Cập nhật query lấy danh sách chờ duyệt (LEFT JOIN giangvien vì maGV có thể null)
    public static ObservableList<LopHocPhan> getPendingRequests() {
        ObservableList<LopHocPhan> list = FXCollections.observableArrayList();
        String sql = "SELECT lhp.*, gv.hoTen AS tenGV, mh.tenMon, kh.hocKy, kh.namHoc, 0 as siSoThucTe " +
                     "FROM lop_hoc_phan lhp " +
                     "LEFT JOIN giangvien gv ON lhp.maGV = gv.maGV " + // LEFT JOIN
                     "JOIN monhoc mh ON lhp.maMon = mh.maMon " +
                     "JOIN ky_hoc kh ON lhp.idKyHoc = kh.id " +
                     "WHERE lhp.trangThai = 'CHO_DUYET' " +
                     "ORDER BY lhp.ngayTao DESC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    
    public static ObservableList<LopHocPhan> getAvailableWishlistClasses(String maSV) {
        ObservableList<LopHocPhan> list = FXCollections.observableArrayList();
        String sql = "SELECT lhp.*, gv.hoTen AS tenGV, mh.tenMon, kh.hocKy, kh.namHoc, " +
                     "(SELECT COUNT(*) FROM ket_qua_hoc_tap kq WHERE kq.idLopHocPhan = lhp.id) AS siSoThucTe " +
                     "FROM lop_hoc_phan lhp " +
                     "LEFT JOIN giangvien gv ON lhp.maGV = gv.maGV " + // LEFT JOIN
                     "JOIN monhoc mh ON lhp.maMon = mh.maMon " +
                     "JOIN ky_hoc kh ON lhp.idKyHoc = kh.id " +
                     "WHERE lhp.loaiLop = 'NguyenVong' AND lhp.trangThai = 'DA_DUYET' " +
                     "AND lhp.id NOT IN (SELECT idLopHocPhan FROM ket_qua_hoc_tap WHERE maSV = ?) " +
                     "ORDER BY lhp.ngayDuyet DESC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private static LopHocPhan mapRow(ResultSet rs) throws SQLException {
        // Xử lý null cho tenGV nếu maGV null
        String tenGV = rs.getString("tenGV");
        if (tenGV == null) tenGV = "Chưa phân công";

        return new LopHocPhan(
            rs.getInt("id"),
            rs.getString("maGV"),
            rs.getString("maMon"),
            rs.getInt("idKyHoc"), 
            rs.getInt("hocKy"),   
            rs.getInt("namHoc"),
            rs.getString("maLopHanhChinh"),
            rs.getString("tenLopHocPhan"),
            rs.getString("loaiLop"),
            rs.getString("trangThai"),
            tenGV, 
            rs.getString("tenMon"),
            rs.getString("lyDoMoLop")
            
        );
    }
}