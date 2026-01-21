package controller;

import dao.KetQuaHocTapDAO;
import dao.LopHocPhanDAO;
import dao.KyHocDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.FloatStringConverter;
import model.KetQuaHocTap;
import model.KyHoc;
import model.LopHocPhan;
import util.ExcelUtil;
import javafx.stage.Stage;
import java.text.SimpleDateFormat;
import java.util.List;

public class DiemController extends BaseController {

    @FXML private TableView<KetQuaHocTap> tableDiem;
    @FXML private TableColumn<KetQuaHocTap, String> colMaSV;
    @FXML private TableColumn<KetQuaHocTap, String> colHoTen;
    @FXML private TableColumn<KetQuaHocTap, Float> colDiemQT;
    @FXML private TableColumn<KetQuaHocTap, Float> colDiemThi;
    @FXML private TableColumn<KetQuaHocTap, Float> colDiemTB;
    
    @FXML private Label lblLopInfo;
    @FXML private TextField tfDiemQT;
    @FXML private TextField tfDiemThi;
    @FXML private Button btnLuu;
    @FXML private Button btnChotDiem;
    @FXML private Label lblStatus;

    private LopHocPhan currentClass;
    private boolean isTimeLocked = false; 
    private boolean isFinalizedLocked = false; 

    // Phương thức khởi tạo dữ liệu cho màn hình nhập điểm
    public void initDataForGrading(LopHocPhan lhp) {
        this.currentClass = lhp;
        
        // Hiển thị tên lớp thông minh (Tên hành chính hoặc tên nguyện vọng)
        lblLopInfo.setText("Lớp: " + lhp.getTenHienThi() + " - Môn: " + lhp.getTenMon());
        
        checkDeadline(lhp.getHocKy(), lhp.getNamHoc());
        
        // Kiểm tra trạng thái khóa từ DB
        if (lhp.isDaKhoaDiem()) {
            isFinalizedLocked = true;
            showError(lblStatus, "Bảng điểm ĐÃ CHỐT. Chỉ được xem.");
            btnChotDiem.setDisable(true);
            btnChotDiem.setText("Đã Chốt");
        } else {
            isFinalizedLocked = false;
            btnChotDiem.setDisable(false);
        }

        updateEditingState();
        loadDiemSinhVien();
    }

    @FXML
    public void initialize() {
        tableDiem.setEditable(true);

        colMaSV.setCellValueFactory(new PropertyValueFactory<>("maSV"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTenSV"));
        
        colDiemQT.setCellValueFactory(new PropertyValueFactory<>("diemQT"));
        colDiemQT.setCellFactory(TextFieldTableCell.forTableColumn(new SafeFloatStringConverter()));
        colDiemQT.setOnEditCommit(e -> handleDirectEdit(e.getRowValue(), e.getNewValue(), true));

        colDiemThi.setCellValueFactory(new PropertyValueFactory<>("diemThi"));
        colDiemThi.setCellFactory(TextFieldTableCell.forTableColumn(new SafeFloatStringConverter()));
        colDiemThi.setOnEditCommit(e -> handleDirectEdit(e.getRowValue(), e.getNewValue(), false));

        colDiemTB.setCellValueFactory(new PropertyValueFactory<>("diemTB"));
        colDiemTB.setCellFactory(column -> new TableCell<KetQuaHocTap, Float>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f", item));
                    if (item < 4.0) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    else if (item >= 8.5) setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: black;");
                }
            }
        });

