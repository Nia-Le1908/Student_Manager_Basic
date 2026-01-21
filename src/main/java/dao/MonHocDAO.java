package dao;

import model.Khoa; // Import Khoa
import model.MonHoc;
import util.DBConnection;
import java.sql.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MonHocDAO {

    /** Lấy tất cả môn học */
    public static ObservableList<MonHoc> getAllMonHoc() {
        ObservableList<MonHoc> list = FXCollections.observableArrayList();
        String sql = "SELECT mh.*, k.tenKhoa FROM monhoc mh LEFT JOIN khoa k ON mh.maKhoa = k.maKhoa ORDER BY mh.hocKy, mh.tenMon";
        try (Connection conn = DBConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                 String maKhoa = rs.getString("maKhoa"); String tenKhoa = rs.getString("tenKhoa");
                 Khoa khoaObj = (maKhoa == null) ? null : new Khoa(maKhoa, tenKhoa != null ? tenKhoa : "N/A");
                list.add(new MonHoc(rs.getString("maMon"), rs.getString("tenMon"), rs.getInt("soTinChi"), rs.getString("loaiMon"), rs.getInt("hocKy"), rs.getString("monTienQuyet"), khoaObj));
            }
        } catch (Exception e) { e.printStackTrace(); } return list;
    }

    /** Lấy môn học theo Khoa */
    public static ObservableList<MonHoc> getMonHocByKhoa(String maKhoa) {
        ObservableList<MonHoc> list = FXCollections.observableArrayList(); if (maKhoa == null || maKhoa.isEmpty()) return list;
        String sql = "SELECT mh.*, k.tenKhoa FROM monhoc mh LEFT JOIN khoa k ON mh.maKhoa = k.maKhoa WHERE mh.maKhoa = ? ORDER BY mh.hocKy, mh.tenMon";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhoa); ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                 String tenKhoa = rs.getString("tenKhoa"); Khoa khoaObj = new Khoa(maKhoa, tenKhoa != null ? tenKhoa : "N/A");
                list.add(new MonHoc(rs.getString("maMon"), rs.getString("tenMon"), rs.getInt("soTinChi"), rs.getString("loaiMon"), rs.getInt("hocKy"), rs.getString("monTienQuyet"), khoaObj));
            }
        } catch (Exception e) { e.printStackTrace(); } return list;
     }

    /**
     * Lấy danh sách các môn học thuộc Khoa mà Giảng viên CHƯA đăng ký dạy.
     * @param maGV Mã giảng viên
     * @param maKhoa Mã khoa của giảng viên
     * @return ObservableList<MonHoc>
     */
    public static ObservableList<MonHoc> getMonHocByKhoaNotInDangKy(String maGV, String maKhoa) { // Đảm bảo phương thức này tồn tại và đúng tên
        ObservableList<MonHoc> list = FXCollections.observableArrayList();
        if (maKhoa == null || maKhoa.isEmpty() || maGV == null || maGV.isEmpty()) {
             System.err.println("getMonHocByKhoaNotInDangKy: Invalid maKhoa or maGV.");
            return list;
        }

        String sql = "SELECT mh.*, k.tenKhoa " +
                     "FROM monhoc mh " +
                     "LEFT JOIN khoa k ON mh.maKhoa = k.maKhoa " +
                     "WHERE mh.maKhoa = ? " +
                     "AND mh.maMon NOT IN (SELECT dk.maMon FROM giangvien_dangky_monhoc dk WHERE dk.maGV = ?) " +
                     "ORDER BY mh.hocKy, mh.tenMon";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhoa);
            ps.setString(2, maGV);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                 String tenKhoa = rs.getString("tenKhoa");
                 Khoa khoaObj = new Khoa(maKhoa, tenKhoa != null ? tenKhoa : "N/A");

                list.add(new MonHoc(
                        rs.getString("maMon"),
                        rs.getString("tenMon"),
                        rs.getInt("soTinChi"),
                        rs.getString("loaiMon"),
                        rs.getInt("hocKy"),
                        rs.getString("monTienQuyet"),
                        khoaObj
                ));
            }
        } catch (SQLException e) {
             System.err.println("SQL Error getting available courses for GV " + maGV + " in Khoa " + maKhoa + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
             System.err.println("General Error getting available courses: " + e.getMessage());
             e.printStackTrace();
        }
        return list;
    }


    /** Thêm môn học */
    public static boolean insert(MonHoc m) {
        String sql = "INSERT INTO monhoc (maMon, tenMon, soTinChi, loaiMon, hocKy, monTienQuyet, maKhoa) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getMaMon());
            ps.setString(2, m.getTenMon());
            ps.setInt(3, m.getSoTinChi());
            ps.setString(4, m.getLoaiMon());
            ps.setInt(5, m.getHocKy());

            // *** FIX: Check if monTienQuyet is empty and set SQL NULL ***
            String monTQ = m.getMonTienQuyet();
            if (monTQ == null || monTQ.trim().isEmpty()) {
                ps.setNull(6, Types.VARCHAR); // Set SQL NULL if empty
            } else {
                ps.setString(6, monTQ.trim().toUpperCase()); // Otherwise set the value
            }

            ps.setString(7, (m.getKhoa() != null) ? m.getKhoa().getMaKhoa() : null);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
             // Specific error for duplicate keys or foreign key issues
             if (e.getMessage().contains("Duplicate entry")) {
                System.err.println("Lỗi thêm môn học: Mã môn học '" + m.getMaMon() + "' đã tồn tại.");
             } else if (e.getMessage().contains("foreign key constraint fails")) {
                 // Check which constraint failed if possible
                 if (e.getMessage().contains("monhoc_ibfk_1")) { // Check constraint name for monTienQuyet
                     System.err.println("Lỗi thêm môn học: Môn tiên quyết '" + m.getMonTienQuyet() + "' không tồn tại.");
                 } else if (e.getMessage().contains("monhoc_ibfk_2")) { // Assuming constraint name for maKhoa is monhoc_ibfk_2
                     System.err.println("Lỗi thêm môn học: Mã khoa '" + (m.getKhoa() != null ? m.getKhoa().getMaKhoa() : "NULL") + "' không hợp lệ.");
                 } else {
                    System.err.println("Lỗi ràng buộc khóa ngoại khi thêm môn học: " + e.getMessage());
                 }
             } else {
                 System.err.println("Lỗi ràng buộc CSDL khi thêm môn học: " + e.getMessage());
             }
             // e.printStackTrace(); // Keep stack trace for debugging if needed
             return false;
        }
        catch (SQLException e) { // Catch broader SQL errors
             System.err.println("Lỗi SQL chung khi thêm môn học: " + e.getMessage());
             e.printStackTrace();
             return false;
        }
        catch (Exception e) { // Catch any other unexpected errors
             System.err.println("Lỗi không xác định khi thêm môn học: " + e.getMessage());
             e.printStackTrace();
             return false;
        }
     }
    /** Cập nhật môn học */
    public static boolean update(MonHoc m) {
        String sql = "UPDATE monhoc SET tenMon=?, soTinChi=?, loaiMon=?, hocKy=?, monTienQuyet=?, maKhoa=? WHERE maMon=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getTenMon());
            ps.setInt(2, m.getSoTinChi());
            ps.setString(3, m.getLoaiMon());
            ps.setInt(4, m.getHocKy());

             // *** FIX: Check if monTienQuyet is empty and set SQL NULL ***
            String monTQ = m.getMonTienQuyet();
            if (monTQ == null || monTQ.trim().isEmpty()) {
                ps.setNull(5, Types.VARCHAR); // Set SQL NULL if empty
            } else {
                ps.setString(5, monTQ.trim().toUpperCase()); // Otherwise set the value
            }

            ps.setString(6, (m.getKhoa() != null) ? m.getKhoa().getMaKhoa() : null);
            ps.setString(7, m.getMaMon());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
             // Similar improved error handling as insert
             if (e.getMessage().contains("foreign key constraint fails")) {
                  if (e.getMessage().contains("monhoc_ibfk_1")) {
                     System.err.println("Lỗi cập nhật môn học: Môn tiên quyết '" + m.getMonTienQuyet() + "' không tồn tại.");
                 } else if (e.getMessage().contains("monhoc_ibfk_2")) {
                     System.err.println("Lỗi cập nhật môn học: Mã khoa '" + (m.getKhoa() != null ? m.getKhoa().getMaKhoa() : "NULL") + "' không hợp lệ.");
                 } else {
                    System.err.println("Lỗi ràng buộc khóa ngoại khi cập nhật môn học: " + e.getMessage());
                 }
             } else {
                 System.err.println("Lỗi ràng buộc CSDL khi cập nhật môn học: " + e.getMessage());
             }
             return false;
        }
        catch (SQLException e) {
             System.err.println("Lỗi SQL khi cập nhật môn học: " + e.getMessage());
             e.printStackTrace(); return false;
        }
        catch (Exception e) {
             System.err.println("Lỗi không xác định khi cập nhật môn học: " + e.getMessage());
            e.printStackTrace(); return false;
        }
    }
    /** Xóa môn học */
    public static boolean delete(String maMon) {
        String sql = "DELETE FROM monhoc WHERE maMon=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maMon); return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) { System.err.println("Không thể xóa môn học '" + maMon + "' vì đang có dữ liệu liên quan (điểm số, đăng ký,...)."); return false; }
        catch (Exception e) { e.printStackTrace(); return false; }
     }
    /** Tìm kiếm môn học */
    public static ObservableList<MonHoc> search(String keyword) {
        ObservableList<MonHoc> list = FXCollections.observableArrayList();
        String sql = "SELECT mh.*, k.tenKhoa FROM monhoc mh LEFT JOIN khoa k ON mh.maKhoa = k.maKhoa " + "WHERE mh.maMon LIKE ? OR mh.tenMon LIKE ? ORDER BY mh.hocKy, mh.tenMon";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchKeyword = "%" + keyword + "%"; ps.setString(1, searchKeyword); ps.setString(2, searchKeyword);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                 String maKhoa = rs.getString("maKhoa"); String tenKhoa = rs.getString("tenKhoa");
                 Khoa khoaObj = (maKhoa == null) ? null : new Khoa(maKhoa, tenKhoa != null ? tenKhoa : "N/A");
                 list.add(new MonHoc(rs.getString("maMon"), rs.getString("tenMon"), rs.getInt("soTinChi"), rs.getString("loaiMon"), rs.getInt("hocKy"), rs.getString("monTienQuyet"), khoaObj));
            }
        } catch (Exception e) { e.printStackTrace(); } return list;
     }
     public static ObservableList<MonHoc> getMonHocByKhoaOfGiangVien(String maGV) {
        ObservableList<MonHoc> list = FXCollections.observableArrayList();
        String sql = "SELECT mh.*, k.tenKhoa FROM monhoc mh " +
                     "JOIN khoa k ON mh.maKhoa = k.maKhoa " +
                     "JOIN giangvien gv ON gv.maKhoa = k.maKhoa " + 
                     "WHERE gv.maGV = ? " +
                     "ORDER BY mh.tenMon";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGV);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                 String maKhoa = rs.getString("maKhoa");
                 String tenKhoa = rs.getString("tenKhoa");
                 Khoa khoaObj = new Khoa(maKhoa, tenKhoa != null ? tenKhoa : "N/A");
                 list.add(new MonHoc(rs.getString("maMon"), rs.getString("tenMon"), rs.getInt("soTinChi"), rs.getString("loaiMon"), rs.getInt("hocKy"), rs.getString("monTienQuyet"), khoaObj));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
}}