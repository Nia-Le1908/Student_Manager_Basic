package controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import dao.SinhVienDAO;
import dao.ThongKeDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.DiemChiTiet;
import model.SinhVien;


public class ThongKeController extends BaseController {


    @FXML private ComboBox<SinhVien> cbSinhVien;
    @FXML private TabPane tabPane;
    @FXML private Tab tabBieuDo;
    @FXML private LineChart<String, Number> lineChartDiem;
    @FXML private CategoryAxis xAxisKy;
    @FXML private NumberAxis yAxisDiem;
    @FXML private HBox selectionHBox;
    @FXML private VBox topVBox; 


    private String currentMaSV; // Lưu mã SV để mở cửa sổ đăng ký

    @FXML
    public void initialize() {
        // Chỉ hiển thị ComboBox và nút xem thống kê nếu không phải sinh viên đăng nhập trực tiếp
        boolean isAdminOrGV = true; // Giả sử mặc định là Admin/GV


        if (selectionHBox != null) {
            if (isAdminOrGV) {
                 try {
                    cbSinhVien.setItems(SinhVienDAO.getAllSinhVien());
                    cbSinhVien.setConverter(new javafx.util.StringConverter<SinhVien>() {
                        @Override public String toString(SinhVien sv) { return sv == null ? null : sv.getMaSV() + " - " + sv.getHoTen(); }
                        @Override public SinhVien fromString(String string) { return null;}
                    });
                 } catch (Exception e) { System.err.println("Lỗi tải DS Sinh viên: " + e.getMessage()); e.printStackTrace(); }
            } else {
                 selectionHBox.setVisible(false);
                 selectionHBox.setManaged(false);
            }
        }



        if(tabPane != null) tabPane.getTabs().removeIf(tab -> tab != tabBieuDo);
        if(yAxisDiem != null) yAxisDiem.setLabel("Điểm GPA (Hệ 4)");
        if(xAxisKy != null) xAxisKy.setLabel("Học Kỳ");
         if(lineChartDiem != null) lineChartDiem.setTitle("Chọn sinh viên để xem biểu đồ");

    }

    public void initDataForStudent(String maSV) {
         if (maSV == null || maSV.isEmpty()) {
             showError(new Label("Lỗi"), "Không nhận được mã sinh viên.");

             return;
         }
        this.currentMaSV = maSV; // Lưu mã SV


        // Ẩn phần lựa chọn SV của Admin/GV
        if(selectionHBox != null) {
            selectionHBox.setVisible(false);
            selectionHBox.setManaged(false);
        }


         // Tải dữ liệu thống kê cho sinh viên này
         xemThongKeInternal(maSV);
    }



    // Called when Admin/GV selects a student from ComboBox
    @FXML
    private void xemThongKe() {
        SinhVien selectedSV = (cbSinhVien != null) ? cbSinhVien.getValue() : null;
        if (selectedSV == null) {
            if(tabPane != null) tabPane.getTabs().removeIf(tab -> tab != tabBieuDo);
            if(lineChartDiem != null) lineChartDiem.getData().clear();
            if(lineChartDiem != null) lineChartDiem.setTitle("Vui lòng chọn sinh viên");
            return;
        }
        this.currentMaSV = selectedSV.getMaSV();
        xemThongKeInternal(selectedSV.getMaSV());
    }

    private void xemThongKeInternal(String maSV) {
         if (maSV == null || maSV.isEmpty()) return;
         try {
             List<DiemChiTiet> dsDiem = ThongKeDAO.getDiemChiTiet(maSV);
             taoGiaoDienBaoCao(dsDiem);
             if (lineChartDiem != null) lineChartDiem.setTitle("Biểu đồ Điểm TB Học Kỳ (Hệ 4) - SV: " + maSV);
         } catch (Exception e) {
             System.err.println("Lỗi khi xem thống kê cho SV " + maSV + ": " + e.getMessage()); e.printStackTrace();
              if(tabPane != null) {
                  tabPane.getTabs().clear();
                  Tab errorTab = new Tab("Lỗi");
                  errorTab.setContent(new Label("Đã xảy ra lỗi khi tải dữ liệu điểm."));
                  tabPane.getTabs().add(errorTab);
              }
              if (lineChartDiem != null) lineChartDiem.getData().clear();
              if(lineChartDiem != null) lineChartDiem.setTitle("Lỗi tải dữ liệu điểm");
         }
    }


