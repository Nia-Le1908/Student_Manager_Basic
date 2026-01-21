package controller;

import dao.GiangVienDAO;
import dao.KyHocDAO;
import dao.MonHocDAO;
import dao.LopHocPhanDAO;
import dao.SinhVienDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import model.GiangVien;
import model.KyHoc;
import model.LopHocPhan;
import model.MonHoc;

public class PhanCongLopController extends BaseController {

    @FXML private ComboBox<GiangVien> cbGiangVien;
    @FXML private ComboBox<MonHoc> cbMonHoc;
    @FXML private ComboBox<String> cbLop; // Chỉ dùng để chọn Lớp HC mới
    @FXML private ComboBox<KyHoc> cbKyHoc;
    
    // MỚI: TextField hiển thị tên lớp đang chọn
    @FXML private TextField tfTenLopHienThi;
    
    @FXML private Button btnPhanCong;
    @FXML private Button btnCapNhat;
    @FXML private Button btnXoa;
    @FXML private Button btnLamMoi;
    
    @FXML private Label lblStatus;
    
    @FXML private TableView<LopHocPhan> tablePhanCong;
    @FXML private TableColumn<LopHocPhan, String> colMaGV;
    @FXML private TableColumn<LopHocPhan, String> colTenGV;
    @FXML private TableColumn<LopHocPhan, String> colMaMon;
    @FXML private TableColumn<LopHocPhan, String> colTenMon;
    @FXML private TableColumn<LopHocPhan, String> colMaLop;
    @FXML private TableColumn<LopHocPhan, Integer> colHocKy;
    @FXML private TableColumn<LopHocPhan, Integer> colNamHoc;
    @FXML private TableColumn<LopHocPhan, String> colTrangThai;

