package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.SQLIntegrityConstraintViolationException; // Import thêm

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Khoa;
import util.DBConnection;

public class KhoaDAO {

    /**
     * Lấy danh sách tất cả các Khoa.
     * @return ObservableList<Khoa>
     */
    public static ObservableList<Khoa> getAllKhoa() {
        ObservableList<Khoa> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM khoa ORDER BY maKhoa"; // Sắp xếp theo mã khoa
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Khoa(
                        rs.getString("maKhoa"),
                        rs.getString("tenKhoa")
                ));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting all Khoa: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("General Error getting all Khoa: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy thông tin một Khoa dựa trên Mã Khoa.
     * @param maKhoa Mã khoa cần tìm.
     * @return Đối tượng Khoa hoặc null nếu không tìm thấy.
     */
    public static Khoa getKhoaByMaKhoa(String maKhoa) { // Thêm phương thức này
        Khoa khoa = null;
        if (maKhoa == null || maKhoa.isEmpty()) {
            return null;
        }
        String sql = "SELECT * FROM khoa WHERE maKhoa = ?";
        // Sử dụng try-with-resources cho PreparedStatement và ResultSet
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhoa);
            try (ResultSet rs = ps.executeQuery()) { // Try-with-resources cho ResultSet
                if (rs.next()) {
                    khoa = new Khoa(
                            rs.getString("maKhoa"),
                            rs.getString("tenKhoa")
                    );
                }
            } // ResultSet tự đóng
        } catch (SQLException e) {
             System.err.println("SQL Error getting Khoa by maKhoa '" + maKhoa + "': " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
             System.err.println("General Error getting Khoa by maKhoa '" + maKhoa + "': " + e.getMessage());
             e.printStackTrace();
        }
        return khoa;
    }


    /**
     * Thêm Khoa mới.
     * @param khoa Đối tượng Khoa cần thêm.
     * @return true nếu thành công, false nếu thất bại.
     */
    public static boolean insert(Khoa khoa) {
        String sql = "INSERT INTO khoa (maKhoa, tenKhoa) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, khoa.getMaKhoa());
            ps.setString(2, khoa.getTenKhoa());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
             System.err.println("Lỗi thêm Khoa: Mã khoa '" + khoa.getMaKhoa() + "' hoặc Tên khoa '" + khoa.getTenKhoa() + "' đã tồn tại.");
             return false;
         } catch (SQLException e) {
            System.err.println("SQL Error inserting Khoa: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
             System.err.println("General Error inserting Khoa: " + e.getMessage());
             e.printStackTrace();
             return false;
        }
    }

    /**
     * Cập nhật thông tin Khoa.
     * @param khoa Đối tượng Khoa cần cập nhật.
     * @return true nếu thành công, false nếu thất bại.
     */
    public static boolean update(Khoa khoa) {
        String sql = "UPDATE khoa SET tenKhoa = ? WHERE maKhoa = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, khoa.getTenKhoa());
            ps.setString(2, khoa.getMaKhoa());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
             System.err.println("Lỗi cập nhật Khoa: Tên khoa '" + khoa.getTenKhoa() + "' có thể đã tồn tại.");
             return false;
         } catch (SQLException e) {
            System.err.println("SQL Error updating Khoa: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
             System.err.println("General Error updating Khoa: " + e.getMessage());
             e.printStackTrace();
             return false;
        }
    }

    /**
     * Xóa Khoa.
     * @param maKhoa Mã khoa cần xóa.
     * @return true nếu thành công, false nếu thất bại.
     */
    public static boolean delete(String maKhoa) {
        String sql = "DELETE FROM khoa WHERE maKhoa = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhoa);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
             // Lỗi khóa ngoại (ví dụ: Khoa đang được tham chiếu bởi Giảng viên, Sinh viên, Môn học)
             System.err.println("Không thể xóa Khoa '" + maKhoa + "' vì đang có dữ liệu liên quan (GV, SV, Môn học...).");
             return false;
         } catch (SQLException e) {
             System.err.println("SQL Error deleting Khoa: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
             System.err.println("General Error deleting Khoa: " + e.getMessage());
             e.printStackTrace();
             return false;
        }
    }
}

