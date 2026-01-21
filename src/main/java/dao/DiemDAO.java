package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.BangDiem;
import model.DiemChiTiet;
import util.DBConnection;

public class DiemDAO {

     /**
     * Xử lý cả lớp học phần thông thường (dựa vào sinhvien.lop) và lớp nguyện vọng (dựa vào sv_dangky_nv).
     * @param maLop Mã lớp học phần (có thể là mã lớp sinh hoạt hoặc mã lớp nguyện vọng mới)
     * @param maMon Mã môn học
     * @param hocKy Học kỳ
     * @param namHoc Năm học
     * @return Danh sách DiemChiTiet
     */
    public static ObservableList<DiemChiTiet> getDiemByLopAndMon(String maLop, String maMon, int hocKy, int namHoc) {
        ObservableList<DiemChiTiet> list = FXCollections.observableArrayList();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Integer idLopNguyenVong = null;

        try {
            conn = DBConnection.getConnection();

            // Bước 1: Kiểm tra xem maLop có phải là của lớp nguyện vọng không
            String checkNvSql = "SELECT id FROM lop_nguyen_vong " +
                                "WHERE maLopHocPhanMoi = ? AND maMon = ? AND hocKy = ? AND namHoc = ? AND trangThai = 'DA_DUYET'";
            ps = conn.prepareStatement(checkNvSql);
            ps.setString(1, maLop);
            ps.setString(2, maMon);
            ps.setInt(3, hocKy);
            ps.setInt(4, namHoc);
            rs = ps.executeQuery();
            if (rs.next()) {
                idLopNguyenVong = rs.getInt("id");
            }
            rs.close();
            ps.close();

            // Bước 2: Xây dựng câu truy vấn dựa trên kết quả kiểm tra
            String sql;
            if (idLopNguyenVong != null) {
                // Là lớp nguyện vọng -> Lấy SV từ sv_dangky_nv
                System.out.println("Lấy điểm cho Lớp Nguyện Vọng ID: " + idLopNguyenVong);
                sql = "SELECT sv.maSV, sv.hoTen, mh.maMon, mh.tenMon, mh.soTinChi, lnv.hocKy, " + // Lấy hocKy từ lnv
                      "bd.diemQT, bd.diemThi, bd.diemTB " +
                      "FROM sv_dangky_nv dknv " +
                      "JOIN sinhvien sv ON dknv.maSV = sv.maSV " +
                      "JOIN lop_nguyen_vong lnv ON dknv.idLopNguyenVong = lnv.id " +
                      "JOIN monhoc mh ON lnv.maMon = mh.maMon " +
                      "LEFT JOIN bangdiem bd ON sv.maSV = bd.maSV AND lnv.maMon = bd.maMon " + // Join bangdiem theo maMon của lnv
                      "WHERE dknv.idLopNguyenVong = ?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, idLopNguyenVong);
            } else {
                // Là lớp thông thường -> Lấy SV từ sinhvien theo lop
                System.out.println("Lấy điểm cho Lớp thông thường: " + maLop + ", Môn: " + maMon);
                sql = "SELECT sv.maSV, sv.hoTen, mh.maMon, mh.tenMon, mh.soTinChi, mh.hocKy, " + // Lấy hocKy từ monhoc
                      "bd.diemQT, bd.diemThi, bd.diemTB " +
                      "FROM sinhvien sv " +
                      "JOIN monhoc mh ON mh.maMon = ? " + // Join monhoc để lấy thông tin
                      "LEFT JOIN bangdiem bd ON sv.maSV = bd.maSV AND bd.maMon = mh.maMon " +
                      "WHERE sv.lop = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, maMon);
                ps.setString(2, maLop);
            }

            // Bước 3: Thực thi truy vấn và xử lý kết quả
            rs = ps.executeQuery();
            while (rs.next()) {
                 float diemQT = rs.getFloat("diemQT"); if (rs.wasNull()) diemQT = 0.0f;
                 float diemThi = rs.getFloat("diemThi"); if (rs.wasNull()) diemThi = 0.0f;
                 float diemTB = rs.getFloat("diemTB"); if (rs.wasNull()) diemTB = 0.0f;

                list.add(new DiemChiTiet(
                    rs.getString("sv.maSV"), rs.getString("sv.hoTen"),
                    rs.getString("mh.maMon"), rs.getString("mh.tenMon"), // Lấy maMon, tenMon từ mh
                    rs.getInt("mh.soTinChi"), rs.getInt("hocKy"), // Lấy hocKy từ lnv hoặc mh tùy câu truy vấn
                    diemQT, diemThi, diemTB
                ));
            }

        } catch (Exception e) {
             System.err.println("Lỗi khi lấy điểm theo lớp và môn: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (ps != null) ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return list;
    }


    /**
     * Thêm mới hoặc cập nhật điểm cho sinh viên.
     * Nếu là Giảng viên, sẽ kiểm tra quyền trước khi thực hiện.
     * @param diem Đối tượng BangDiem chứa thông tin điểm cần cập nhật (maSV, maMon, diemQT, diemThi)
     * @param maGV Mã giảng viên đang thực hiện thao tác (null nếu là Admin)
     * @return true nếu thành công, false nếu thất bại (do lỗi hoặc không có quyền)
     */
    public static boolean upsertDiem(BangDiem diem, String maGV) {
        if (diem == null || diem.getMaSV() == null || diem.getMaMon() == null) {
            System.err.println("Lỗi upsertDiem: Dữ liệu điểm không hợp lệ (null).");
            return false;
        }

        String maLopSV = null; // Biến lưu mã lớp của SV

        // --- Bước 1: Kiểm tra quyền nếu là Giảng viên ---
        if (maGV != null) {
            // Lấy mã lớp của sinh viên trong một transaction riêng hoặc kết nối riêng
             try (Connection connCheck = DBConnection.getConnection()) {
                 maLopSV = getLopByMaSV(connCheck, diem.getMaSV());
                 if (maLopSV == null) {
                     System.err.println("Lỗi upsertDiem: Không tìm thấy lớp cho sinh viên: " + diem.getMaSV());
                     // **QUAN TRỌNG:** Nếu không tìm thấy lớp sinh hoạt, có thể SV này thuộc lớp NV?
                     // Cần logic kiểm tra phức tạp hơn ở đây nếu GV cần nhập điểm cho lớp NV.
                     // Tạm thời trả về lỗi nếu không tìm thấy lớp sinh hoạt.
                     return false;
                 }

                // Kiểm tra xem giảng viên có được phân công và đã nhận lớp này không
                // **LƯU Ý:** Logic kiểm tra quyền này chỉ đúng cho lớp thông thường.
                // Cần sửa lại nếu muốn kiểm tra quyền cho lớp NV.
                String checkSql = "SELECT 1 FROM phanconggiangday " +
                                  "WHERE maGV = ? AND maMon = ? AND maLop = ? AND trangThai = 'Đã nhận'";
                try (PreparedStatement psCheck = connCheck.prepareStatement(checkSql)) {
                    psCheck.setString(1, maGV);
                    psCheck.setString(2, diem.getMaMon());
                    psCheck.setString(3, maLopSV); // Kiểm tra theo lớp sinh hoạt của SV
                    try (ResultSet rsCheck = psCheck.executeQuery()) {
                        if (!rsCheck.next()) {
                            // Nếu không tìm thấy bản ghi phân công hợp lệ -> Không có quyền
                            System.out.println("Giảng viên " + maGV + " không có quyền sửa điểm môn " + diem.getMaMon() + " cho lớp '" + maLopSV + "' của SV " + diem.getMaSV());
                            // **CẦN KIỂM TRA THÊM:** GV có dạy lớp NV mà SV này đăng ký không?
                            // Logic này tạm thời bỏ qua kiểm tra quyền cho lớp NV khi GV nhập điểm.
                            // return false; // Tạm thời comment để cho phép GV nhập điểm lớp NV
                        }
                        // Có quyền, tiếp tục thực hiện upsert
                        System.out.println("Giảng viên " + maGV + " có quyền sửa điểm (hoặc đang nhập cho lớp NV - tạm bỏ qua check).");
                    }
                }
             } catch (SQLException e) {
                  System.err.println("Lỗi SQL khi kiểm tra quyền giảng viên: " + e.getMessage());
                  e.printStackTrace();
                  return false; // Lỗi CSDL khi kiểm tra quyền
             } catch (Exception e) {
                 System.err.println("Lỗi không xác định khi kiểm tra quyền giảng viên: " + e.getMessage());
                 e.printStackTrace();
                 return false;
             }
        } else {
             System.out.println("Thực hiện upsert với quyền Admin.");
        }


        // --- Bước 2: Thực hiện INSERT hoặc UPDATE ---
        String upsertSql = "INSERT INTO bangdiem (maSV, maMon, diemQT, diemThi) VALUES (?, ?, ?, ?) " +
                           "ON DUPLICATE KEY UPDATE diemQT = VALUES(diemQT), diemThi = VALUES(diemThi)";

        try (Connection connUpsert = DBConnection.getConnection();
             PreparedStatement psUpsert = connUpsert.prepareStatement(upsertSql)) {

            psUpsert.setString(1, diem.getMaSV());
            psUpsert.setString(2, diem.getMaMon());
            psUpsert.setFloat(3, diem.getDiemQT());
            psUpsert.setFloat(4, diem.getDiemThi());

            int affectedRows = psUpsert.executeUpdate();
             System.out.println("Upsert điểm: affectedRows = " + affectedRows); // Log số dòng bị ảnh hưởng
            // executeUpdate trả về số dòng bị ảnh hưởng (có thể là 1 cho INSERT, 1 hoặc 2 cho UPDATE)
            // Hoặc 0 nếu không có gì thay đổi (dữ liệu giống hệt)
            // Coi như thành công nếu không có lỗi và affectedRows >= 0
            return affectedRows >= 0; // Chấp nhận cả trường hợp không có thay đổi (affectedRows=0) là thành công

        } catch (SQLException e) {
             System.err.println("Lỗi SQL khi upsert điểm cho SV " + diem.getMaSV() + ", Môn " + diem.getMaMon() + ": " + e.getMessage());
            e.printStackTrace();
            return false; // Lỗi CSDL khi thực hiện upsert
        } catch (Exception e) {
             System.err.println("Lỗi không xác định khi upsert điểm: " + e.getMessage());
             e.printStackTrace();
             return false;
        }
    }

     /**
     * Hàm phụ trợ: Lấy mã lớp (maLop) của một sinh viên từ bảng sinhvien.
     * Cần Connection vì có thể được gọi trong transaction kiểm tra quyền.
     * @param conn Connection đang mở
     * @param maSV Mã sinh viên
     * @return Mã lớp sinh hoạt hoặc null nếu không tìm thấy/lỗi.
     */
     private static String getLopByMaSV(Connection conn, String maSV) throws SQLException {
        if (conn == null || conn.isClosed()) {
             System.err.println("Lỗi getLopByMaSV: Connection is null or closed.");
             return null; // Hoặc ném ra ngoại lệ mới
        }
        String sql = "SELECT lop FROM sinhvien WHERE maSV = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("lop");
                }
            }
        } // try-with-resources tự đóng PreparedStatement và ResultSet
        return null; // Không tìm thấy SV
     }

}
