package dao;

import model.User;
import model.Role;
import util.DBConnection;
import util.PasswordUtil;
import java.sql.*;

public class UserDAO {

    /**
     * Lấy User theo Username.
     */
    public static User getUserByUsername(String username) {
        String sql = "SELECT username, password, role, maSV, maGV FROM users WHERE username = ?";
        User user = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("maSV"),
                    rs.getString("maGV")
                );
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting user: " + e.getMessage());
            e.printStackTrace();
        }
        return user;
    }

     /**
     * Xác thực User - CÓ DEBUG LOGGING.
     */
    public static User authenticateUser(String username, String password) {
        User user = getUserByUsername(username);

        if (user != null) {
            String hashedPasswordFromDB = user.getPasswordHash(); // Lấy hash từ user object
            System.out.println("Attempting login for user: " + username);
            System.out.println("Hashed password from DB: " + hashedPasswordFromDB);

            boolean passwordMatch = PasswordUtil.checkPassword(password, hashedPasswordFromDB);
            System.out.println("Password match result: " + passwordMatch); // In kết quả kiểm tra

            if (passwordMatch) {
                System.out.println("Authentication successful for user: " + username);
                return user;
            } else {
                System.out.println("Authentication failed for user: " + username);
            }
        } else {
            System.out.println("User not found: " + username);
        }
        return null;
    }


    /**
     * Tạo tài khoản Giảng viên (Admin only).
     */
    public static boolean createLecturerAccount(String username, String hashedPassword, String maGV) {
        if (getUserByUsername(username) != null) {
            System.err.println("Username already exists: " + username);
            return false;
        }

        if (getUserByMaGV(maGV) != null) {
            System.err.println("Lecturer already has account: " + maGV);
            return false;
        }

        if (!lecturerExists(maGV)) {
            System.err.println("Lecturer ID does not exist: " + maGV);
            return false;
        }

        String sql = "INSERT INTO users (username, password, role, maGV) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.setString(3, Role.GIANG_VIEN.getValue());
            ps.setString(4, maGV);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error creating lecturer account: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tạo tài khoản Sinh viên (Admin only).
     */
    public static boolean createStudentAccount(String username, String hashedPassword, String maSV) {
        if (getUserByUsername(username) != null) {
            System.err.println("Username already exists: " + username);
            return false;
        }

        if (getUserByMaSV(maSV) != null) {
            System.err.println("Student already has account: " + maSV);
            return false;
        }

        if (!studentExists(maSV)) {
            System.err.println("Student ID does not exist: " + maSV);
            return false;
        }

        String sql = "INSERT INTO users (username, password, role, maSV) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.setString(3, Role.SINH_VIEN.getValue());
            ps.setString(4, maSV);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error creating student account: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật mật khẩu.
     */
    public static boolean updatePassword(String username, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hashedPassword);
            ps.setString(2, username);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error updating password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy User theo maGV.
     */
    public static User getUserByMaGV(String maGV) {
        String sql = "SELECT username, password, role, maSV, maGV FROM users WHERE maGV = ?";
        User user = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maGV);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("maSV"),
                    rs.getString("maGV")
                );
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting user by maGV: " + e.getMessage());
            e.printStackTrace();
        }
        return user;
    }

    /**
     * Lấy User theo maSV.
     */
    public static User getUserByMaSV(String maSV) {
        String sql = "SELECT username, password, role, maSV, maGV FROM users WHERE maSV = ?";
        User user = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maSV);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("maSV"),
                    rs.getString("maGV")
                );
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting user by maSV: " + e.getMessage());
            e.printStackTrace();
        }
        return user;
    }

    // Helper methods
    private static boolean studentExists(String maSV) {
        String sql = "SELECT 1 FROM sinhvien WHERE maSV = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean lecturerExists(String maGV) {
        String sql = "SELECT 1 FROM giangvien WHERE maGV = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGV);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String registerStudent(String username, String password, String maSV) {
        throw new UnsupportedOperationException("Unimplemented method 'registerStudent'");
    }
}