    private void taoGiaoDienBaoCao(List<DiemChiTiet> dsDiem) {
         if (tabPane == null) return;
         // Giữ lại tab biểu đồ, xóa các tab học kỳ và tổng kết cũ
         tabPane.getTabs().removeIf(tab -> tab != tabBieuDo);


        if (dsDiem.isEmpty()) {
            Tab emptyTab = new Tab("Thông báo");
            emptyTab.setClosable(false);
            emptyTab.setContent(new Label("Sinh viên này chưa có điểm."));
             // Thêm tab thông báo vào đầu (sau tab biểu đồ nếu nó ở đầu)
            tabPane.getTabs().add(tabPane.getTabs().contains(tabBieuDo) ? 1 : 0, emptyTab);
             if (lineChartDiem != null) lineChartDiem.getData().clear();
             if (tabBieuDo != null) tabBieuDo.setDisable(true);
             if(lineChartDiem != null) lineChartDiem.setTitle("Chưa có dữ liệu điểm");
            return;
        }

         if (tabBieuDo != null) tabBieuDo.setDisable(false);


        double tongDiemNhanTinChiHe4ToanKhoa = 0;
        int tongTinChiDaHocToanKhoa = 0;
        int tongTinChiTichLuyToanKhoa = 0; // Vẫn tính riêng tín chỉ tích lũy

        // Nhóm điểm theo kỳ
         Map<Integer, List<DiemChiTiet>> diemTheoKy = dsDiem.stream()
            .collect(Collectors.groupingBy(DiemChiTiet::getHocKy));

         // Lấy danh sách các kỳ đã sắp xếp
        List<Integer> sortedKeys = diemTheoKy.keySet().stream().sorted().collect(Collectors.toList());

        for (int hocKy : sortedKeys) {
            List<DiemChiTiet> diemKyNay = diemTheoKy.get(hocKy);
            double tongDiemNhanTinChiHe4KyNay = 0;
            int tongTinChiKyNay = 0; // Tổng TC đã học trong kỳ

            // Tính toán cho từng môn trong kỳ
            for (DiemChiTiet diem : diemKyNay) {
                int soTinChiMonNay = diem.getSoTinChi();
                double diemHe10MonNay = diem.getDiemTB();
                double diemHe4MonNay = ThongKeDAO.convertToScale4(diemHe10MonNay);

                // Cộng vào tổng toàn khóa
                tongTinChiDaHocToanKhoa += soTinChiMonNay;
                tongDiemNhanTinChiHe4ToanKhoa += diemHe4MonNay * soTinChiMonNay;
                if (diemHe10MonNay >= 4.0) { // Chỉ cộng tín chỉ tích lũy nếu qua môn
                    tongTinChiTichLuyToanKhoa += soTinChiMonNay;
                }

                // Cộng vào tổng của kỳ này (cho tab học kỳ)
                 tongTinChiKyNay += soTinChiMonNay;
                 // Điểm nhân tín chỉ của kỳ chỉ tính môn qua (thường là vậy, cần xác nhận lại)
                 if (diemHe10MonNay >= 4.0) {
                     tongDiemNhanTinChiHe4KyNay += diemHe4MonNay * soTinChiMonNay;
                 }
            }

            // Tính GPA và xếp loại cho kỳ này
            // GPA Kỳ nên chia cho tổng TC đã học trong kỳ
             double gpaKyNay = (tongTinChiKyNay == 0) ? 0.0 : tongDiemNhanTinChiHe4KyNay / tongTinChiKyNay;
            String xepLoaiKy = ThongKeDAO.xepLoaiHocLuc(gpaKyNay);

            // Tạo Tab cho học kỳ
            Tab tab = new Tab("Học kỳ " + hocKy);
            VBox tabContent = new VBox(15);
            tabContent.setPadding(new javafx.geometry.Insets(15)); // Chỉ định rõ Insets
            TableView<DiemChiTiet> tableView = taoBangDiem();
            tableView.setItems(FXCollections.observableArrayList(diemKyNay));

            Label lblGpa = new Label(String.format("Điểm trung bình học kỳ (Hệ 4): %.2f", gpaKyNay));
            Label lblXepLoai = new Label("Xếp loại học kỳ: " + xepLoaiKy);
            lblGpa.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            lblXepLoai.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            tabContent.getChildren().addAll(tableView, lblGpa, lblXepLoai);
            tab.setContent(tabContent);
             tabPane.getTabs().add(tab); // Thêm tab học kỳ vào cuối
        }

        // --- Tạo Tab Tổng kết ---
        Tab summaryTab = new Tab("Tổng kết");
        VBox summaryContent = new VBox(10);
        summaryContent.setPadding(new javafx.geometry.Insets(20)); // Chỉ định rõ Insets

        // Tính GPA toàn khóa theo cách mới (chia cho tổng TC đã học)
        double gpaTichLuyToanKhoa = (tongTinChiDaHocToanKhoa == 0) ? 0.0 : tongDiemNhanTinChiHe4ToanKhoa / tongTinChiDaHocToanKhoa;
        String xepLoaiToanKhoa = ThongKeDAO.xepLoaiHocLuc(gpaTichLuyToanKhoa);

        Label title = new Label("KẾT QUẢ HỌC TẬP TOÀN KHÓA");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label lblTongTinChiDaHoc = new Label("Tổng số tín chỉ đã đăng ký: " + tongTinChiDaHocToanKhoa); // Hiển thị TC đã học
        Label lblTongTinChiTichLuy = new Label("Tổng số tín chỉ tích lũy: " + tongTinChiTichLuyToanKhoa); // Hiển thị TC tích lũy
        Label lblGpaTichLuy = new Label(String.format("Điểm trung bình tích lũy (Hệ 4): %.2f", gpaTichLuyToanKhoa)); // GPA mới
        Label lblXepLoaiTK = new Label("Xếp loại toàn khóa: " + xepLoaiToanKhoa);

        lblTongTinChiDaHoc.setStyle("-fx-font-size: 14px;");
        lblTongTinChiTichLuy.setStyle("-fx-font-size: 14px;");
        lblGpaTichLuy.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        lblXepLoaiTK.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        summaryContent.getChildren().addAll(title, lblTongTinChiDaHoc, lblTongTinChiTichLuy, lblGpaTichLuy, lblXepLoaiTK);
        summaryTab.setContent(summaryContent);
         tabPane.getTabs().add(summaryTab); // Thêm tab tổng kết vào cuối

         // --- Tạo Biểu đồ ---
         taoBieuDoDiem(diemTheoKy); // Gọi sau khi đã tính toán các kỳ

         // --- Sắp xếp lại Tabs ---
         // Đảm bảo tab biểu đồ luôn ở đầu tiên, tab tổng kết luôn ở cuối cùng
         if (tabBieuDo != null && tabPane.getTabs().contains(tabBieuDo)) {
              tabPane.getTabs().remove(tabBieuDo);
              tabPane.getTabs().add(0, tabBieuDo);
         }
         if (tabPane.getTabs().contains(summaryTab)) {
             tabPane.getTabs().remove(summaryTab);
             tabPane.getTabs().add(summaryTab);
         }


         // Chọn tab đầu tiên (thường là biểu đồ)
         if (!tabPane.getTabs().isEmpty()) {
            tabPane.getSelectionModel().selectFirst();
         }
    }