        tableDiem.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                tfDiemQT.setText(String.valueOf(newV.getDiemQT()));
                tfDiemThi.setText(String.valueOf(newV.getDiemThi()));
            }
        });
    }

    private void handleDirectEdit(KetQuaHocTap row, Float newVal, boolean isQT) {
        if (isTimeLocked || isFinalizedLocked) {
            showError(lblStatus, "Đã khóa điểm. Không thể sửa.");
            tableDiem.refresh();
            return;
        }
        if (newVal == null || newVal < 0 || newVal > 10) {
            showError(lblStatus, "Điểm không hợp lệ (0-10).");
            tableDiem.refresh();
            return;
        }

        if (isQT) row.setDiemQT(newVal); else row.setDiemThi(newVal);
        float tb = (row.getDiemQT() * 0.4f + row.getDiemThi() * 0.6f);
        row.setDiemTB((float) (Math.round(tb * 10.0) / 10.0));

        // Gọi DAO cập nhật (Dùng ID kết quả học tập)
        runTask(
            () -> KetQuaHocTapDAO.updateDiem(row.getId(), row.getDiemQT(), row.getDiemThi()),
            null,
            success -> {
                if (success) {
                    showSuccess(lblStatus, "Đã lưu.");
                    tableDiem.refresh();
                } else {
                    showError(lblStatus, "Lỗi lưu DB.");
                    loadDiemSinhVien();
                }
            },
            error -> showError(lblStatus, "Lỗi kết nối: " + error.getMessage())
        );
    }

    private void updateEditingState() {
        boolean locked = isTimeLocked || isFinalizedLocked;
        tableDiem.setEditable(!locked);
        if(tfDiemQT != null) tfDiemQT.setDisable(locked);
        if(tfDiemThi != null) tfDiemThi.setDisable(locked);
        if(btnLuu != null) btnLuu.setDisable(locked);
    }

    private void loadDiemSinhVien() {
        if (currentClass == null) return;
        // Gọi DAO mới với ID lớp học phần
        runTask(
            () -> KetQuaHocTapDAO.getListByLopHocPhan(currentClass.getId()),
            tableDiem,
            list -> tableDiem.setItems(list),
            error -> showError(lblStatus, "Lỗi tải danh sách: " + error.getMessage())
        );
    }
    
    private void checkDeadline(int hocKy, int namHoc) {
        KyHoc kyHoc = KyHocDAO.getKyHoc(hocKy, namHoc);
        if (kyHoc == null) { isTimeLocked = false; return; }
        if (!kyHoc.isTrongThoiGianNhapDiem()) {
            isTimeLocked = true;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            showError(lblStatus, "Hết hạn nhập điểm (" + sdf.format(kyHoc.getNgayKetThucNhapDiem()) + ")");
        } else {
            isTimeLocked = false;
        }
    }

    @FXML private void handleLuuDiem() { 
         KetQuaHocTap selected = tableDiem.getSelectionModel().getSelectedItem();
         if(selected != null) {
             try {
                 handleDirectEdit(selected, Float.parseFloat(tfDiemQT.getText()), true);
                 handleDirectEdit(selected, Float.parseFloat(tfDiemThi.getText()), false);
             } catch (NumberFormatException e) { showError(lblStatus, "Số không hợp lệ."); }
         }
    }

    @FXML private void handleChotDiem() { 
        if (isTimeLocked || isFinalizedLocked) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn chắc chắn muốn CHỐT điểm? Không thể sửa sau khi chốt.");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
             if (LopHocPhanDAO.lockGrade(currentClass.getId())) { // Gọi DAO mới
                 showSuccess(lblStatus, "Đã chốt điểm.");
                 isFinalizedLocked = true;
                 currentClass.setTrangThai("DA_KHOA_DIEM");
                 updateEditingState();
                 btnChotDiem.setText("Đã Chốt");
                 btnChotDiem.setDisable(true);
             }
        }
    }

    @FXML private void handleExportDiem() { 
         if (currentClass == null) return;
         Stage stage = (Stage) tableDiem.getScene().getWindow();
         ExcelUtil.exportBangDiemToExcel(tableDiem.getItems(), currentClass.getTenHienThi(), currentClass.getTenMon(), stage);
         showSuccess(lblStatus, "Xuất Excel thành công.");
    }

    @FXML private void handleImportDiem() { 
         if (isTimeLocked || isFinalizedLocked) { showError(lblStatus, "Đã khóa nhập điểm."); return; }
         Stage stage = (Stage) tableDiem.getScene().getWindow();
         List<KetQuaHocTap> importedList = ExcelUtil.importBangDiemFromExcel(stage);
         if (importedList.isEmpty()) return;

         int count = 0;
         for (KetQuaHocTap imported : importedList) {
             for (KetQuaHocTap current : tableDiem.getItems()) {
                 if (current.getMaSV().equalsIgnoreCase(imported.getMaSV())) {
                     float newQT = imported.getDiemQT() >= 0 ? imported.getDiemQT() : current.getDiemQT();
                     float newThi = imported.getDiemThi() >= 0 ? imported.getDiemThi() : current.getDiemThi();
                     if (KetQuaHocTapDAO.updateDiem(current.getId(), newQT, newThi)) { count++; }
                     break; 
                 }
             }
         }
         loadDiemSinhVien();
         showSuccess(lblStatus, "Đã nhập " + count + " sinh viên.");
    }

    public static class SafeFloatStringConverter extends FloatStringConverter {
        @Override public Float fromString(String value) {
            try { return super.fromString(value); } catch (NumberFormatException e) { return -1f; }
        }
    }
}