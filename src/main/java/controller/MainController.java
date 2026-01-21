package controller;

import dao.GiangVienDAO;
import dao.LopHocPhanDAO;
import dao.SinhVienDAO;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.GiangVien;
import model.LopHocPhan;
import model.SinhVien;
import model.User;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class MainController extends BaseController {

    @FXML private StackPane rootPane;
    @FXML private AnchorPane sidebarPane;
    @FXML private VBox sidebarVBox; 
    @FXML private Button btnMenu;

    @FXML private Label lblWelcome;
    @FXML private Label lblAdminTitle;
    
    @FXML private Button btnQuanLyKhoa;
    @FXML private Button btnQuanLyGiangVien;
    @FXML private Button btnQuanLyMonHoc;
    @FXML private Button btnPhanCongLop;
    @FXML private Button btnDuyetLopNguyenVong;
    @FXML private Button btnQuanLyKyHoc;
    @FXML private Button btnQuanLySinhVien;
    @FXML private Button btnThongKe;
    @FXML private Button btnQuanLyLopHC; 

    @FXML private VBox adminSection;
    @FXML private Label lblPendingWishlists;
    @FXML private Label lblTotalStudents;
    @FXML private Label lblTotalLecturers;
    @FXML private PieChart adminPieChart;

    @FXML private VBox giangVienSection;
    @FXML private Button btnTaoLopNguyenVong;
    @FXML private TableView<LopHocPhan> tablePhanCong;
    @FXML private TableColumn<LopHocPhan, String> colMaMon;
    @FXML private TableColumn<LopHocPhan, String> colTenMon;
    @FXML private TableColumn<LopHocPhan, String> colMaLop;
    @FXML private TableColumn<LopHocPhan, Integer> colHocKy;
    @FXML private TableColumn<LopHocPhan, Integer> colNamHoc;
    @FXML private TableColumn<LopHocPhan, String> colTrangThai;

    private User loggedInUser;
    private GiangVien loggedInGiangVienDetails;
    private boolean isAdmin = false;
    private boolean isGiangVien = false;
    
    private boolean isSidebarOpen = false;
    private final double SIDEBAR_WIDTH = 260.0;

    public void initializeData(User user) {
        this.loggedInUser = user;
        
        if (rootPane != null) {
            playFadeInTransition(rootPane);
        }

        if (loggedInUser == null) return;
        
        if ("giangvien".equals(loggedInUser.getRole()) && loggedInUser.getMaGV() != null) {
            this.loggedInGiangVienDetails = GiangVienDAO.getGiangVienByMaGV(loggedInUser.getMaGV());
        }
        
        String welcomeMsg = "Xin chào, ";
        if ("admin".equals(loggedInUser.getRole())) welcomeMsg += "Quản trị viên";
        else if (loggedInGiangVienDetails != null) welcomeMsg += "GV. " + loggedInGiangVienDetails.getHoTen();
        else welcomeMsg += loggedInUser.getUsername();
        lblWelcome.setText(welcomeMsg);
        
        setupPermissions();
        
        if (sidebarPane != null) {
            sidebarPane.setTranslateX(-SIDEBAR_WIDTH);
            isSidebarOpen = false;
        }
    }

    @FXML
    private void toggleSidebar() {
        if (!isSidebarOpen) {
            if (sidebarVBox != null) {
                for (Node node : sidebarVBox.getChildren()) {
                    if (node.isVisible()) { 
                        node.setOpacity(0);
                        node.setTranslateX(-20);
                    }
                }
            }

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), sidebarPane);
            slideIn.setToX(0);
            
            slideIn.setOnFinished(e -> {
                if (sidebarVBox != null) {
                    int delay = 0;
                    for (Node node : sidebarVBox.getChildren()) {
                        if (!node.isVisible() || !node.isManaged()) continue;

                        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
                        ft.setToValue(1);
                        TranslateTransition tt = new TranslateTransition(Duration.millis(300), node);
                        tt.setToX(0);
                        
                        ParallelTransition pt = new ParallelTransition(ft, tt);
                        pt.setDelay(Duration.millis(delay)); 
                        pt.play();
                        delay += 50; 
                    }
                }
            });
            slideIn.play();
            isSidebarOpen = true;
        } else {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), sidebarPane);
            slideOut.setToX(-SIDEBAR_WIDTH);
            slideOut.play();
            isSidebarOpen = false;
        }
    }

    private void setupPermissions() {
        isAdmin = loggedInUser != null && "admin".equals(loggedInUser.getRole());
        isGiangVien = loggedInUser != null && "giangvien".equals(loggedInUser.getRole());

        setNodeVisibility(lblAdminTitle, isAdmin);
        setNodeVisibility(btnQuanLyKhoa, isAdmin);
        setNodeVisibility(btnQuanLyGiangVien, isAdmin);
        setNodeVisibility(btnQuanLyMonHoc, isAdmin);
        setNodeVisibility(btnPhanCongLop, isAdmin);
        setNodeVisibility(btnDuyetLopNguyenVong, isAdmin);
        setNodeVisibility(btnQuanLyKyHoc, isAdmin);
        setNodeVisibility(btnQuanLyLopHC, isAdmin);
        
        setNodeVisibility(btnQuanLySinhVien, isAdmin || isGiangVien);
        setNodeVisibility(btnThongKe, isAdmin || isGiangVien);
        setNodeVisibility(giangVienSection, isGiangVien);
        setNodeVisibility(adminSection, isAdmin);

        if (isGiangVien) {
            initializeGiangVienUI();
            loadPhanCongData();
        } else if (isAdmin) {
            loadAdminDashboardData();
        }
    }
    
    private void setNodeVisibility(Control node, boolean visible) {
        if (node != null) { node.setVisible(visible); node.setManaged(visible); }
    }
    private void setNodeVisibility(VBox node, boolean visible) {
        if (node != null) { node.setVisible(visible); node.setManaged(visible); }
    }

    private void initializeGiangVienUI() {
        if(tablePhanCong != null) {
            colMaMon.setCellValueFactory(new PropertyValueFactory<>("maMon"));
            colTenMon.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
            colMaLop.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getTenHienThi()));
            colHocKy.setCellValueFactory(new PropertyValueFactory<>("hocKy"));
            colNamHoc.setCellValueFactory(new PropertyValueFactory<>("namHoc"));
            colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
            
            tablePhanCong.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    LopHocPhan selected = tablePhanCong.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        openGradeWindowForClass(selected);
                    }
                }
            });
        }
    }

    private void loadPhanCongData() {
        if (isGiangVien && tablePhanCong != null) {
            ObservableList<LopHocPhan> list = LopHocPhanDAO.getLopHocPhan(loggedInUser.getMaGV());
            tablePhanCong.setItems(list);
            if (list.isEmpty()) {
                tablePhanCong.setPlaceholder(new Label("Bạn chưa được phân công lớp nào."));
            }
        }
    }

    private void loadAdminDashboardData() {
        try {
            int pending = LopHocPhanDAO.getPendingRequests().size();
            ObservableList<SinhVien> allStudents = SinhVienDAO.getAllSinhVien();
            
            if (lblPendingWishlists != null) lblPendingWishlists.setText(String.valueOf(pending));
            if (lblTotalStudents != null) lblTotalStudents.setText(String.valueOf(allStudents.size()));
            if (lblTotalLecturers != null) lblTotalLecturers.setText(String.valueOf(GiangVienDAO.getAllGiangVien().size()));

            if (adminPieChart != null) {
                Map<String, Long> studentByFaculty = allStudents.stream()
                    .collect(Collectors.groupingBy(
                        sv -> (sv.getKhoa() != null ? sv.getKhoa().getTenKhoa() : "Chưa phân khoa"),
                        Collectors.counting()
                    ));
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
                studentByFaculty.forEach((k, v) -> pieChartData.add(new PieChart.Data(k, v)));
                adminPieChart.setData(pieChartData);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void refreshGiangVienData(ActionEvent event) { loadPhanCongData(); }

    @FXML private void moQuanLySinhVien(ActionEvent event) { openWindow("/view/SinhVienView.fxml", "Quản lý Sinh viên", true); }
    @FXML private void moThongKe(ActionEvent event) { openWindow("/view/ThongKeView.fxml", "Thống kê", false); }
    @FXML private void moQuanLyKhoa(ActionEvent event) { openWindow("/view/KhoaView.fxml", "Quản lý Khoa", false); }
    @FXML private void moQuanLyGiangVien(ActionEvent event) { openWindow("/view/GiangVienView.fxml", "Quản lý Giảng viên", false); }
    @FXML private void moQuanLyMonHoc(ActionEvent event) { openWindow("/view/MonHocView.fxml", "Quản lý Môn học", false); }
    @FXML private void moPhanCongLop(ActionEvent event) { openWindow("/view/PhanCongLopView.fxml", "Phân công Lớp", false); }
    @FXML private void moTaoLopNguyenVong(ActionEvent event) { openWindow("/view/TaoLopNguyenVongView.fxml", "Tạo Lớp NV", true); }
    @FXML private void moDuyetLopNguyenVong(ActionEvent event) { openWindow("/view/DuyetLopNguyenVongView.fxml", "Duyệt Lớp NV", true); }
    @FXML private void moQuanLyKyHoc(ActionEvent event) { openWindow("/view/QuanLyKyHocView.fxml", "Quản lý Kỳ Học", false); }
    @FXML private void moQuanLyLopHC(ActionEvent event) { openWindow("/view/LopHanhChinhView.fxml", "Quản lý Lớp Hành Chính", false); }

    @FXML private void thoatChuongTrinh(ActionEvent event) {
        Stage stage = (Stage) lblWelcome.getScene().getWindow();
        stage.close();
        new LoginController().openLoginWindowAgain();
    }

    private void openWindow(String fxmlFile, String title, boolean passUserInfo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            if (passUserInfo && loggedInUser != null) {
                Object controller = loader.getController();
                if (controller instanceof SinhVienController) ((SinhVienController) controller).initializeData(loggedInUser);
                else if (controller instanceof TaoLopNguyenVongController) ((TaoLopNguyenVongController) controller).initializeData(loggedInUser);
                else if (controller instanceof DuyetLopNguyenVongController) ((DuyetLopNguyenVongController) controller).initializeData(loggedInUser.getUsername());
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            
            // --- ANIMATION POP-UP ---
            playPopUpAnimation(stage);
            
            stage.showAndWait();
            
            if (isGiangVien) loadPhanCongData();
            if (isAdmin) loadAdminDashboardData();
            
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    private void openGradeWindowForClass(LopHocPhan lhp) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DiemView.fxml"));
            Parent root = loader.load();
            DiemController diemController = loader.getController();
            diemController.initDataForGrading(lhp);

            Stage stage = new Stage();
            stage.setTitle("Nhập điểm: " + lhp.getTenMon());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            
            // --- ANIMATION POP-UP ---
            playPopUpAnimation(stage);

            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }
}