package controller;

import dao.KhoaDAO; 
import dao.MonHocDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.util.StringConverter;
import model.Khoa;
import model.MonHoc;

import java.util.Optional;

public class MonHocController {

    @FXML private TableView<MonHoc> tableMonHoc;
    @FXML private TableColumn<MonHoc, String> colMaMon, colTenMon, colLoaiMon, colMonTienQuyet;
    @FXML private TableColumn<MonHoc, Integer> colSoTinChi, colHocKy;
    @FXML private TableColumn<MonHoc, Khoa> colKhoa; // Column for Khoa object
    @FXML private TextField tfMaMon, tfTenMon, tfSoTinChi, tfMonTienQuyet, tfTimKiem, tfHocKy;
    @FXML private ComboBox<String> cbLoaiMon;
    @FXML private ComboBox<Khoa> cbKhoa; // ComboBox for Khoa
    @FXML private Label lblStatus;

    private ObservableList<MonHoc> list;
    private ObservableList<Khoa> dsKhoa; // List of Khoa

    @FXML
    public void initialize() {
        cbLoaiMon.setItems(FXCollections.observableArrayList("Bắt buộc", "Tự chọn"));

        // Load Khoa data
        dsKhoa = KhoaDAO.getAllKhoa();
        cbKhoa.setItems(dsKhoa);
        configureKhoaComboBox();

        // Configure Table Columns
        colMaMon.setCellValueFactory(data -> data.getValue().maMonProperty());
        colTenMon.setCellValueFactory(data -> data.getValue().tenMonProperty());
        colSoTinChi.setCellValueFactory(data -> data.getValue().soTinChiProperty().asObject());
        colLoaiMon.setCellValueFactory(data -> data.getValue().loaiMonProperty());
        colHocKy.setCellValueFactory(data -> data.getValue().hocKyProperty().asObject());
        colKhoa.setCellValueFactory(data -> data.getValue().khoaProperty()); // Bind to Khoa property
        colMonTienQuyet.setCellValueFactory(data -> data.getValue().monTienQuyetProperty());
        configureKhoaTableColumn(); // Custom display for Khoa in table

        tableMonHoc.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showMonHocDetails(newValue));

