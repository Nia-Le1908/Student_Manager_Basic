package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.GiangVien;
import model.Khoa;
import util.DBConnection;

public class GiangVienDAO {

    /** Lấy tất cả GV */
    public static ObservableList<GiangVien> getAllGiangVien() {
        ObservableList<GiangVien> list = FXCollections.observableArrayList();
        String sql = "SELECT gv.*, k.tenKhoa FROM giangvien gv LEFT JOIN khoa k ON gv.maKhoa = k.maKhoa ORDER BY gv.maGV";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String maKhoa = rs.getString("maKhoa");
                String tenKhoa = rs.getString("tenKhoa");
                Khoa khoaObj = new Khoa(maKhoa, tenKhoa != null ? tenKhoa : (maKhoa == null ? "Chưa phân khoa" : "N/A"));
                list.add(new GiangVien(rs.getString("maGV"), rs.getString("hoTen"), rs.getString("email"), khoaObj));
            }
        } catch (SQLException e) { System.err.println("SQL Error getting all lecturers: " + e.getMessage()); e.printStackTrace(); }
        catch (Exception e) { System.err.println("General Error getting all lecturers: " + e.getMessage()); e.printStackTrace(); }
        return list;
    }

    /**
     * Lấy thông tin chi tiết của một Giảng viên dựa vào mã GV.
     * @param maGV Mã giảng viên
     * @return Đối tượng GiangVien hoặc null nếu không tìm thấy.
     */
    public static GiangVien getGiangVienByMaGV(String maGV) {
        GiangVien gv = null;
        String sql = "SELECT gv.*, k.tenKhoa FROM giangvien gv LEFT JOIN khoa k ON gv.maKhoa = k.maKhoa WHERE gv.maGV = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maGV);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String maKhoa = rs.getString("maKhoa");
                String tenKhoa = rs.getString("tenKhoa");
                // Đảm bảo tạo Khoa object ngay cả khi maKhoa là null
                Khoa khoaObj = new Khoa(maKhoa, tenKhoa != null ? tenKhoa : (maKhoa == null ? "Chưa phân khoa" : "N/A"));
                gv = new GiangVien(rs.getString("maGV"), rs.getString("hoTen"), rs.getString("email"), khoaObj);
            }
        } catch (SQLException e) { System.err.println("SQL Error getting lecturer by ID '" + maGV + "': " + e.getMessage()); e.printStackTrace(); }
        catch (Exception e) { System.err.println("General Error getting lecturer by ID '" + maGV + "': " + e.getMessage()); e.printStackTrace(); }
        return gv;
    }


    /** Thêm GV */
    public static boolean insert(GiangVien gv) { 
        String sql = "INSERT INTO giangvien (maGV, hoTen, email, maKhoa) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gv.getMaGV()); ps.setString(2, gv.getHoTen()); ps.setString(3, gv.getEmail());
            ps.setString(4, (gv.getKhoa() != null) ? gv.getKhoa().getMaKhoa() : null);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) { System.err.println("Lỗi thêm giảng viên: Mã GV '" + gv.getMaGV() + "' hoặc Email '" + gv.getEmail() + "' đã tồn tại."); return false; }
        catch (SQLException e) { System.err.println("SQL Error inserting lecturer: " + e.getMessage()); e.printStackTrace(); return false; }
        catch (Exception e) { System.err.println("General Error inserting lecturer: " + e.getMessage()); e.printStackTrace(); return false; }
     }
    /** Cập nhật GV */
    public static boolean update(GiangVien gv) { 
        String sql = "UPDATE giangvien SET hoTen = ?, email = ?, maKhoa = ? WHERE maGV = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gv.getHoTen()); ps.setString(2, gv.getEmail()); ps.setString(3, (gv.getKhoa() != null) ? gv.getKhoa().getMaKhoa() : null); ps.setString(4, gv.getMaGV());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) { System.err.println("Lỗi cập nhật giảng viên: Email '" + gv.getEmail() + "' có thể đã được sử dụng."); return false; }
        catch (SQLException e) { System.err.println("SQL Error updating lecturer: " + e.getMessage()); e.printStackTrace(); return false; }
        catch (Exception e) { System.err.println("General Error updating lecturer: " + e.getMessage()); e.printStackTrace(); return false; }
     }
    /** Xóa GV */
    public static boolean delete(String maGV) { 
        String sql = "DELETE FROM giangvien WHERE maGV = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGV);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) { System.err.println("Không thể xóa giảng viên '" + maGV + "' vì đang có dữ liệu liên quan."); return false; }
        catch (SQLException e) { System.err.println("SQL Error deleting lecturer: " + e.getMessage()); e.printStackTrace(); return false; }
        catch (Exception e) { System.err.println("General Error deleting lecturer: " + e.getMessage()); e.printStackTrace(); return false; }
     }
    /** Tìm kiếm GV */
    public static ObservableList<GiangVien> search(String keyword) { 
        ObservableList<GiangVien> list = FXCollections.observableArrayList();
        String sql = "SELECT gv.*, k.tenKhoa FROM giangvien gv LEFT JOIN khoa k ON gv.maKhoa = k.maKhoa " + "WHERE gv.maGV LIKE ? OR gv.hoTen LIKE ? OR gv.email LIKE ? " + "ORDER BY gv.maGV";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchKeyword = "%" + keyword + "%"; ps.setString(1, searchKeyword); ps.setString(2, searchKeyword); ps.setString(3, searchKeyword);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                 String maKhoa = rs.getString("maKhoa"); String tenKhoa = rs.getString("tenKhoa");
                 Khoa khoaObj = new Khoa(maKhoa, tenKhoa != null ? tenKhoa : (maKhoa == null ? "Chưa phân khoa" : "N/A"));
                list.add(new GiangVien(rs.getString("maGV"), rs.getString("hoTen"), rs.getString("email"), khoaObj));
            }
        } catch (SQLException e) { System.err.println("SQL Error searching lecturers: " + e.getMessage()); e.printStackTrace(); }
        catch (Exception e) { System.err.println("General Error searching lecturers: " + e.getMessage()); e.printStackTrace(); }
        return list;
     }
}