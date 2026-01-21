package controller;

import dao.GiangVienDAO;
import dao.KhoaDAO;
import dao.MonHocDAO;
import dao.UserDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.util.StringConverter;
import model.GiangVien;
import model.Khoa;
import model.MonHoc;
import model.User;
import util.PasswordUtil;

import java.util.Optional;

public class GiangVienController {

    @FXML private TableView<GiangVien> tableGV;
    @FXML private TableColumn<GiangVien, String> colMaGV;
    @FXML private TableColumn<GiangVien, String> colHoTen;
    @FXML private TableColumn<GiangVien, String> colEmail;
    @FXML private TableColumn<GiangVien, Khoa> colKhoa;
    @FXML private TextField tfMaGV;
    @FXML private TextField tfHoTen;
    @FXML private TextField tfEmail;
    @FXML private ComboBox<Khoa> cbKhoa;
    @FXML private TextField tfTimKiem;
    @FXML private Label lblStatus;
    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private Button btnCreateUpdateAccount;
    @FXML private Label lblAccountStatus;

 
    @FXML private ComboBox<MonHoc> cbMonHocTheoKhoa;


    private ObservableList<GiangVien> dsGV;
    private ObservableList<Khoa> dsKhoa;

    @FXML
    public void initialize() {
        System.out.println("✅ GiangVienController initialized");

        dsKhoa = KhoaDAO.getAllKhoa();
        cbKhoa.setItems(dsKhoa);
        configureKhoaComboBox();

        colMaGV.setCellValueFactory(data -> data.getValue().maGVProperty());
        colHoTen.setCellValueFactory(data -> data.getValue().hoTenProperty());
        colEmail.setCellValueFactory(data -> data.getValue().emailProperty());
        colKhoa.setCellValueFactory(data -> data.getValue().khoaProperty());
        configureKhoaTableColumn();

        tableGV.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showGiangVienDetails(newValue));

        configureMonHocComboBox();


