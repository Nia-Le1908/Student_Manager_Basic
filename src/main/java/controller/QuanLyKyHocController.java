package controller;

import dao.KyHocDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.KyHoc;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

public class QuanLyKyHocController extends BaseController {

    // SỬA: Dùng ComboBox thay vì TextField
    @FXML private ComboBox<Integer> cbHocKy;
    @FXML private ComboBox<Integer> cbNamHoc;
    
    @FXML private DatePicker dpBatDau;
    @FXML private DatePicker dpKetThuc;
    
    @FXML private TableView<KyHoc> tableKyHoc;
    @FXML private TableColumn<KyHoc, Integer> colHocKy;
    @FXML private TableColumn<KyHoc, Integer> colNamHoc;
    @FXML private TableColumn<KyHoc, String> colBatDau;
    @FXML private TableColumn<KyHoc, String> colKetThuc;
    
    @FXML private Label lblStatus;

    @FXML
    public void initialize() {
        // Cấu hình bảng
        colHocKy.setCellValueFactory(new PropertyValueFactory<>("hocKy"));
        colNamHoc.setCellValueFactory(new PropertyValueFactory<>("namHoc"));
        colBatDau.setCellValueFactory(new PropertyValueFactory<>("ngayBatDauNhapDiem"));
        colKetThuc.setCellValueFactory(new PropertyValueFactory<>("ngayKetThucNhapDiem"));
        
        // --- CẤU HÌNH COMBOBOX ---
        // Học kỳ: 1, 2, 3
        cbHocKy.setItems(FXCollections.observableArrayList(1, 2, 3));
        cbHocKy.getSelectionModel().selectFirst();

        // Năm học: Năm ngoái, Năm nay, Năm sau (Tránh nhập năm 2066)
        int currentYear = LocalDate.now().getYear();
        cbNamHoc.setItems(FXCollections.observableArrayList(
            currentYear - 1, 
            currentYear, 
            currentYear + 1
        ));
        cbNamHoc.getSelectionModel().select(Integer.valueOf(currentYear)); // Mặc định chọn năm nay
        
        loadData();
        
        // Auto-fill khi chọn dòng
        tableKyHoc.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                cbHocKy.setValue(newV.getHocKy());
                cbNamHoc.setValue(newV.getNamHoc());
                
                if (newV.getNgayBatDauNhapDiem() != null)
                    dpBatDau.setValue(newV.getNgayBatDauNhapDiem().toLocalDateTime().toLocalDate());
                if (newV.getNgayKetThucNhapDiem() != null)
                    dpKetThuc.setValue(newV.getNgayKetThucNhapDiem().toLocalDateTime().toLocalDate());
                
                // Khóa sửa HK/Năm để tránh lỗi logic, chỉ cho sửa ngày
                cbHocKy.setDisable(true);
                cbNamHoc.setDisable(true);
            }
        });
    }

    private void loadData() {
        ObservableList<KyHoc> list = KyHocDAO.getAllKyHoc();
        tableKyHoc.setItems(list);
    }

    @FXML
    private void handleThem() {
        try {
            // Lấy giá trị từ ComboBox
            Integer hk = cbHocKy.getValue();
            Integer nam = cbNamHoc.getValue();
            
            if (hk == null || nam == null) {
                showError(lblStatus, "Vui lòng chọn Học kỳ và Năm học.");
                return;
            }
            
            if (dpBatDau.getValue() == null || dpKetThuc.getValue() == null) {
                showError(lblStatus, "Vui lòng chọn ngày bắt đầu và kết thúc nhập điểm.");
                return;
            }
            
            Timestamp start = Timestamp.valueOf(dpBatDau.getValue().atStartOfDay());
            Timestamp end = Timestamp.valueOf(dpKetThuc.getValue().atTime(LocalTime.MAX));
            
            if (start.after(end)) {
                showError(lblStatus, "Ngày bắt đầu phải trước ngày kết thúc.");
                return;
            }

            KyHoc kh = new KyHoc(0, hk, nam, start, end);
            if (KyHocDAO.insert(kh)) {
                showSuccess(lblStatus, "Thêm kỳ học thành công!");
                loadData();
                handleLamMoi();
            } else {
                showError(lblStatus, "Lỗi: Kỳ học này có thể đã tồn tại.");
            }
        } catch (Exception e) {
            showError(lblStatus, "Lỗi dữ liệu: " + e.getMessage());
        }
    }

    @FXML
    private void handleSua() {
        KyHoc selected = tableKyHoc.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError(lblStatus, "Chọn kỳ học để sửa thời gian nhập điểm.");
            return;
        }
        
        if (dpBatDau.getValue() == null || dpKetThuc.getValue() == null) return;
        
        Timestamp start = Timestamp.valueOf(dpBatDau.getValue().atStartOfDay());
        Timestamp end = Timestamp.valueOf(dpKetThuc.getValue().atTime(LocalTime.MAX));
        
        KyHoc updateKh = new KyHoc(selected.getId(), selected.getHocKy(), selected.getNamHoc(), start, end);
        
        if (KyHocDAO.update(updateKh)) {
            showSuccess(lblStatus, "Cập nhật thời gian thành công!");
            loadData();
            handleLamMoi();
        } else {
            showError(lblStatus, "Lỗi cập nhật.");
        }
    }

    @FXML
    private void handleXoa() {
        KyHoc selected = tableKyHoc.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        if (KyHocDAO.delete(selected.getId())) {
            showSuccess(lblStatus, "Đã xóa kỳ học.");
            loadData();
            handleLamMoi();
        } else {
            showError(lblStatus, "Không thể xóa kỳ học đã có dữ liệu lớp học.");
        }
    }

    @FXML
    private void handleLamMoi() {
        cbHocKy.getSelectionModel().selectFirst();
        cbNamHoc.getSelectionModel().select(Integer.valueOf(LocalDate.now().getYear()));
        
        dpBatDau.setValue(null);
        dpKetThuc.setValue(null);
        
        // Mở lại khả năng chọn khi làm mới
        cbHocKy.setDisable(false);
        cbNamHoc.setDisable(false);
        
        tableKyHoc.getSelectionModel().clearSelection();
        clearStatus(lblStatus);
    }
}