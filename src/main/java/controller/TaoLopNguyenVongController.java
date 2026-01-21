package controller;

import dao.KetQuaHocTapDAO;
import dao.LopHocPhanDAO;
import dao.MonHocDAO;
import dao.KyHocDAO;
import dao.SinhVienDangKyDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.LopHocPhan;
import model.MonHoc;
import model.KyHoc;
import model.User;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.stream.Collectors;

public class TaoLopNguyenVongController extends BaseController {

    @FXML private ComboBox<MonHoc> cbMonHoc;
    @FXML private ComboBox<KyHoc> cbKyHoc;
    @FXML private TextArea taLyDo;
    @FXML private Button btnGuiYeuCau;
    @FXML private Label lblStatus;

    private User loggedInUser;

    public void initializeData(User user) {
        this.loggedInUser = user;
        if (loggedInUser != null && "sinhvien".equals(loggedInUser.getRole())) {
             loadFilteredMonHoc(); 
        } else {
             showError(lblStatus, "Chức năng này chỉ dành cho Sinh viên.");
             btnGuiYeuCau.setDisable(true);
        }
        loadDanhSachKyHoc();
    }

    @FXML public void initialize() {
        // Converter cho ComboBox Môn học
        cbMonHoc.setConverter(new StringConverter<MonHoc>() {
            @Override public String toString(MonHoc m) { return (m == null) ? null : m.getMaMon() + " - " + m.getTenMon(); }
            @Override public MonHoc fromString(String s) { return null; }
        });
        
        // Converter cho ComboBox Kỳ học
        cbKyHoc.setConverter(new StringConverter<KyHoc>() {
            @Override public String toString(KyHoc k) { return k == null ? null : "Học kỳ " + k.getHocKy() + " - Năm " + k.getNamHoc(); }
            @Override public KyHoc fromString(String string) { return null; }
        });
    }

    private void loadFilteredMonHoc() {
        try {
            ObservableList<MonHoc> allMonHoc = MonHocDAO.getAllMonHoc();
            ObservableList<MonHoc> eligibleMonHoc = FXCollections.observableArrayList();
            String maSV = loggedInUser.getMaSV();

            for (MonHoc mh : allMonHoc) {
                if (SinhVienDangKyDAO.checkConditionForWishlist(maSV, mh.getMaMon())) {
                    eligibleMonHoc.add(mh);
                }
            }

            cbMonHoc.setItems(eligibleMonHoc);
            
            if (eligibleMonHoc.isEmpty()) {
                cbMonHoc.setPromptText("Bạn không có môn nào cần học lại/cải thiện.");
                cbMonHoc.setDisable(true);
                btnGuiYeuCau.setDisable(true);
            } else {
                cbMonHoc.setPromptText("Chọn môn học...");
            }

        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void loadDanhSachKyHoc() {
        try {
            ObservableList<KyHoc> allKys = KyHocDAO.getAllKyHoc();
            int currentYear = LocalDate.now().getYear();
            ObservableList<KyHoc> displayKys = allKys.stream()
                .filter(k -> k.getNamHoc() >= currentYear - 1) 
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
            
            if (displayKys.isEmpty()) {
                cbKyHoc.setItems(allKys);
            } else {
                cbKyHoc.setItems(displayKys);
            }

            if (!cbKyHoc.getItems().isEmpty()) {
                cbKyHoc.getSelectionModel().selectFirst();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleGuiYeuCau() {
        MonHoc mh = cbMonHoc.getValue();
        KyHoc kh = cbKyHoc.getValue(); 
        String lyDo = taLyDo.getText();
        String maSV = loggedInUser.getMaSV();

        if (mh == null || kh == null) {
            showError(lblStatus, "Vui lòng chọn Môn học và Kỳ học."); return;
        }
        
        if (!SinhVienDangKyDAO.checkConditionForWishlist(maSV, mh.getMaMon())) {
             showError(lblStatus, "Bạn không đủ điều kiện đăng ký môn này.");
             return;
        }

        try {
            int existingClassId = findExistingWishlistClass(mh.getMaMon(), kh.getId());
            int targetClassId = -1;

            if (existingClassId != -1) {
                // Gộp đơn
                if (isStudentRegistered(existingClassId, maSV)) {
                     showError(lblStatus, "Bạn đã đăng ký nguyện vọng này rồi.");
                     return;
                }
                targetClassId = existingClassId;
            } else {
                // Tạo mới - CẢI THIỆN TÊN LỚP TẠI ĐÂY
                // Format cũ: "NV_" + mh.getMaMon() + "_" + kh.getNamHoc() + "_K" + kh.getHocKy();
                
                // Format mới: "NV [Mã] - [Tên Môn] (HK[Kỳ]/[Năm])"
                // Ví dụ: NV IT301 - Lập trình Java (HK1/2024)
                String tenTuSinh = String.format("NV %s - %s (HK%d/%d)", 
                    mh.getMaMon(), mh.getTenMon(), kh.getHocKy(), kh.getNamHoc());
                
                // Đảm bảo tên không quá dài cho cột DB (VARCHAR 100)
                if (tenTuSinh.length() > 95) {
                    tenTuSinh = tenTuSinh.substring(0, 95) + "...";
                }

                LopHocPhan yeuCau = new LopHocPhan(0, null, mh.getMaMon(), kh.getId(), kh.getHocKy(), kh.getNamHoc(), null, tenTuSinh, "NguyenVong", "CHO_DUYET", "", "", lyDo);

                if (LopHocPhanDAO.createClass(yeuCau)) {
                    targetClassId = findExistingWishlistClass(mh.getMaMon(), kh.getId());
                }
            }

            if (targetClassId != -1) {
                if (KetQuaHocTapDAO.registerStudent(targetClassId, maSV)) {
                    int currentSize = KetQuaHocTapDAO.countStudentsInClass(targetClassId);
                    String msg = "Đăng ký thành công! Sĩ số hiện tại: " + currentSize;
                    if (currentSize >= 10) {
                        LopHocPhanDAO.approveClass(targetClassId);
                        msg += "\nLớp đã đủ điều kiện và được TỰ ĐỘNG MỞ!";
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Chúc mừng! Lớp đã đủ sĩ số và được mở tự động.");
                        alert.showAndWait();
                    }
                    showSuccess(lblStatus, msg);
                    closeWindowDelay();
                } else {
                    showError(lblStatus, "Lỗi khi lưu đăng ký.");
                }
            } else {
                showError(lblStatus, "Lỗi hệ thống (Không tìm thấy ID lớp).");
            }

        } catch (Exception e) {
            showError(lblStatus, "Lỗi xử lý: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int findExistingWishlistClass(String maMon, int idKyHoc) {
        String sql = "SELECT id FROM lop_hoc_phan WHERE maMon = ? AND idKyHoc = ? AND loaiLop = 'NguyenVong' AND trangThai IN ('CHO_DUYET', 'DA_DUYET') LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maMon);
            ps.setInt(2, idKyHoc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }
    
    private boolean isStudentRegistered(int classId, String maSV) {
        String sql = "SELECT 1 FROM ket_qua_hoc_tap WHERE idLopHocPhan = ? AND maSV = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setString(2, maSV);
            return ps.executeQuery().next();
        } catch (Exception e) { return false; }
    }

    private void closeWindowDelay() {
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override public void run() {
                javafx.application.Platform.runLater(() -> {
                    Stage stage = (Stage) btnGuiYeuCau.getScene().getWindow();
                    stage.close();
                });
            }
        }, 1500);
    }
}