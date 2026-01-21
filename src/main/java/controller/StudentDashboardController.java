package controller;

import dao.SinhVienDAO;
import dao.ThongKeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane; 
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DiemChiTiet;
import model.MonHoc;
import model.SinhVien;
import model.User;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StudentDashboardController extends BaseController {

    @FXML private BorderPane rootPane; 

    @FXML private ImageView imgAvatar;
    @FXML private Label lblMaSV;
    @FXML private Label lblHoTen;
    @FXML private Label lblGioiTinh;
    @FXML private Label lblNgaySinh;
    @FXML private Label lblLop;
    @FXML private Label lblKhoa;
    @FXML private Label lblNoiSinh;

    @FXML private Button btnKetQuaHocTap;
    @FXML private Button btnDangKyHocPhan;
    @FXML private Button btnDangKyLopNV;

    @FXML private ComboBox<String> cbSemesterChart;
    @FXML private BarChart<String, Number> barChartGrades;
    @FXML private CategoryAxis xAxisMonHoc;
    @FXML private NumberAxis yAxisDiemTB;

    @FXML private ComboBox<String> cbSemesterTable;
    @FXML private TableView<MonHoc> tableCourses;
    @FXML private TableColumn<MonHoc, String> colMaMon;
    @FXML private TableColumn<MonHoc, String> colTenMon;
    @FXML private TableColumn<MonHoc, Integer> colSoTinChi;

    @FXML private Label lblStatus;

    private User loggedInUser;
    private SinhVien currentStudent;
    private List<DiemChiTiet> allGrades;
    private Map<String, List<MonHoc>> registeredCoursesBySemester;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void initializeData(User user) {
        this.loggedInUser = user;
        
        if (rootPane != null) {
            playFadeInTransition(rootPane);
        }

        if (user == null || !"sinhvien".equals(user.getRole()) || user.getMaSV() == null || user.getMaSV().isEmpty()) {
            showError(lblStatus, "Lỗi xác thực người dùng.");
             disableButtonsOnError();
            return;
        }
        loadStudentInfo(user.getMaSV());
        loadGradeData(user.getMaSV());
        loadRegisteredCourses(user.getMaSV());
        populateSemesterComboBoxes();
        selectLatestSemester();
    }

     private void disableButtonsOnError() {
        if (btnKetQuaHocTap != null) btnKetQuaHocTap.setDisable(true);
        if (btnDangKyHocPhan != null) btnDangKyHocPhan.setDisable(true);
        if (btnDangKyLopNV != null) btnDangKyLopNV.setDisable(true);
        if (cbSemesterChart != null) cbSemesterChart.setDisable(true);
        if (cbSemesterTable != null) cbSemesterTable.setDisable(true);
     }

    @FXML
    public void initialize() {
        colMaMon.setCellValueFactory(new PropertyValueFactory<>("maMon"));
        colTenMon.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
        colSoTinChi.setCellValueFactory(new PropertyValueFactory<>("soTinChi"));
        tableCourses.setPlaceholder(new Label("Chọn học kỳ để xem lớp học phần."));

        barChartGrades.setTitle("Điểm trung bình môn (Hệ 10)");
        xAxisMonHoc.setLabel("Môn học");
        yAxisDiemTB.setLabel("Điểm TB (Hệ 10)");
        yAxisDiemTB.setAutoRanging(false);
        yAxisDiemTB.setLowerBound(0);
        yAxisDiemTB.setUpperBound(10);
        yAxisDiemTB.setTickUnit(1);

        try {
            Image defaultAvatar = new Image(getClass().getResourceAsStream("/view/default_avatar.png"));
            if (imgAvatar != null) {
                if (defaultAvatar == null || defaultAvatar.isError()) {
                     imgAvatar.setImage(null);
                } else {
                    imgAvatar.setImage(defaultAvatar);
                }
            }
        } catch (Exception e) {
             if (imgAvatar != null) imgAvatar.setImage(null);
        }

         clearStatus(lblStatus);
    }


    private void loadStudentInfo(String maSV) {
        currentStudent = SinhVienDAO.getSinhVienByCriteria("sv.maSV = ?", maSV).stream().findFirst().orElse(null);
        if (currentStudent != null) {
            lblMaSV.setText(currentStudent.getMaSV());
            lblHoTen.setText(currentStudent.getHoTen());
            lblGioiTinh.setText(currentStudent.getGioiTinh() != null ? currentStudent.getGioiTinh() : "N/A");
            lblNgaySinh.setText(currentStudent.getNgaySinh() != null ? currentStudent.getNgaySinh().format(dateFormatter) : "N/A");
            lblLop.setText(currentStudent.getLop() != null ? currentStudent.getLop() : "N/A");
            lblKhoa.setText(currentStudent.getKhoa() != null ? currentStudent.getKhoa().getTenKhoa() : "N/A");
            lblNoiSinh.setText(currentStudent.getQueQuan() != null ? currentStudent.getQueQuan() : "N/A");
        } else {
            showError(lblStatus, "Không tìm thấy thông tin sinh viên " + maSV);
             disableButtonsOnError();
        }
    }

    private void loadGradeData(String maSV) {
        try {
            allGrades = ThongKeDAO.getDiemChiTiet(maSV);
        } catch (Exception e) {
            allGrades = new ArrayList<>();
            showError(lblStatus, "Không thể tải dữ liệu điểm: " + e.getMessage());
             disableButtonsOnError();
        }
    }

    private void loadRegisteredCourses(String maSV) {
        registeredCoursesBySemester = new HashMap<>();
        try {
            if (allGrades != null) {
                Map<String, List<DiemChiTiet>> gradesBySemesterMap = allGrades.stream()
                        .collect(Collectors.groupingBy(d -> {
                             int namHoc = ThongKeDAO.getNamHocFromDiem(d.getMaSV(), d.getMaMon());
                             return d.getHocKy() + "/" + getNhanNamHoc(d.getHocKy(), namHoc);
                        }));

                for (Map.Entry<String, List<DiemChiTiet>> entry : gradesBySemesterMap.entrySet()) {
                    String semesterKey = entry.getKey();
                    List<MonHoc> coursesInSemester = entry.getValue().stream()
                            .map(diem -> new MonHoc(diem.getMaMon(), diem.getTenMon(), diem.getSoTinChi(), null, diem.getHocKy(), null, null))
                            .distinct()
                            .collect(Collectors.toList());
                    registeredCoursesBySemester.put(semesterKey, coursesInSemester);
                }
            }
        } catch (Exception e) {
            showError(lblStatus, "Không thể tải danh sách lớp học phần: " + e.getMessage());
        }
    }

    private void populateSemesterComboBoxes() {
        Set<String> semesters = new TreeSet<>(Comparator
                .comparingInt((String s) -> {
                    try {
                        String yearPart = s.split("/")[1];
                        String startYearStr = yearPart.split("-")[0];
                        return Integer.parseInt(startYearStr);
                    } catch (Exception e) { return 0; }
                })
                .thenComparingInt(s -> {
                    try { return Integer.parseInt(s.split("/")[0]); } catch (Exception e) { return 0; }
                 }));

        if (allGrades != null) {
            allGrades.forEach(diem -> {
                int namHoc = ThongKeDAO.getNamHocFromDiem(diem.getMaSV(), diem.getMaMon());
                if (namHoc != -1) {
                    semesters.add(diem.getHocKy() + "/" + getNhanNamHoc(diem.getHocKy(), namHoc));
                }
            });
        }
         if (registeredCoursesBySemester != null) {
             semesters.addAll(registeredCoursesBySemester.keySet());
         }

        ObservableList<String> semesterList = FXCollections.observableArrayList(semesters);
        cbSemesterChart.setItems(semesterList);
        cbSemesterTable.setItems(semesterList);

         if (semesterList.isEmpty()) {
            cbSemesterChart.setPlaceholder(new Label("Chưa có dữ liệu học kỳ"));
            cbSemesterTable.setPlaceholder(new Label("Chưa có dữ liệu học kỳ"));
             cbSemesterChart.setDisable(true);
             cbSemesterTable.setDisable(true);
         } else {
             cbSemesterChart.setDisable(false);
             cbSemesterTable.setDisable(false);
         }
    }

    private void selectLatestSemester() {
        if (!cbSemesterChart.getItems().isEmpty()) {
            cbSemesterChart.getSelectionModel().selectLast();
            loadChartData();
        } else { loadChartData(); }
        if (!cbSemesterTable.getItems().isEmpty()) {
            cbSemesterTable.getSelectionModel().selectLast();
            loadTableData();
        } else { loadTableData(); }
    }

    private String getNhanNamHoc(int hocKy, int namBatDau) {
         if (namBatDau <= 0) return "N/A";
         return namBatDau + "-" + (namBatDau + 1);
    }

    @FXML
    void loadChartData() {
        String selectedSemester = cbSemesterChart.getValue();
        barChartGrades.getData().clear();
        if (selectedSemester == null || allGrades == null || cbSemesterChart.getItems().isEmpty()) {
            barChartGrades.setTitle("Chưa có dữ liệu học kỳ để hiển thị");
            return;
        }
        try {
            String[] parts = selectedSemester.split("/");
            int ky = Integer.parseInt(parts[0]);
             String yearPart = parts[1];
             String startYearStr = yearPart.split("-")[0];
            int nam = Integer.parseInt(startYearStr);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(selectedSemester);

            List<DiemChiTiet> semesterGrades = allGrades.stream()
                .filter(d -> d.getHocKy() == ky && ThongKeDAO.getNamHocFromDiem(d.getMaSV(), d.getMaMon()) == nam)
                .collect(Collectors.toList());

            if (semesterGrades.isEmpty()) {
                barChartGrades.setTitle("Không có dữ liệu điểm cho " + selectedSemester);
                return;
            }

            Map<String, Double> averageGrades = semesterGrades.stream()
                .collect(Collectors.groupingBy(DiemChiTiet::getMaMon, Collectors.averagingDouble(DiemChiTiet::getDiemTB)));

            List<String> sortedMaMon = averageGrades.keySet().stream().sorted().collect(Collectors.toList());

            for (String maMon : sortedMaMon) {
                String tenMonDisplay = semesterGrades.stream()
                                                    .filter(d -> d.getMaMon().equals(maMon))
                                                    .map(DiemChiTiet::getTenMon)
                                                    .findFirst().orElse(maMon);
                 String categoryLabel = tenMonDisplay.length() > 15 ? tenMonDisplay.substring(0, 12) + "..." : tenMonDisplay;
                 categoryLabel = maMon + "\n" + categoryLabel;
                 series.getData().add(new XYChart.Data<>(categoryLabel, averageGrades.get(maMon)));
            }
            barChartGrades.getData().add(series);
            barChartGrades.setTitle("Điểm TB môn (Hệ 10) - Kỳ " + selectedSemester);
        } catch (Exception e) {
            showError(lblStatus, "Lỗi tải dữ liệu biểu đồ.");
        }
    }

    @FXML
    void loadTableData() {
        String selectedSemester = cbSemesterTable.getValue();
        tableCourses.getItems().clear();
        if (selectedSemester == null || registeredCoursesBySemester == null) {
            tableCourses.setPlaceholder(new Label("Chưa có dữ liệu học kỳ"));
            return;
        }
        List<MonHoc> courses = registeredCoursesBySemester.getOrDefault(selectedSemester, Collections.emptyList());
        if (courses.isEmpty()) {
            tableCourses.setPlaceholder(new Label("Không có lớp học phần nào."));
        } else {
            tableCourses.setItems(FXCollections.observableArrayList(courses));
             tableCourses.setPlaceholder(null);
        }
    }

    @FXML
    void moKetQuaHocTap(ActionEvent event) {
        if (loggedInUser == null) return;
         openWindow("/view/ThongKeView.fxml", "Kết quả học tập", loggedInUser.getMaSV(), ThongKeController.class);
    }

    @FXML
    void moDangKyHocPhan(ActionEvent event) {
        if (loggedInUser == null) return;
        openWindow("/view/DangKyHocPhanView.fxml", "Đăng ký Học phần", loggedInUser.getMaSV(), DangKyHocPhanController.class);
    }

    @FXML
    void moTaoLopNguyenVong(ActionEvent event) {
         if (loggedInUser == null || loggedInUser.getMaSV() == null || loggedInUser.getMaSV().isEmpty()) {
              showError(lblStatus, "Không xác định được sinh viên.");
             return;
         }
         // Mở form TaoLopNguyenVongView thay vì DangKyNguyenVongView
         openWindow("/view/TaoLopNguyenVongView.fxml", "Đăng ký Nguyện Vọng Mở Lớp", loggedInUser, TaoLopNguyenVongController.class);
    }

     private <T extends BaseController> void openWindow(String fxmlFile, String title, Object data, Class<T> controllerClass) {
         try {
             FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
             Parent root = loader.load();
             Object controller = loader.getController();

             if (controllerClass.isInstance(controller)) {
                 T typedController = controllerClass.cast(controller);
                 if (typedController instanceof DangKyHocPhanController) ((DangKyHocPhanController) typedController).initializeData((String) data);
                 else if (typedController instanceof ThongKeController) ((ThongKeController) typedController).initDataForStudent((String) data);
                 // Hỗ trợ TaoLopNguyenVongController
                 else if (typedController instanceof TaoLopNguyenVongController) ((TaoLopNguyenVongController) typedController).initializeData((User) data);
             } 

             Stage stage = new Stage();
             stage.setTitle(title);
             stage.initModality(Modality.APPLICATION_MODAL);
             stage.setScene(new Scene(root));
             
             // Animation Pop-up
             playPopUpAnimation(stage);
             
             stage.showAndWait();

         } catch (IOException e) {
             e.printStackTrace();
             showAlert(Alert.AlertType.ERROR, "Lỗi Tải Giao Diện", e.getMessage());
         }
     }

     private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

     @Override protected void showStatus(Label label, String message, boolean isError) { super.showStatus(this.lblStatus, message, isError); }
     @Override protected void showSuccess(Label label, String message) { super.showSuccess(this.lblStatus, message); }
     @Override protected void showError(Label label, String message) { super.showError(this.lblStatus, message); }
     @Override protected void clearStatus(Label label) { super.clearStatus(this.lblStatus); }
}