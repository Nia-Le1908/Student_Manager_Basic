package controller;

import dao.LopHocPhanDAO; // DAO mới
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.LopHocPhan; // Model mới

public class DuyetLopNguyenVongController extends BaseController {

    @FXML private TableView<LopHocPhan> tableYeuCau;
    @FXML private TableColumn<LopHocPhan, String> colMaGV;
    @FXML private TableColumn<LopHocPhan, String> colTenGV;
    @FXML private TableColumn<LopHocPhan, String> colMaMon;
    @FXML private TableColumn<LopHocPhan, String> colTenMon;
    @FXML private TableColumn<LopHocPhan, Integer> colHocKy;
    @FXML private TableColumn<LopHocPhan, Integer> colNamHoc;
    @FXML private TableColumn<LopHocPhan, String> colLyDo;
    @FXML private TableColumn<LopHocPhan, String> colTrangThai;

    @FXML private Label lblSelectedRequest;
    @FXML private TextField tfMaLopHocPhanMoi;
    @FXML private Button btnDuyet;
    @FXML private Button btnTuChoi;
    @FXML private Button btnXoa;
    @FXML private Label lblStatus;

    private LopHocPhan selectedRequest = null;

    public void initializeData(String username) {
        // Admin user, không cần lưu
    }

    @FXML
    public void initialize() {
        colMaGV.setCellValueFactory(new PropertyValueFactory<>("maGV"));
        colTenGV.setCellValueFactory(new PropertyValueFactory<>("tenGV"));
        colMaMon.setCellValueFactory(new PropertyValueFactory<>("maMon"));
        colTenMon.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
        colHocKy.setCellValueFactory(new PropertyValueFactory<>("hocKy"));
        colNamHoc.setCellValueFactory(new PropertyValueFactory<>("namHoc"));
        colLyDo.setCellValueFactory(new PropertyValueFactory<>("lyDoMoLop"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        loadAllRequests();

        tableYeuCau.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            this.selectedRequest = newV;
            boolean isSelected = (newV != null);
            lblSelectedRequest.setText(isSelected ? (newV.getTenMon() + " - " + newV.getTenGV()) : "...");
            
            boolean isPending = isSelected && "CHO_DUYET".equals(newV.getTrangThai());
            btnDuyet.setDisable(!isPending);
            btnTuChoi.setDisable(!isPending);
            btnXoa.setDisable(!isSelected);
        });
    }

    private void loadAllRequests() {
        // Lấy danh sách từ DAO mới
        ObservableList<LopHocPhan> list = LopHocPhanDAO.getPendingRequests();
        tableYeuCau.setItems(list);
    }

    @FXML
    private void handleDuyet() {
        if (selectedRequest == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Duyệt yêu cầu này?");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Duyệt đơn giản: Update trạng thái -> DA_DUYET
            if (LopHocPhanDAO.approveClass(selectedRequest.getId())) {
                showSuccess(lblStatus, "Đã duyệt!");
                loadAllRequests();
            } else {
                showError(lblStatus, "Lỗi duyệt.");
            }
        }
    }

    @FXML
    private void handleTuChoi() {
        handleXoa(); // Từ chối có thể hiểu là xóa yêu cầu
    }

    @FXML
    private void handleXoa() {
        if (selectedRequest == null) return;
        if (LopHocPhanDAO.rejectOrDeleteClass(selectedRequest.getId())) {
            showSuccess(lblStatus, "Đã xóa yêu cầu.");
            loadAllRequests();
        }
    }
}