        loadData();
    }

    private void loadData() {
        list = MonHocDAO.getAllMonHoc();
        tableMonHoc.setItems(list);
        clearFields();
    }

    /** Configure ComboBox to display Khoa name */
    private void configureKhoaComboBox() {
        cbKhoa.setConverter(new StringConverter<Khoa>() {
            @Override public String toString(Khoa khoa) { return khoa == null ? null : khoa.getTenKhoa(); }
            @Override public Khoa fromString(String string) { return null; }
        });
    }

    /** Configure TableColumn to display Khoa name */
    private void configureKhoaTableColumn() {
        colKhoa.setCellFactory(column -> new TableCell<MonHoc, Khoa>() {
            @Override protected void updateItem(Khoa item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null : item.getTenKhoa());
            }
        });
    }


    @FXML
    private void themMonHoc() {
        String maMon = tfMaMon.getText().trim().toUpperCase();
        String tenMon = tfTenMon.getText().trim();
        String soTinChiStr = tfSoTinChi.getText().trim();
        String loaiMon = cbLoaiMon.getValue();
        String hocKyStr = tfHocKy.getText().trim();
        String monTQ = tfMonTienQuyet.getText().trim().toUpperCase();
        Khoa khoa = cbKhoa.getValue(); // Get selected Khoa object

        // --- Validation ---
        if (maMon.isEmpty() || tenMon.isEmpty() || soTinChiStr.isEmpty() || loaiMon == null || hocKyStr.isEmpty()) {
            showStatus("Lỗi: Vui lòng nhập đầy đủ thông tin bắt buộc (*).", true); return;
        }
        int soTinChi, hocKy;
        try {
            soTinChi = Integer.parseInt(soTinChiStr); hocKy = Integer.parseInt(hocKyStr);
            if (soTinChi <= 0 || hocKy <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showStatus("Lỗi: Số tín chỉ và Học kỳ phải là số nguyên dương.", true); return;
        }
       
        if (MonHocDAO.getAllMonHoc().stream().anyMatch(m -> m.getMaMon().equalsIgnoreCase(maMon))) { // Query DB directly for check
             showStatus("Lỗi: Mã môn học đã tồn tại.", true); return;
        }


        // --- Create MonHoc object with Khoa ---
        try {
            MonHoc m = new MonHoc( maMon, tenMon, soTinChi, loaiMon, hocKy, monTQ.isEmpty() ? null : monTQ, khoa ); // Pass Khoa object

            if (MonHocDAO.insert(m)) {
                loadData();
                showStatus("Thêm môn học thành công!", false);
            } else {
                 // The DAO's insert method should print specific errors (like duplicate key)
                 showStatus("Lỗi: Thêm môn học thất bại (kiểm tra Console).", true);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace to console for detailed debugging
            showStatus("Lỗi hệ thống khi thêm: " + e.getMessage(), true);
        }
    }

    @FXML
    private void suaMonHoc() {
        MonHoc selected = tableMonHoc.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Lỗi: Vui lòng chọn môn học cần sửa.", true); return; }

        String tenMon = tfTenMon.getText().trim();
        String soTinChiStr = tfSoTinChi.getText().trim();
        String loaiMon = cbLoaiMon.getValue();
        String hocKyStr = tfHocKy.getText().trim();
        String monTQ = tfMonTienQuyet.getText().trim().toUpperCase();
        Khoa khoa = cbKhoa.getValue(); // Get selected Khoa object

        // --- Validation ---
         if (tenMon.isEmpty() || soTinChiStr.isEmpty() || loaiMon == null || hocKyStr.isEmpty()) {
            showStatus("Lỗi: Vui lòng nhập đầy đủ thông tin bắt buộc (*).", true); return;
        }
        int soTinChi, hocKy;
        try {
            soTinChi = Integer.parseInt(soTinChiStr); hocKy = Integer.parseInt(hocKyStr);
             if (soTinChi <= 0 || hocKy <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showStatus("Lỗi: Số tín chỉ và Học kỳ phải là số nguyên dương.", true); return;
        }
        if (!monTQ.isEmpty() && list.stream().noneMatch(m -> m.getMaMon().equalsIgnoreCase(monTQ) && !m.getMaMon().equalsIgnoreCase(selected.getMaMon()))) {
            showStatus("Lỗi: Mã môn tiên quyết '" + monTQ + "' không tồn tại hoặc không hợp lệ.", true); // Warning only?
        }

        // --- Update MonHoc object and DB ---
        try {
            selected.setTenMon(tenMon);
            selected.setSoTinChi(soTinChi);
            selected.setLoaiMon(loaiMon);
            selected.setHocKy(hocKy);
            selected.setMonTienQuyet(monTQ.isEmpty() ? null : monTQ);
            selected.setKhoa(khoa); // Set Khoa object

            if (MonHocDAO.update(selected)) {
                tableMonHoc.refresh();
                clearFields();
                showStatus("Cập nhật môn học thành công!", false);
            } else {
                 showStatus("Lỗi: Cập nhật môn học thất bại.", true);
            }
         } catch (Exception e) {
            e.printStackTrace();
            showStatus("Lỗi hệ thống: " + e.getMessage(), true);
        }
    }

    @FXML private void xoaMonHoc() { 
        MonHoc selected = tableMonHoc.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Lỗi: Vui lòng chọn môn học cần xóa.", true); return; }
        Alert alert = new Alert(AlertType.CONFIRMATION); alert.setTitle("Xác nhận xóa"); alert.setHeaderText("Xóa môn học?"); alert.setContentText(selected.getMaMon() + " - " + selected.getTenMon());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (MonHocDAO.delete(selected.getMaMon())) { loadData(); showStatus("Xóa môn học thành công!", false); }
            else { showStatus("Lỗi: Không thể xóa môn học.", true); }
        }
     }
    @FXML private void timKiemMonHoc() { 
        String keyword = tfTimKiem.getText(); list = MonHocDAO.search(keyword); tableMonHoc.setItems(list);
        if (list.isEmpty()) showStatus("Không tìm thấy môn học nào.", false); else clearStatus();
     }

    private void showMonHocDetails(MonHoc monHoc) {
        clearStatus();
        if (monHoc != null) {
            tfMaMon.setText(monHoc.getMaMon());
            tfTenMon.setText(monHoc.getTenMon());
            tfSoTinChi.setText(String.valueOf(monHoc.getSoTinChi()));
            cbLoaiMon.setValue(monHoc.getLoaiMon());
            tfHocKy.setText(String.valueOf(monHoc.getHocKy()));
            tfMonTienQuyet.setText(monHoc.getMonTienQuyet() != null ? monHoc.getMonTienQuyet() : "");
            cbKhoa.setValue(monHoc.getKhoa()); // Set Khoa object in ComboBox
            tfMaMon.setEditable(false);
        } else {
            clearFields();
        }
    }

    @FXML
    private void clearFields() {
        tfMaMon.clear(); tfTenMon.clear(); tfSoTinChi.clear(); cbLoaiMon.setValue(null); tfHocKy.clear(); tfMonTienQuyet.clear(); cbKhoa.setValue(null); // Clear Khoa ComboBox
        tfTimKiem.clear();
        tfMaMon.setEditable(true);
        tableMonHoc.getSelectionModel().clearSelection();
        clearStatus();
    }

     private void showStatus(String message, boolean isError) {  lblStatus.setText(message); if (isError) { lblStatus.getStyleClass().remove("status-label-success"); lblStatus.getStyleClass().add("status-label-error"); } else { lblStatus.getStyleClass().remove("status-label-error"); lblStatus.getStyleClass().add("status-label-success"); } lblStatus.setVisible(true); lblStatus.setManaged(true); }
     private void clearStatus() {  lblStatus.setText(""); lblStatus.setVisible(false); lblStatus.setManaged(false); }
}