    @FXML
    public void initialize() {
        colMaGV.setCellValueFactory(new PropertyValueFactory<>("maGV"));
        colTenGV.setCellValueFactory(new PropertyValueFactory<>("tenGV"));
        colMaMon.setCellValueFactory(new PropertyValueFactory<>("maMon"));
        colTenMon.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
        
        // Sử dụng getTenHienThi() để hiển thị đúng tên lớp (HC hoặc NV)
        colMaLop.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTenHienThi()));
        
        colHocKy.setCellValueFactory(new PropertyValueFactory<>("hocKy"));
        colNamHoc.setCellValueFactory(new PropertyValueFactory<>("namHoc"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        loadGiangVien();
        loadLopSinhVien();
        loadKyHoc();
        loadData();

        cbGiangVien.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) loadMonHocTheoGV(newV.getMaGV());
        });
        
        cbKyHoc.setConverter(new StringConverter<KyHoc>() {
            @Override public String toString(KyHoc k) { return k == null ? null : "Học kỳ " + k.getHocKy() + " - Năm " + k.getNamHoc(); }
            @Override public KyHoc fromString(String string) { return null; }
        });

        tablePhanCong.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                fillForm(newV);
                btnPhanCong.setDisable(true); // Không cho thêm khi đang chọn sửa
                btnCapNhat.setDisable(false);
                btnXoa.setDisable(false);
            }
        });
        
        btnCapNhat.setDisable(true);
        btnXoa.setDisable(true);
    }
    
    private void fillForm(LopHocPhan lhp) {
        // Điền tên lớp vào ô hiển thị (QUAN TRỌNG)
        tfTenLopHienThi.setText(lhp.getTenHienThi());
        
        // Chọn Giảng viên (Xử lý null nếu chưa phân công)
        cbGiangVien.setValue(null); 
        if (lhp.getMaGV() != null) {
            for (GiangVien gv : cbGiangVien.getItems()) {
                if (gv.getMaGV().equals(lhp.getMaGV())) {
                    cbGiangVien.setValue(gv);
                    break;
                }
            }
        }

        // Chọn Kỳ học
        for (KyHoc kh : cbKyHoc.getItems()) {
            if (kh.getHocKy() == lhp.getHocKy() && kh.getNamHoc() == lhp.getNamHoc()) {
                cbKyHoc.setValue(kh);
                break;
            }
        }
        
        // Nếu là lớp Hành chính thì chọn trong ComboBox, nếu NV thì bỏ qua
        if ("HanhChinh".equalsIgnoreCase(lhp.getLoaiLop())) {
            cbLop.setValue(lhp.getMaLopHanhChinh());
        } else {
            cbLop.setValue(null); // Clear selection vì đây là lớp NV
        }
        
        javafx.application.Platform.runLater(() -> {
            for (MonHoc mh : cbMonHoc.getItems()) {
                if (mh.getMaMon().equals(lhp.getMaMon())) {
                    cbMonHoc.setValue(mh);
                    break;
                }
            }
        });
    }

    private void loadData() {
        ObservableList<LopHocPhan> list = LopHocPhanDAO.getLopHocPhan(null);
        tablePhanCong.setItems(list);
    }

    private void loadGiangVien() { 
        cbGiangVien.setItems(GiangVienDAO.getAllGiangVien()); 
    }
    
    private void loadLopSinhVien() { 
        cbLop.setItems(FXCollections.observableArrayList(SinhVienDAO.getAllLop())); 
    }
    
    private void loadKyHoc() {
        try {
            ObservableList<KyHoc> listKy = KyHocDAO.getAllKyHoc();
            cbKyHoc.setItems(listKy);
            if (!listKy.isEmpty()) cbKyHoc.getSelectionModel().selectFirst();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadMonHocTheoGV(String maGV) {
        ObservableList<MonHoc> listMon = MonHocDAO.getMonHocByKhoaOfGiangVien(maGV);
        cbMonHoc.setItems(listMon);
    }

    @FXML
    private void handlePhanCong() {
        // Logic thêm mới chỉ dành cho Lớp Hành Chính
        if (cbLop.getValue() == null) {
             showError(lblStatus, "Để thêm mới, vui lòng chọn Lớp Sinh Viên (HC).");
             return;
        }
        if (!validateInput(true)) return;

        try {
            LopHocPhan newClass = createModelFromForm();
            if (LopHocPhanDAO.createClass(newClass)) { 
                showSuccess(lblStatus, "Phân công thành công!");
                loadData();
                handleLamMoi();
            } else {
                showError(lblStatus, "Lỗi phân công. Có thể lớp này đã tồn tại.");
            }
        } catch (Exception e) { 
            showError(lblStatus, "Lỗi hệ thống: " + e.getMessage()); 
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCapNhat() {
        LopHocPhan selected = tablePhanCong.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        // Khi cập nhật, chỉ cần quan tâm GV, Môn, Kỳ (Lớp giữ nguyên ID)
        if (cbGiangVien.getValue() == null) {
             showError(lblStatus, "Vui lòng chọn Giảng viên để phân công.");
             return;
        }
        
        try {
            LopHocPhan fromForm = createModelFromForm();
            
            // Giữ nguyên các thông tin gốc quan trọng (Loại lớp, Tên lớp NV...)
            LopHocPhan finalUpdate = new LopHocPhan(
                selected.getId(), 
                fromForm.getMaGV(), 
                fromForm.getMaMon(), 
                fromForm.getIdKyHoc(),
                fromForm.getHocKy(), fromForm.getNamHoc(),
                selected.getMaLopHanhChinh(), // Giữ nguyên lớp
                selected.getTenLopHocPhan(),  // Giữ nguyên tên
                selected.getLoaiLop(), 
                selected.getTrangThai(),
                "", "", ""
            );
            
            if (LopHocPhanDAO.updateAssignment(finalUpdate)) {
                showSuccess(lblStatus, "Cập nhật thành công!");
                loadData();
                handleLamMoi();
            } else {
                showError(lblStatus, "Cập nhật thất bại.");
            }
        } catch (Exception e) {
             showError(lblStatus, "Lỗi: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleXoa() {
        LopHocPhan selected = tablePhanCong.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa phân công này?");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (LopHocPhanDAO.deleteAssignment(selected.getId())) {
                showSuccess(lblStatus, "Đã xóa phân công.");
                loadData();
                handleLamMoi();
            } else {
                showError(lblStatus, "Không thể xóa (Lớp đã có dữ liệu).");
            }
        }
    }
    
    @FXML
    private void handleLamMoi() {
        cbGiangVien.getSelectionModel().clearSelection();
        cbMonHoc.getSelectionModel().clearSelection();
        cbLop.getSelectionModel().clearSelection();
        tfTenLopHienThi.clear(); // Clear ô hiển thị
        
        tablePhanCong.getSelectionModel().clearSelection();
        btnPhanCong.setDisable(false);
        btnCapNhat.setDisable(true);
        btnXoa.setDisable(true);
        clearStatus(lblStatus);
    }

    private boolean validateInput(boolean isCreate) {
        if (cbGiangVien.getValue() == null || cbMonHoc.getValue() == null || cbKyHoc.getValue() == null) {
            showError(lblStatus, "Vui lòng chọn đầy đủ thông tin."); 
            return false;
        }
        return true;
    }
    
    private LopHocPhan createModelFromForm() {
        GiangVien gv = cbGiangVien.getValue();
        MonHoc mh = cbMonHoc.getValue();
        String lop = cbLop.getValue(); 
        KyHoc kh = cbKyHoc.getValue();
        
        String maGV = (gv != null) ? gv.getMaGV() : null;
        String maMon = (mh != null) ? mh.getMaMon() : null;
        int idKy = (kh != null) ? kh.getId() : 0;
        int hocKy = (kh != null) ? kh.getHocKy() : 0;
        int namHoc = (kh != null) ? kh.getNamHoc() : 0;
        
        return new LopHocPhan(
            0, maGV, maMon, 
            idKy, hocKy, namHoc, 
            lop, null, "HanhChinh", "DA_DUYET", 
            "", "", ""
        );
    }
}