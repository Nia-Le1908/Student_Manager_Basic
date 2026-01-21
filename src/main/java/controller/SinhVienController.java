package controller;

import dao.KhoaDAO;
import dao.SinhVienDAO;
import dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane; // Import StackPane
import javafx.util.StringConverter;
import model.Khoa;
import model.SinhVien;
import model.User;
import util.PasswordUtil;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SinhVienController extends BaseController { 

    @FXML private TextField tfMaSV, tfHoTen, tfQueQuan, tfLop, tfTimKiem;
    @FXML private DatePicker dpNgaySinh;
    @FXML private ComboBox<String> cbGioiTinh;
    @FXML private ComboBox<Khoa> cbKhoa;
    @FXML private ComboBox<String> cbLopLoc; 
    @FXML private TableView<SinhVien> tableSV;
    @FXML private TableColumn<SinhVien, String> colMaSV, colHoTen, colGioiTinh, colQueQuan, colLop;
    @FXML private TableColumn<SinhVien, LocalDate> colNgaySinh;
    @FXML private TableColumn<SinhVien, Khoa> colKhoa;
    @FXML private TableColumn<SinhVien, Double> colDiemTichLuy;
    @FXML private Label lblStatus;
    
    // Nút chức năng
    @FXML private Button btnThem, btnSua, btnXoa, btnImportExcel;
    
    // Quản lý tài khoản
    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private Button btnCreateUpdateAccount;
    @FXML private Label lblAccountStatus, lblUsername, lblPassword;

    private ObservableList<SinhVien> dsSV;
    private ObservableList<Khoa> dsKhoa;
    private User loggedInUser;

    public void initializeData(User user) {
        this.loggedInUser = user;
        setupPermissionsAndLoadData();
    }

    @FXML
    public void initialize() {
        // Animation
        if (tableSV != null) playFadeInTransition(tableSV);

        cbGioiTinh.getItems().addAll("Nam", "Nữ");
        dsKhoa = KhoaDAO.getAllKhoa();
        cbKhoa.setItems(dsKhoa);
        configureKhoaComboBox();
        configureKhoaTableColumn();
        configureTableColumns();

        tableSV.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showSinhVienDetails(newValue));

        if (cbLopLoc != null) {
            // Logic lọc client-side cho Admin hoặc bỏ qua cho GV
            cbLopLoc.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> timSinhVien());
        }
        clearAccountFields(); 
    }

    private void setupPermissionsAndLoadData() {
        boolean isAdmin = loggedInUser != null && "admin".equals(loggedInUser.getRole());
        boolean isGiangVien = loggedInUser != null && "giangvien".equals(loggedInUser.getRole());

        // --- PHÂN QUYỀN GIAO DIỆN ---
        // Giảng viên CHỈ ĐƯỢC XEM, không được thêm/sửa/xóa hồ sơ sinh viên
        setNodeVisibility(btnThem, isAdmin);
        setNodeVisibility(btnSua, isAdmin);
        setNodeVisibility(btnXoa, isAdmin);
        setNodeVisibility(btnImportExcel, isAdmin);
        
        // Quản lý tài khoản user
        setNodeVisibility(tfUsername, isAdmin);
        setNodeVisibility(pfPassword, isAdmin);
        setNodeVisibility(btnCreateUpdateAccount, isAdmin);
        setNodeVisibility(lblUsername, isAdmin);
        setNodeVisibility(lblPassword, isAdmin);

        // Cấu hình bộ lọc
        if (isAdmin) {
            // Admin: Lọc theo lớp hành chính
            List<String> listLop = new ArrayList<>();
            listLop.add("Tất cả");
            listLop.addAll(SinhVienDAO.getAllLop());
            cbLopLoc.setItems(FXCollections.observableArrayList(listLop));
            cbLopLoc.getSelectionModel().selectFirst();
            cbLopLoc.setVisible(true); // Admin cần lọc lớp hành chính
        } else {
            // Giảng viên: Không cần lọc lớp hành chính vì đã query theo lớp học phần
            // Hoặc có thể ẩn luôn cbLopLoc để giao diện gọn hơn
            cbLopLoc.setVisible(false); 
            cbLopLoc.setManaged(false);
        }

        timSinhVien(); // Tải dữ liệu ban đầu
    }

    @FXML 
    private void timSinhVien() {
        String keyword = tfTimKiem.getText().trim();
        // Lấy giá trị của cbLopLoc trên luồng UI trước khi đưa vào task nền
        String selectedLop = (cbLopLoc != null) ? cbLopLoc.getValue() : null;
              
        Node rootNode = tableSV.getScene() != null ? tableSV.getScene().getRoot() : tableSV.getParent();
        StackPane overlayTarget = null;
        
        // Cố gắng tìm StackPane để hiện loading overlay
        if (rootNode instanceof StackPane) {
            overlayTarget = (StackPane) rootNode;
        } 
        
        if (overlayTarget != null) {
             // Sử dụng Loading Overlay từ BaseController
            runTaskWithOverlay(() -> {
                if (isGiangVien()) {
                    //  Lấy sinh viên thuộc các lớp học phần GV dạy
                    return SinhVienDAO.getStudentsTaughtBy(loggedInUser.getMaGV(), keyword);
                } else {
                    // Admin tìm kiếm toàn cục
                    if (selectedLop != null && !selectedLop.equals("Tất cả")) {
                        return SinhVienDAO.search(keyword, null).filtered(sv -> sv.getLop().equals(selectedLop));
                    } else {
                        return SinhVienDAO.search(keyword, null);
                    }
                }
            }, overlayTarget, // Root node (StackPane) để hiện loading
            (resultList) -> {
                // Chuyển List thành ObservableList để gán vào TableView
                dsSV = FXCollections.observableArrayList(resultList);
                tableSV.setItems(dsSV);
                
                if (dsSV.isEmpty()) {
                    if (isGiangVien()) {
                        showStatus("Bạn chưa có sinh viên nào trong các lớp học phần.", false);
                    } else {
                        showStatus("Không tìm thấy kết quả phù hợp.", false);
                    }
                    tableSV.setPlaceholder(new Label("Không có dữ liệu."));
                } else {
                    clearStatus();
                }
            }, 
            (error) -> showError("Lỗi tải dữ liệu: " + error.getMessage())
            );
        } else {
            // Fallback: Dùng runTask thường nếu không tìm thấy StackPane cho overlay
             runTask(() -> {
                if (isGiangVien()) {
                    return SinhVienDAO.getStudentsTaughtBy(loggedInUser.getMaGV(), keyword);
                } else {
                    if (selectedLop != null && !selectedLop.equals("Tất cả")) {
                        return SinhVienDAO.search(keyword, null).filtered(sv -> sv.getLop().equals(selectedLop));
                    } else {
                        return SinhVienDAO.search(keyword, null);
                    }
                }
            }, tableSV, // Cursor loading trên bảng
            (resultList) -> {
                dsSV = FXCollections.observableArrayList(resultList);
                tableSV.setItems(dsSV);
                if (dsSV.isEmpty()) {
                     if (isGiangVien()) showStatus("Bạn chưa có sinh viên nào.", false);
                     else showStatus("Không tìm thấy kết quả.", false);
                } else clearStatus();
            }, 
            (error) -> showError("Lỗi tải dữ liệu: " + error.getMessage())
            );
        }
    }


    private void setNodeVisibility(Node node, boolean visible) {
        if (node != null) { node.setVisible(visible); node.setManaged(visible); }
    }
    private void configureKhoaComboBox() {
         cbKhoa.setConverter(new StringConverter<Khoa>() {
            @Override public String toString(Khoa khoa) { return khoa == null ? null : khoa.getTenKhoa(); }
            @Override public Khoa fromString(String string) { return null; }
        });
    }
    private void configureKhoaTableColumn() {
        colKhoa.setCellFactory(column -> new TableCell<SinhVien, Khoa>() {
            @Override protected void updateItem(Khoa item, boolean empty) {
                super.updateItem(item, empty);
                setText( (item == null || empty) ? null : item.getTenKhoa() );
            }
        });
     }
    private void configureTableColumns(){
         colMaSV.setCellValueFactory(data -> data.getValue().maSVProperty());
        colHoTen.setCellValueFactory(data -> data.getValue().hoTenProperty());
        colNgaySinh.setCellValueFactory(data -> data.getValue().ngaySinhProperty());
        colGioiTinh.setCellValueFactory(data -> data.getValue().gioiTinhProperty());
        colQueQuan.setCellValueFactory(data -> data.getValue().queQuanProperty());
        colKhoa.setCellValueFactory(data -> data.getValue().khoaProperty());
        colLop.setCellValueFactory(data -> data.getValue().lopProperty());
        colDiemTichLuy.setCellValueFactory(data -> data.getValue().diemTichLuyProperty().asObject());
    }

    @FXML private void themSinhVien() {
         if (!isAdmin()) return; 
         if (tfMaSV.getText().isEmpty() || tfHoTen.getText().isEmpty() || cbKhoa.getValue() == null) { showError("Mã SV, Họ tên và Khoa là bắt buộc."); return; }
        if (!tfMaSV.getText().matches("^SV\\d+$")) { showError("Mã SV không hợp lệ (ví dụ: SV001)."); return; }
        SinhVien sv = new SinhVien(tfMaSV.getText(), tfHoTen.getText(), dpNgaySinh.getValue(), cbGioiTinh.getValue(), tfQueQuan.getText(), cbKhoa.getValue(), tfLop.getText());
        if (SinhVienDAO.insert(sv)) { timSinhVien(); showSuccess("Thêm sinh viên thành công."); }
        else { showError("Thêm sinh viên thất bại (Mã SV có thể đã tồn tại)."); }
    }
    @FXML private void suaSinhVien() {
        if (!isAdmin()) return;
        SinhVien selectedSV = getSelectedStudentWithErrorCheck();
        if (selectedSV == null) return;
        if (tfHoTen.getText().isEmpty() || cbKhoa.getValue() == null) { showError("Họ tên và Khoa là bắt buộc."); return; }
        selectedSV.setHoTen(tfHoTen.getText()); selectedSV.setNgaySinh(dpNgaySinh.getValue()); selectedSV.setGioiTinh(cbGioiTinh.getValue()); selectedSV.setQueQuan(tfQueQuan.getText()); selectedSV.setKhoa(cbKhoa.getValue()); selectedSV.setLop(tfLop.getText());
        if (SinhVienDAO.update(selectedSV)) { timSinhVien(); showSuccess("Cập nhật sinh viên thành công."); }
        else { showError("Cập nhật sinh viên thất bại."); }
     }
    @FXML private void xoaSinhVien() {
        if (!isAdmin()) return;
        SinhVien selectedSV = getSelectedStudentWithErrorCheck();
        if (selectedSV == null) return;
        Alert alert = new Alert(AlertType.CONFIRMATION); alert.setTitle("Xác nhận xóa"); alert.setHeaderText("Xóa sinh viên?"); alert.setContentText(selectedSV.getMaSV() + " - " + selectedSV.getHoTen());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (SinhVienDAO.delete(selectedSV.getMaSV())) { timSinhVien(); showSuccess("Xóa sinh viên thành công."); }
            else { showError("Xóa sinh viên thất bại."); }
        }
    }
    
    @FXML private void handleCreateUpdateAccount() {
         if (!isAdmin()) { showAccountStatus("Chỉ Admin mới có quyền quản lý tài khoản.", true); return; }
        SinhVien selectedSV = getSelectedStudentWithErrorCheck();
        if (selectedSV == null) return;
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText();
        if (username.isEmpty()) { showAccountStatus("Lỗi: Tên đăng nhập không được để trống.", true); return; }
        User existingUserByMaSV = UserDAO.getUserByMaSV(selectedSV.getMaSV());
        User existingUserByUsername = UserDAO.getUserByUsername(username);

        if (existingUserByMaSV != null) { 
             if (!existingUserByMaSV.getUsername().equals(username)) { showAccountStatus("Lỗi: Không thể thay đổi tên đăng nhập.", true); return; }
             if (password.isEmpty()) { showAccountStatus("Lỗi: Vui lòng nhập mật khẩu mới để reset.", true); return; }
            Alert alert = new Alert(AlertType.CONFIRMATION); alert.setTitle("Xác nhận Reset Mật khẩu"); alert.setHeaderText("Reset mật khẩu cho tài khoản '" + username + "'?"); alert.setContentText("Mật khẩu sẽ được cập nhật.");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String hashedPassword = PasswordUtil.hashPassword(password);
                if (UserDAO.updatePassword(username, hashedPassword)) { showAccountStatus("Reset mật khẩu thành công!", false); pfPassword.clear(); }
                else { showAccountStatus("Lỗi: Reset mật khẩu thất bại.", true); }
            }
        } else { 
             if (password.isEmpty()) { showAccountStatus("Lỗi: Mật khẩu không được trống khi tạo TK.", true); return; }
            if (existingUserByUsername != null) { showAccountStatus("Lỗi: Tên đăng nhập '" + username + "' đã được sử dụng.", true); return; }
            String hashedPassword = PasswordUtil.hashPassword(password);
            if (UserDAO.createStudentAccount(username, hashedPassword, selectedSV.getMaSV())) {
                showAccountStatus("Tạo tài khoản thành công!", false); tfUsername.setEditable(false); pfPassword.clear(); pfPassword.setPromptText("Nhập MK mới để Reset"); btnCreateUpdateAccount.setText("Reset Mật khẩu");
            } else { showAccountStatus("Lỗi: Tạo tài khoản thất bại.", true); }
        }
    }

    @FXML private void handleExportExcel() {
        ObservableList<SinhVien> currentList = tableSV.getItems();
        if (currentList == null || currentList.isEmpty()) { showStatus("Không có dữ liệu để xuất.", true); return; }
        Stage stage = (Stage) tableSV.getScene().getWindow();
        util.ExcelUtil.exportSinhVienToExcel(currentList, stage);
        showSuccess("Xuất danh sách sinh viên ra Excel thành công.");
    }

    @FXML private void handleImportExcel() {
        if (!isAdmin()) { showError("Chỉ Quản trị viên mới có quyền nhập dữ liệu."); return; }
        Stage stage = (Stage) tableSV.getScene().getWindow();
        List<SinhVien> listFromFile = util.ExcelUtil.importSinhVienFromExcel(stage);
        if (listFromFile.isEmpty()) return;
        int countSuccess = 0, countFail = 0;
        for (SinhVien sv : listFromFile) {
            if (SinhVienDAO.insert(sv)) countSuccess++; else countFail++;
        }
        timSinhVien(); 
        if (countFail > 0) showStatus("Đã nhập " + countSuccess + " SV. Có " + countFail + " SV bị trùng hoặc lỗi.", true);
        else showSuccess("Nhập thành công " + countSuccess + " sinh viên từ Excel.");
    }

    private void showSinhVienDetails(SinhVien sv) {
        clearStatus(); clearAccountFields();
        if (sv != null) {
            tfMaSV.setText(sv.getMaSV()); tfHoTen.setText(sv.getHoTen()); dpNgaySinh.setValue(sv.getNgaySinh()); cbGioiTinh.setValue(sv.getGioiTinh()); tfQueQuan.setText(sv.getQueQuan()); cbKhoa.setValue(sv.getKhoa()); tfLop.setText(sv.getLop()); tfMaSV.setEditable(false);
            
            // Chỉ Admin mới xem được thông tin tài khoản
            if (isAdmin()) {
                tfUsername.setDisable(false); pfPassword.setDisable(false); btnCreateUpdateAccount.setDisable(false);
                User existingUser = UserDAO.getUserByMaSV(sv.getMaSV());
                if (existingUser != null) {
                    tfUsername.setText(existingUser.getUsername()); tfUsername.setEditable(false); pfPassword.setPromptText("Nhập MK mới để Reset"); btnCreateUpdateAccount.setText("Reset Mật khẩu"); showAccountStatus("Tài khoản đã tồn tại.", false);
                } else {
                    tfUsername.clear(); tfUsername.setEditable(true); pfPassword.setPromptText("Nhập mật khẩu"); btnCreateUpdateAccount.setText("Tạo Tài khoản"); showAccountStatus("Sinh viên chưa có tài khoản.", false);
                }
                lblAccountStatus.setVisible(true); lblAccountStatus.setManaged(true);
            } else { 
                clearAccountFields(); 
            }
        } else { clearForm(); }
    }

    @FXML private void clearForm() {
        tfMaSV.clear(); tfHoTen.clear(); dpNgaySinh.setValue(null); cbGioiTinh.setValue(null); tfQueQuan.clear(); cbKhoa.setValue(null); tfLop.clear(); tfTimKiem.clear(); tfMaSV.setEditable(true); tableSV.getSelectionModel().clearSelection(); clearStatus(); clearAccountFields();
    }
     private void clearAccountFields() {
         if(tfUsername != null) { tfUsername.clear(); tfUsername.setDisable(true); tfUsername.setEditable(true); }
         if(pfPassword != null) { pfPassword.clear(); pfPassword.setDisable(true); pfPassword.setPromptText("");}
         if(btnCreateUpdateAccount != null) btnCreateUpdateAccount.setDisable(true);
         if(lblAccountStatus != null) { lblAccountStatus.setText(""); lblAccountStatus.setVisible(false); lblAccountStatus.setManaged(false);}
     }

    private SinhVien getSelectedStudentWithErrorCheck() { SinhVien selected = tableSV.getSelectionModel().getSelectedItem(); if (selected == null) { showError("Vui lòng chọn một sinh viên."); } return selected; }
    private boolean isAdmin() { return loggedInUser != null && "admin".equals(loggedInUser.getRole()); }
    private boolean isGiangVien() { return loggedInUser != null && "giangvien".equals(loggedInUser.getRole()); }

    // Override BaseController methods (giữ nguyên)
    @Override protected void showStatus(Label label, String message, boolean isError) { super.showStatus(this.lblStatus, message, isError); }
    @Override protected void showSuccess(Label label, String message) { super.showSuccess(this.lblStatus, message); }
    @Override protected void showError(Label label, String message) { super.showError(this.lblStatus, message); }
    @Override protected void clearStatus(Label label) { super.clearStatus(this.lblStatus); }
    private void showAccountStatus(String message, boolean isError) { if (lblAccountStatus != null) { lblAccountStatus.setText(message); lblAccountStatus.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;"); lblAccountStatus.setVisible(true); lblAccountStatus.setManaged(true); } }
    public void showSuccess(String message) { showSuccess(lblStatus, message); }
    public void showError(String message) { showError(lblStatus, message); }
    public void clearStatus() { clearStatus(lblStatus); }
    private void showStatus(String message, boolean isError) { showStatus(this.lblStatus, message, isError); }
}