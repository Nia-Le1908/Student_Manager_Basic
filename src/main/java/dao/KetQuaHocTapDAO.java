package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.KetQuaHocTap;
import util.DBConnection;
import java.sql.*;

public class KetQuaHocTapDAO {

    /**
     * Lấy danh sách điểm của MỘT LỚP HỌC PHẦN cụ thể.
     */
    public static ObservableList<KetQuaHocTap> getListByLopHocPhan(int idLopHocPhan) {
        ObservableList<KetQuaHocTap> list = FXCollections.observableArrayList();
        
        String sql = "SELECT kq.*, sv.hoTen, mh.tenMon, mh.maMon " +
                     "FROM ket_qua_hoc_tap kq " +
                     "JOIN sinhvien sv ON kq.maSV = sv.maSV " +
                     "JOIN lop_hoc_phan lhp ON kq.idLopHocPhan = lhp.id " +
                     "JOIN monhoc mh ON lhp.maMon = mh.maMon " +
                     "WHERE kq.idLopHocPhan = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLopHocPhan);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new KetQuaHocTap(
                    rs.getInt("id"),
                    rs.getString("maSV"),
                    rs.getString("hoTen"),
                    rs.getString("maMon"),
                    rs.getString("tenMon"),
                    0, 0, 0,
                    (Float) rs.getObject("diemQT"),
                    (Float) rs.getObject("diemThi"),
                    (Float) rs.getObject("diemTB"),
                    rs.getString("trangThai")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Cập nhật điểm.
     */
    public static boolean updateDiem(int idKetQua, float diemQT, float diemThi) {
        String sql = "UPDATE ket_qua_hoc_tap SET diemQT = ?, diemThi = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, diemQT);
            ps.setFloat(2, diemThi);
            ps.setInt(3, idKetQua);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Đăng ký sinh viên vào lớp học phần.
     */
    public static boolean registerStudent(int idLopHocPhan, String maSV) {
        String sql = "INSERT INTO ket_qua_hoc_tap (idLopHocPhan, maSV, trangThai) VALUES (?, ?, 'Dang_Ky')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLopHocPhan);
            ps.setString(2, maSV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Có thể lỗi do trùng lặp (đã có unique key)
            return false;
        }
    }

    /**
     * MỚI: Đếm số lượng sinh viên trong một lớp học phần.
     */
    public static int countStudentsInClass(int idLopHocPhan) {
        String sql = "SELECT COUNT(*) FROM ket_qua_hoc_tap WHERE idLopHocPhan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLopHocPhan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}