    /** Tạo và hiển thị dữ liệu lên biểu đồ đường */
    private void taoBieuDoDiem(Map<Integer, List<DiemChiTiet>> diemTheoKy) {
        if (lineChartDiem == null) { System.err.println("Lỗi: LineChart null."); return; }
        lineChartDiem.getData().clear();
        lineChartDiem.setLegendVisible(false);

        XYChart.Series<String, Number> seriesGPA = new XYChart.Series<>();
        seriesGPA.setName("Điểm GPA Hệ 4");

        List<Integer> sortedKy = diemTheoKy.keySet().stream().sorted().collect(Collectors.toList());

        for (int hocKy : sortedKy) {
            List<DiemChiTiet> diemKyNay = diemTheoKy.get(hocKy);
            double tongDiemNhanTinChiHe4KyNay = 0;
             // **Sửa:** Mẫu số là tổng TC đã học trong kỳ
            int tongTinChiDaHocKyNay = 0;

            for (DiemChiTiet diem : diemKyNay) {
                 int soTinChiMonNay = diem.getSoTinChi();
                 double diemHe10MonNay = diem.getDiemTB();
                 double diemHe4MonNay = ThongKeDAO.convertToScale4(diemHe10MonNay);

                 tongTinChiDaHocKyNay += soTinChiMonNay; // Luôn cộng TC đã học
                 tongDiemNhanTinChiHe4KyNay += diemHe4MonNay * soTinChiMonNay; // Luôn cộng điểm * TC (vì F=0)
            }
             // **Sửa:** Chia cho tổng TC đã học trong kỳ
            double gpaKyNay = (tongTinChiDaHocKyNay == 0) ? 0.0 : tongDiemNhanTinChiHe4KyNay / tongTinChiDaHocKyNay;
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>("Kỳ " + hocKy, gpaKyNay);
             seriesGPA.getData().add(dataPoint);
        }

        lineChartDiem.getData().add(seriesGPA);

         // Cấu hình trục Y
         if(yAxisDiem != null) {
            yAxisDiem.setAutoRanging(false);
            yAxisDiem.setLowerBound(0.0);
            yAxisDiem.setUpperBound(4.0);
            yAxisDiem.setTickUnit(0.5);
         }

         // Đặt tiêu đề biểu đồ
         if (seriesGPA.getData().isEmpty() && lineChartDiem != null) {
             lineChartDiem.setTitle("Không có dữ liệu điểm GPA theo kỳ");
         } else if (lineChartDiem != null) {
             String maSVDisplay = currentMaSV != null ? currentMaSV : ""; // Lấy mã SV đã lưu
             lineChartDiem.setTitle("Biểu đồ Điểm TB Học Kỳ (Hệ 4)" + (maSVDisplay.isEmpty() ? "" : " - SV: " + maSVDisplay));
         }

    }


