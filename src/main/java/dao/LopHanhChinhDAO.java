package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Khoa;
import model.LopHanhChinh;
import util.DBConnection;
import java.sql.*;

public class LopHanhChinhDAO {

    public static ObservableList<LopHanhChinh> getAll() {
        ObservableList<LopHanhChinh> list = FXCollections.observableArrayList();
        String sql = "SELECT l.*, k.tenKhoa FROM lop_hanh_chinh l " +
                     "LEFT JOIN khoa k ON l.maKhoa = k.maKhoa " +
                     "ORDER BY l.khoaHoc DESC, l.maLop";
        
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Khoa k = new Khoa(rs.getString("maKhoa"), rs.getString("tenKhoa"));
                list.add(new LopHanhChinh(
                    rs.getString("maLop"),
                    rs.getString("tenLop"),
                    k,
                    rs.getInt("khoaHoc")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static boolean insert(LopHanhChinh l) {
        String sql = "INSERT INTO lop_hanh_chinh (maLop, tenLop, maKhoa, khoaHoc) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, l.getMaLop());
            ps.setString(2, l.getTenLop());
            ps.setString(3, l.getKhoa() != null ? l.getKhoa().getMaKhoa() : null);
            ps.setInt(4, l.getKhoaHoc());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public static boolean update(LopHanhChinh l) {
        String sql = "UPDATE lop_hanh_chinh SET tenLop=?, maKhoa=?, khoaHoc=? WHERE maLop=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, l.getTenLop());
            ps.setString(2, l.getKhoa() != null ? l.getKhoa().getMaKhoa() : null);
            ps.setInt(3, l.getKhoaHoc());
            ps.setString(4, l.getMaLop());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public static boolean delete(String maLop) {
        String sql = "DELETE FROM lop_hanh_chinh WHERE maLop=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLop);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}