package controller;

import dao.KhoaDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import model.Khoa;

public class KhoaController {

    @FXML private TextField tfMaKhoa;
    @FXML private TextField tfTenKhoa;
    @FXML private TableView<Khoa> tvKhoa;
    @FXML private TableColumn<Khoa, String> colMaKhoa;
    @FXML private TableColumn<Khoa, String> colTenKhoa;
    @FXML private Label lblStatus;

    private ObservableList<Khoa> dsKhoa;

    @FXML
    public void initialize() {
        colMaKhoa.setCellValueFactory(data -> data.getValue().maKhoaProperty());
        colTenKhoa.setCellValueFactory(data -> data.getValue().tenKhoaProperty());

        // Listener để tự động điền form khi chọn khoa
        tvKhoa.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showKhoaDetails(newValue));

        loadData();
    }

    private void loadData() {
        dsKhoa = KhoaDAO.getAllKhoa();
        tvKhoa.setItems(dsKhoa);
        clearForm();
        showStatus("", false); // Xóa thông báo cũ
    }

    @FXML
    private void handleThem() {
        String maKhoa = tfMaKhoa.getText().trim().toUpperCase(); // Chuẩn hóa mã khoa
        String tenKhoa = tfTenKhoa.getText().trim();

        if (maKhoa.isEmpty() || tenKhoa.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Vui lòng nhập đầy đủ Mã Khoa và Tên Khoa.");
            return;
        }

        Khoa khoa = new Khoa(maKhoa, tenKhoa);
        if (KhoaDAO.insert(khoa)) {
            loadData();
            showStatus("Thêm khoa thành công!", false);
        } else {
            showStatus("Thêm khoa thất bại! Mã hoặc tên khoa có thể đã tồn tại.", true);
        }
    }

    @FXML
    private void handleSua() {
        Khoa selectedKhoa = tvKhoa.getSelectionModel().getSelectedItem();
        if (selectedKhoa == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa Chọn Khoa", "Vui lòng chọn một khoa trong bảng để sửa.");
            return;
        }

        String tenKhoaMoi = tfTenKhoa.getText().trim();
        if (tenKhoaMoi.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Tên Khoa không được để trống.");
            return;
        }

        // Chỉ cho phép sửa tên khoa, không sửa mã khoa
        selectedKhoa.setTenKhoa(tenKhoaMoi);

        if (KhoaDAO.update(selectedKhoa)) {
            loadData(); // Tải lại để cập nhật bảng
             showStatus("Cập nhật khoa thành công!", false);
        } else {
             showStatus("Cập nhật khoa thất bại! Tên khoa có thể đã tồn tại.", true);
            // Có thể cần tải lại dữ liệu gốc nếu cập nhật thất bại
            loadData();
        }
    }

    @FXML
    private void handleXoa() {
        Khoa selectedKhoa = tvKhoa.getSelectionModel().getSelectedItem();
        if (selectedKhoa == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa Chọn Khoa", "Vui lòng chọn một khoa trong bảng để xóa.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác Nhận Xóa");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa khoa '" + selectedKhoa.getTenKhoa() + "'?");
        confirmAlert.setContentText("Hành động này không thể hoàn tác. Giảng viên và Sinh viên thuộc khoa này sẽ bị mất liên kết Khoa.");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            if (KhoaDAO.delete(selectedKhoa.getMaKhoa())) {
                loadData();
                showStatus("Xóa khoa thành công!", false);
            } else {
                 showStatus("Xóa khoa thất bại! Có thể do Khoa đang được sử dụng.", true);
            }
        }
    }

    @FXML
    private void handleLamMoi() {
        loadData();
    }

    private void showKhoaDetails(Khoa khoa) {
        if (khoa != null) {
            tfMaKhoa.setText(khoa.getMaKhoa());
            tfTenKhoa.setText(khoa.getTenKhoa());
            tfMaKhoa.setDisable(true); // Không cho sửa mã khoa khi đã chọn
        } else {
            clearForm();
        }
         showStatus("", false); // Xóa thông báo khi chọn dòng khác
    }

    private void clearForm() {
        tvKhoa.getSelectionModel().clearSelection(); // Bỏ chọn dòng
        tfMaKhoa.clear();
        tfTenKhoa.clear();
        tfMaKhoa.setDisable(false); // Cho phép nhập mã khoa khi form trống
    }

     private void showStatus(String message, boolean isError) {
        lblStatus.setText(message);
        lblStatus.setTextFill(isError ? Color.RED : Color.GREEN);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