    /** Tạo bảng điểm mới với các cột được cấu hình */
    private TableView<DiemChiTiet> taoBangDiem() {
        TableView<DiemChiTiet> tableView = new TableView<>();
        TableColumn<DiemChiTiet, String> colMaMon = new TableColumn<>("Mã Môn");
        TableColumn<DiemChiTiet, String> colTenMon = new TableColumn<>("Tên Môn Học");
        TableColumn<DiemChiTiet, Integer> colSoTinChi = new TableColumn<>("Số TC");
        TableColumn<DiemChiTiet, Float> colDiemQT = new TableColumn<>("Điểm QT");
        TableColumn<DiemChiTiet, Float> colDiemThi = new TableColumn<>("Điểm Thi");
        TableColumn<DiemChiTiet, Float> colDiemTB = new TableColumn<>("Điểm TB (10)");
        TableColumn<DiemChiTiet, String> colDiemChu = new TableColumn<>("Điểm Chữ");

        colMaMon.setCellValueFactory(new PropertyValueFactory<>("maMon"));
        colTenMon.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
        colSoTinChi.setCellValueFactory(new PropertyValueFactory<>("soTinChi"));
        colDiemQT.setCellValueFactory(new PropertyValueFactory<>("diemQT"));
        colDiemThi.setCellValueFactory(new PropertyValueFactory<>("diemThi"));
        colDiemTB.setCellValueFactory(new PropertyValueFactory<>("diemTB"));
        colDiemChu.setCellValueFactory(new PropertyValueFactory<>("diemChu"));

        colTenMon.setPrefWidth(250);
        colMaMon.setPrefWidth(100);
         colSoTinChi.setStyle("-fx-alignment: CENTER;");
         colDiemQT.setStyle("-fx-alignment: CENTER;");
         colDiemThi.setStyle("-fx-alignment: CENTER;");
         colDiemTB.setStyle("-fx-alignment: CENTER;");
         colDiemChu.setStyle("-fx-alignment: CENTER;");

        tableView.getColumns().addAll(colMaMon, colTenMon, colSoTinChi, colDiemQT, colDiemThi, colDiemTB, colDiemChu);
        tableView.setPlaceholder(new Label("Chưa có điểm cho học kỳ này.")); // Thêm placeholder
        return tableView;
    }
}