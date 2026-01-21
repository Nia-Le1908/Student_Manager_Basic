package controller;

import dao.KhoaDAO;
import dao.LopHanhChinhDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import model.Khoa;
import model.LopHanhChinh;

public class LopHanhChinhController extends BaseController {

    @FXML private StackPane rootPane; // Để hiện loading overlay
    @FXML private TextField tfMaLop, tfTenLop, tfKhoaHoc;
    @FXML private ComboBox<Khoa> cbKhoa;
    @FXML private TableView<LopHanhChinh> tableLop;
    @FXML private TableColumn<LopHanhChinh, String> colMaLop, colTenLop;
    @FXML private TableColumn<LopHanhChinh, Integer> colKhoaHoc;
    @FXML private TableColumn<LopHanhChinh, String> colKhoa;
    @FXML private Label lblStatus;

    @FXML
    public void initialize() {
        // Setup bảng
        colMaLop.setCellValueFactory(data -> data.getValue().maLopProperty());
        colTenLop.setCellValueFactory(data -> data.getValue().tenLopProperty());
        colKhoaHoc.setCellValueFactory(data -> data.getValue().khoaHocProperty().asObject());
        colKhoa.setCellValueFactory(data -> data.getValue().getKhoa().tenKhoaProperty());

        // Setup ComboBox Khoa
        cbKhoa.setItems(KhoaDAO.getAllKhoa());
        cbKhoa.setConverter(new StringConverter<Khoa>() {
            @Override public String toString(Khoa k) { return k == null ? null : k.getTenKhoa(); }
            @Override public Khoa fromString(String s) { return null; }
        });

        // Sự kiện chọn dòng
        tableLop.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                tfMaLop.setText(newV.getMaLop());
                tfMaLop.setDisable(true); // Khóa mã lớp khi sửa
                tfTenLop.setText(newV.getTenLop());
                tfKhoaHoc.setText(String.valueOf(newV.getKhoaHoc()));
                
                // Chọn đúng khoa trong combobox
                for(Khoa k : cbKhoa.getItems()) {
                    if(k.getMaKhoa().equals(newV.getKhoa().getMaKhoa())) {
                        cbKhoa.setValue(k);
                        break;
                    }
                }
            }
        });

        loadData();
    }

    private void loadData() {
        runTaskWithOverlay(
            () -> LopHanhChinhDAO.getAll(), 
            rootPane, 
            list -> tableLop.setItems(list), 
            err -> showError("Lỗi tải dữ liệu: " + err.getMessage())
        );
    }

    @FXML
    private void handleThem() {
        if (!validateInput()) return;
        LopHanhChinh l = new LopHanhChinh(
            tfMaLop.getText().trim().toUpperCase(),
            tfTenLop.getText().trim(),
            cbKhoa.getValue(),
            Integer.parseInt(tfKhoaHoc.getText().trim())
        );

        if (LopHanhChinhDAO.insert(l)) {
            showSuccess("Thêm lớp thành công!");
            loadData();
            handleLamMoi();
        } else {
            showError("Lỗi: Mã lớp đã tồn tại hoặc lỗi hệ thống.");
        }
    }

    @FXML
    private void handleSua() {
        LopHanhChinh selected = tableLop.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!validateInput()) return;

        selected.setTenLop(tfTenLop.getText().trim());
        selected.setKhoaHoc(Integer.parseInt(tfKhoaHoc.getText().trim()));
        selected.setKhoa(cbKhoa.getValue());

        if (LopHanhChinhDAO.update(selected)) {
            showSuccess("Cập nhật thành công!");
            tableLop.refresh();
            handleLamMoi();
        } else {
            showError("Lỗi cập nhật.");
        }
    }

    @FXML
    private void handleXoa() {
        LopHanhChinh selected = tableLop.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa lớp " + selected.getMaLop() + "?\nLưu ý: Không thể xóa nếu đã có sinh viên.");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (LopHanhChinhDAO.delete(selected.getMaLop())) {
                showSuccess("Đã xóa lớp.");
                loadData();
                handleLamMoi();
            } else {
                showError("Không thể xóa. Lớp này đang có sinh viên.");
            }
        }
    }

    @FXML
    private void handleLamMoi() {
        tfMaLop.clear(); tfMaLop.setDisable(false);
        tfTenLop.clear();
        tfKhoaHoc.clear();
        cbKhoa.getSelectionModel().clearSelection();
        tableLop.getSelectionModel().clearSelection();
        clearStatus(lblStatus);
    }

    private boolean validateInput() {
        if (tfMaLop.getText().isEmpty() || tfTenLop.getText().isEmpty() || tfKhoaHoc.getText().isEmpty() || cbKhoa.getValue() == null) {
            showError("Vui lòng nhập đủ thông tin.");
            return false;
        }
        try {
            Integer.parseInt(tfKhoaHoc.getText().trim());
        } catch (NumberFormatException e) {
            showError("Khóa học phải là số (VD: 2024).");
            return false;
        }
        return true;
    }
}