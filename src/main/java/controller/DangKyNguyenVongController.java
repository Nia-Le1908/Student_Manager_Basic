package controller;

import dao.KetQuaHocTapDAO;
import dao.LopHocPhanDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.LopHocPhan; // Model Mới
import model.User;

public class DangKyNguyenVongController extends BaseController {

    @FXML private TableView<LopHocPhan> tableLopNguyenVong;
    @FXML private TableColumn<LopHocPhan, String> colTenMon;
    @FXML private TableColumn<LopHocPhan, String> colMaLopMoi; // Hiển thị tên lớp NV
    @FXML private TableColumn<LopHocPhan, String> colTenGV;
    @FXML private TableColumn<LopHocPhan, Integer> colHocKy;
    @FXML private TableColumn<LopHocPhan, Integer> colNamHoc;
    @FXML private Button btnDangKy;
    @FXML private Label lblStatus;

    private User loggedInStudent;

    public void initializeData(User user) {
        this.loggedInStudent = user;
        if (loggedInStudent == null || !"sinhvien".equals(loggedInStudent.getRole())) {
            showError(lblStatus, "Lỗi xác thực sinh viên.");
            tableLopNguyenVong.setDisable(true);
        } else {
            loadAvailableClasses();
        }
    }

    @FXML
    public void initialize() {
        colTenMon.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
        colMaLopMoi.setCellValueFactory(new PropertyValueFactory<>("tenLopHocPhan")); // Tên lớp NV
        colTenGV.setCellValueFactory(new PropertyValueFactory<>("tenGV"));
        colHocKy.setCellValueFactory(new PropertyValueFactory<>("hocKy"));
        colNamHoc.setCellValueFactory(new PropertyValueFactory<>("namHoc"));

        btnDangKy.setDisable(true);
        tableLopNguyenVong.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> btnDangKy.setDisable(newVal == null)
        );
    }

    private void loadAvailableClasses() {
        if (loggedInStudent == null) return;
        // Gọi DAO lấy danh sách lớp NV chưa đăng ký
        ObservableList<LopHocPhan> list = LopHocPhanDAO.getAvailableWishlistClasses(loggedInStudent.getMaSV());
        tableLopNguyenVong.setItems(list);
        if (list.isEmpty()) {
            tableLopNguyenVong.setPlaceholder(new Label("Hiện không có lớp nguyện vọng nào để đăng ký."));
        }
    }

    @FXML
    private void handleDangKy() {
        LopHocPhan selected = tableLopNguyenVong.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Đăng ký vào lớp này?");
        alert.setHeaderText("Môn: " + selected.getTenMon() + "\nGV: " + selected.getTenGV());
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Gọi DAO đăng ký (Insert vào ket_qua_hoc_tap)
            boolean success = KetQuaHocTapDAO.registerStudent(selected.getId(), loggedInStudent.getMaSV());
            
            if (success) {
                showSuccess(lblStatus, "Đăng ký thành công!");
                loadAvailableClasses(); // Refresh
            } else {
                showError(lblStatus, "Đăng ký thất bại.");
            }
        }
    }
}