        loadData();
        clearAccountFields();
    }

    private void configureMonHocComboBox() {
        if (cbMonHocTheoKhoa != null) {
            cbMonHocTheoKhoa.setConverter(new StringConverter<MonHoc>() {
                @Override public String toString(MonHoc mh) { return mh == null ? null : mh.toString(); }
                @Override public MonHoc fromString(String s) { return null; }
            });
             cbMonHocTheoKhoa.setDisable(true); 
        }
    }



    private void loadData() { 
        dsGV = GiangVienDAO.getAllGiangVien();
        tableGV.setItems(dsGV);
        clearFields();
     }
    private void configureKhoaComboBox() { 
        cbKhoa.setConverter(new StringConverter<Khoa>() {
            @Override public String toString(Khoa khoa) { return khoa == null ? null : khoa.getTenKhoa(); }
            @Override public Khoa fromString(String string) { return null; }
        });
     }
    private void configureKhoaTableColumn() { 
        colKhoa.setCellFactory(column -> new TableCell<GiangVien, Khoa>() {
            @Override protected void updateItem(Khoa item, boolean empty) {
                super.updateItem(item, empty);
                setText((item == null || empty) ? null : item.getTenKhoa());
            }
        });
     }
    @FXML private void themGiangVien() { 
         if (tfMaGV.getText().isEmpty() || tfHoTen.getText().isEmpty() || cbKhoa.getValue() == null) { showStatus("Lỗi: Mã GV, Họ tên và Khoa là bắt buộc.", true); return; }
        if (!tfMaGV.getText().matches("^GV\\d+$")) { showStatus("Lỗi: Mã GV không hợp lệ (ví dụ: GV001).", true); return; }
        String email = tfEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) { showStatus("Lỗi: Định dạng email không hợp lệ.", true); return; }
        GiangVien gv = new GiangVien(tfMaGV.getText().trim().toUpperCase(), tfHoTen.getText().trim(), email.isEmpty() ? null : email, cbKhoa.getValue());
        if (GiangVienDAO.insert(gv)) { loadData(); showStatus("Thêm giảng viên thành công!", false); }
        else { showStatus("Lỗi: Thêm giảng viên thất bại.", true); }
    }
    @FXML private void suaGiangVien() { 
         GiangVien selected = tableGV.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Lỗi: Vui lòng chọn giảng viên cần sửa.", true); return; }
        if (tfHoTen.getText().isEmpty() || cbKhoa.getValue() == null) { showStatus("Lỗi: Họ tên và Khoa là bắt buộc.", true); return; }
         String email = tfEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) { showStatus("Lỗi: Định dạng email không hợp lệ.", true); return; }
        selected.setHoTen(tfHoTen.getText().trim()); selected.setEmail(email.isEmpty() ? null : email); selected.setKhoa(cbKhoa.getValue());
        if (GiangVienDAO.update(selected)) { tableGV.refresh(); clearFields(); showStatus("Cập nhật giảng viên thành công!", false); }
        else { showStatus("Lỗi: Cập nhật giảng viên thất bại.", true); }
    }
    @FXML private void xoaGiangVien() { 
         GiangVien selected = tableGV.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Lỗi: Vui lòng chọn giảng viên cần xóa.", true); return; }
        Alert alert = new Alert(AlertType.CONFIRMATION); alert.setTitle("Xác nhận xóa"); alert.setHeaderText("Xóa giảng viên?"); alert.setContentText(selected.getMaGV() + " - " + selected.getHoTen());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (GiangVienDAO.delete(selected.getMaGV())) { loadData(); showStatus("Xóa giảng viên thành công!", false); }
            else { showStatus("Lỗi: Không thể xóa giảng viên.", true); }
        }
    }
    @FXML private void timKiemGiangVien() { 
        String keyword = tfTimKiem.getText(); dsGV = GiangVienDAO.search(keyword); tableGV.setItems(dsGV);
        if (dsGV.isEmpty()) showStatus("Không tìm thấy giảng viên nào.", false); else clearStatus();
     }

    private void showGiangVienDetails(GiangVien gv) {
        clearStatus();
        clearAccountFields();

         if (cbMonHocTheoKhoa != null) {
            cbMonHocTheoKhoa.getItems().clear();
            cbMonHocTheoKhoa.setDisable(true);
        }


        if (gv != null) {
            tfMaGV.setText(gv.getMaGV());
            tfHoTen.setText(gv.getHoTen());
            tfEmail.setText(gv.getEmail() != null ? gv.getEmail() : "");
            cbKhoa.setValue(gv.getKhoa());
            tfMaGV.setEditable(false);

            if (gv.getKhoa() != null && cbMonHocTheoKhoa != null) {
                ObservableList<MonHoc> monHocCuaKhoa = MonHocDAO.getMonHocByKhoa(gv.getKhoa().getMaKhoa());
                cbMonHocTheoKhoa.setItems(monHocCuaKhoa);
                cbMonHocTheoKhoa.setDisable(monHocCuaKhoa.isEmpty()); // Enable nếu có môn học
                 cbMonHocTheoKhoa.setPromptText(monHocCuaKhoa.isEmpty() ? "(Khoa này chưa có môn học)" : "Chọn môn học để phân công...");
            } else if (cbMonHocTheoKhoa != null) {
                 cbMonHocTheoKhoa.setPromptText("(GV chưa thuộc Khoa nào)");
            }



            tfUsername.setDisable(false); pfPassword.setDisable(false); btnCreateUpdateAccount.setDisable(false);
            User existingUser = UserDAO.getUserByMaGV(gv.getMaGV());
            if (existingUser != null) {
                tfUsername.setText(existingUser.getUsername()); tfUsername.setEditable(false);
                pfPassword.setPromptText("Nhập MK mới để Reset"); btnCreateUpdateAccount.setText("Reset Mật khẩu");
                lblAccountStatus.setText("Tài khoản đã tồn tại."); lblAccountStatus.setStyle("-fx-text-fill: green;");
            } else {
                tfUsername.clear(); tfUsername.setEditable(true);
                pfPassword.setPromptText("Nhập mật khẩu"); btnCreateUpdateAccount.setText("Tạo Tài khoản");
                lblAccountStatus.setText("Giảng viên chưa có tài khoản."); lblAccountStatus.setStyle("-fx-text-fill: orange;");
            }
             lblAccountStatus.setVisible(true); lblAccountStatus.setManaged(true);

        } else {
            clearFields();
        }
    }

    @FXML private void clearFields() { 
        tfMaGV.clear(); tfHoTen.clear(); tfEmail.clear(); cbKhoa.setValue(null); tfTimKiem.clear(); tfMaGV.setEditable(true); tableGV.getSelectionModel().clearSelection(); clearStatus(); clearAccountFields();

         if(cbMonHocTheoKhoa != null) { cbMonHocTheoKhoa.getItems().clear(); cbMonHocTheoKhoa.setDisable(true); cbMonHocTheoKhoa.setPromptText(""); }

     }
     private void clearAccountFields() { 
         if(tfUsername != null) { tfUsername.clear(); tfUsername.setDisable(true); tfUsername.setEditable(true); }
         if(pfPassword != null) { pfPassword.clear(); pfPassword.setDisable(true); pfPassword.setPromptText("");}
         if(btnCreateUpdateAccount != null) btnCreateUpdateAccount.setDisable(true);
         if(lblAccountStatus != null) { lblAccountStatus.setText(""); lblAccountStatus.setVisible(false); lblAccountStatus.setManaged(false);}
     }
    @FXML private void handleCreateUpdateAccount() { 
         GiangVien selectedGV = tableGV.getSelectionModel().getSelectedItem(); if (selectedGV == null) { showAccountStatus("Lỗi: Vui lòng chọn giảng viên.", true); return; }
        String username = tfUsername.getText().trim(); String password = pfPassword.getText();
        if (username.isEmpty()) { showAccountStatus("Lỗi: Tên đăng nhập không được để trống.", true); return; }
        User existingUserByMaGV = UserDAO.getUserByMaGV(selectedGV.getMaGV()); User existingUserByUsername = UserDAO.getUserByUsername(username);
        if (existingUserByMaGV != null) { // Reset
             if (!existingUserByMaGV.getUsername().equals(username)) { showAccountStatus("Lỗi: Không thể thay đổi tên đăng nhập.", true); return; }
             if (password.isEmpty()) { showAccountStatus("Lỗi: Vui lòng nhập mật khẩu mới để reset.", true); return; }
            Alert alert = new Alert(AlertType.CONFIRMATION); alert.setTitle("Xác nhận Reset Mật khẩu"); alert.setHeaderText("Reset mật khẩu cho tài khoản '" + username + "'?"); alert.setContentText("Mật khẩu sẽ được cập nhật.");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String hashedPassword = PasswordUtil.hashPassword(password);
                if (UserDAO.updatePassword(username, hashedPassword)) { showAccountStatus("Reset mật khẩu thành công!", false); pfPassword.clear(); }
                else { showAccountStatus("Lỗi: Reset mật khẩu thất bại.", true); }
            }
        } else { // Create
             if (password.isEmpty()) { showAccountStatus("Lỗi: Mật khẩu không được trống khi tạo TK.", true); return; }
            if (existingUserByUsername != null) { showAccountStatus("Lỗi: Tên đăng nhập '" + username + "' đã được sử dụng.", true); return; }
            String hashedPassword = PasswordUtil.hashPassword(password);
            if (UserDAO.createLecturerAccount(username, hashedPassword, selectedGV.getMaGV())) {
                showAccountStatus("Tạo tài khoản thành công!", false); tfUsername.setEditable(false); pfPassword.clear(); pfPassword.setPromptText("Nhập MK mới để Reset"); btnCreateUpdateAccount.setText("Reset Mật khẩu");
            } else { showAccountStatus("Lỗi: Tạo tài khoản thất bại.", true); }
        }
    }

    private void showStatus(String message, boolean isError) { /* ... */ if (lblStatus != null) { lblStatus.setText(message); lblStatus.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;"); lblStatus.setVisible(true); lblStatus.setManaged(true); } }
    private void clearStatus() { /* ... */ if (lblStatus != null) { lblStatus.setText(""); lblStatus.setVisible(false); lblStatus.setManaged(false); } }
    private void showAccountStatus(String message, boolean isError) { /* ... */ if (lblAccountStatus != null) { lblAccountStatus.setText(message); lblAccountStatus.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;"); lblAccountStatus.setVisible(true); lblAccountStatus.setManaged(true); } }
}