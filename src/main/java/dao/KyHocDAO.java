package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.KyHoc;
import util.DBConnection;
import java.sql.*;

public class KyHocDAO {

    /**
     * Lấy tất cả các kỳ học (Cho Admin quản lý).
     */
    public static ObservableList<KyHoc> getAllKyHoc() {
        ObservableList<KyHoc> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM ky_hoc ORDER BY namHoc DESC, hocKy DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Lấy ID kỳ học từ (Học kỳ, Năm học). Dùng khi Insert lớp học phần.
     */
    public static int getKyHocId(int hocKy, int namHoc) {
        String sql = "SELECT id FROM ky_hoc WHERE hocKy = ? AND namHoc = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hocKy);
            ps.setInt(2, namHoc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    /**
     * Lấy thông tin đối tượng KyHoc. Dùng để check deadline trong DiemController.
     * (Đây là phương thức bị thiếu gây ra lỗi của bạn)
     */
    public static KyHoc getKyHoc(int hocKy, int namHoc) {
        String sql = "SELECT * FROM ky_hoc WHERE hocKy = ? AND namHoc = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hocKy);
            ps.setInt(2, namHoc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // --- CRUD Methods (Thêm, Sửa, Xóa) ---

    public static boolean insert(KyHoc kh) {
        String sql = "INSERT INTO ky_hoc (hocKy, namHoc, ngayBatDauNhapDiem, ngayKetThucNhapDiem) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, kh.getHocKy());
            ps.setInt(2, kh.getNamHoc());
            ps.setTimestamp(3, kh.getNgayBatDauNhapDiem());
            ps.setTimestamp(4, kh.getNgayKetThucNhapDiem());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public static boolean update(KyHoc kh) {
        String sql = "UPDATE ky_hoc SET ngayBatDauNhapDiem = ?, ngayKetThucNhapDiem = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, kh.getNgayBatDauNhapDiem());
            ps.setTimestamp(2, kh.getNgayKetThucNhapDiem());
            ps.setInt(3, kh.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public static boolean delete(int id) {
        String sql = "DELETE FROM ky_hoc WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // Helper map row
    private static KyHoc mapRow(ResultSet rs) throws SQLException {
        return new KyHoc(
            rs.getInt("id"),
            rs.getInt("hocKy"),
            rs.getInt("namHoc"),
            rs.getTimestamp("ngayBatDauNhapDiem"),
            rs.getTimestamp("ngayKetThucNhapDiem")
        );
    }
}