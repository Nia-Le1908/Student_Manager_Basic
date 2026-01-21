package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.DiemChiTiet;
import util.DBConnection; 

public class ThongKeDAO {

    /**
     * Lấy danh sách điểm chi tiết.
     */
    public static List<DiemChiTiet> getDiemChiTiet(String maSV) {
        List<DiemChiTiet> list = new ArrayList<>();
        // Query sửa đổi: JOIN thêm bảng ky_hoc (kh)
        String sql = "SELECT kq.*, lhp.maMon, kh.hocKy, kh.namHoc, mh.tenMon, mh.soTinChi " +
                     "FROM ket_qua_hoc_tap kq " +
                     "JOIN lop_hoc_phan lhp ON kq.idLopHocPhan = lhp.id " +
                     "JOIN ky_hoc kh ON lhp.idKyHoc = kh.id " +  // Thêm dòng này
                     "JOIN monhoc mh ON lhp.maMon = mh.maMon " +
                     "WHERE kq.maSV = ? " +
                     "ORDER BY kh.namHoc DESC, kh.hocKy DESC"; // Sửa lhp -> kh

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                float diemQT = rs.getObject("diemQT") != null ? rs.getFloat("diemQT") : 0;
                float diemThi = rs.getObject("diemThi") != null ? rs.getFloat("diemThi") : 0;
                float diemTB = rs.getObject("diemTB") != null ? rs.getFloat("diemTB") : 0;
                
                list.add(new DiemChiTiet(
                    rs.getString("maSV"), 
                    "", 
                    rs.getString("maMon"),
                    rs.getString("tenMon"),
                    rs.getInt("soTinChi"), 
                    rs.getInt("hocKy"), // Lấy từ kh.hocKy
                    diemQT, 
                    diemThi, 
                    diemTB
                ));
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    /**
     * Lấy năm học.
     * FIX: JOIN ky_hoc.
     */
    public static int getNamHocFromDiem(String maSV, String maMon) {
        String sql = "SELECT kh.namHoc " +
                     "FROM ket_qua_hoc_tap kq " +
                     "JOIN lop_hoc_phan lhp ON kq.idLopHocPhan = lhp.id " +
                     "JOIN ky_hoc kh ON lhp.idKyHoc = kh.id " + // Thêm Join
                     "WHERE kq.maSV = ? AND lhp.maMon = ? " +
                     "ORDER BY kh.namHoc DESC LIMIT 1"; 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, maMon);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("namHoc");
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return -1;
    }

    /**
     * Tính điểm tích lũy (Giữ nguyên vì không dùng hocKy/namHoc).
     */
    public static double getDiemTichLuy(String maSV) {
        double tongDiemHe4 = 0;
        int tongTinChi = 0;
        
        String sql = "SELECT mh.soTinChi, MAX(kq.diemTB) as maxDiem " +
                     "FROM ket_qua_hoc_tap kq " +
                     "JOIN lop_hoc_phan lhp ON kq.idLopHocPhan = lhp.id " +
                     "JOIN monhoc mh ON lhp.maMon = mh.maMon " +
                     "WHERE kq.maSV = ? " +
                     "GROUP BY mh.maMon, mh.soTinChi " + 
                     "HAVING MAX(kq.diemTB) >= 4.0";     

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double diemTBHe10 = rs.getDouble("maxDiem");
                int tinChi = rs.getInt("soTinChi");
                double diemHe4 = convertToScale4(diemTBHe10);
                tongDiemHe4 += diemHe4 * tinChi;
                tongTinChi += tinChi;
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        
        if (tongTinChi == 0) return 0.0;
        return Math.round((tongDiemHe4 / tongTinChi) * 100.0) / 100.0;
    }

    public static double convertToScale4(double diemHe10) {
        if (diemHe10 >= 8.5) return 4.0;
        if (diemHe10 >= 7.0) return 3.0;
        if (diemHe10 >= 5.5) return 2.0;
        if (diemHe10 >= 4.0) return 1.0;
        return 0.0;
    }
    
    public static String xepLoaiHocLuc(double gpaHe4) {
        if (gpaHe4 >= 3.6) return "Xuất sắc";
        if (gpaHe4 >= 3.2) return "Giỏi";
        if (gpaHe4 >= 2.5) return "Khá";
        if (gpaHe4 >= 2.0) return "Trung bình";
        return "Yếu";
    }
}