package controller;

import dao.KyHocDAO;
import dao.SinhVienDangKyDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import model.KyHoc;
import model.MonHoc;

import java.time.LocalDate;
import java.util.stream.Collectors;

public class DangKyHocPhanController extends BaseController {

    @FXML private Label lblStudentInfo;
    @FXML private ComboBox<KyHoc> cbKyHoc;
    @FXML private ListView<MonHoc> lvAvailableCourses;
    @FXML private ListView<MonHoc> lvRegisteredCourses;
    @FXML private Button btnRegister;
    @FXML private Button btnUnregister;
    @FXML private Label lblStatus;

    private String currentMaSV;
    private KyHoc selectedKyHoc;

    @FXML
    public void initialize() {
        // Cấu hình hiển thị cho ListView
        configureListView(lvAvailableCourses);
        configureListView(lvRegisteredCourses);

        // Cấu hình hiển thị cho ComboBox Kỳ học
        cbKyHoc.setConverter(new StringConverter<KyHoc>() {
            @Override public String toString(KyHoc k) { return k == null ? null : "Học kỳ " + k.getHocKy() + " - " + k.getNamHoc(); }
            @Override public KyHoc fromString(String string) { return null; }
        });

        // Xử lý nút Đăng ký/Hủy (Enable/Disable)
        lvAvailableCourses.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> 
            btnRegister.setDisable(newV == null)
        );
        lvRegisteredCourses.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> 
            btnUnregister.setDisable(newV == null)
        );
        
        btnRegister.setDisable(true);
        btnUnregister.setDisable(true);
    }

    public void initializeData(String maSV) {
        this.currentMaSV = maSV;
        lblStudentInfo.setText(maSV);
        loadKyHoc();
    }

    private void loadKyHoc() {
        ObservableList<KyHoc> listKyHoc = KyHocDAO.getAllKyHoc();
        
        int currentYear = LocalDate.now().getYear();

        // Lọc danh sách kỳ học theo yêu cầu:
        // 1. Chỉ lấy năm hiện tại và năm trước đó (currentYear và currentYear - 1)
        // 2. Học kỳ phải nằm trong khoảng 1 đến 3
        ObservableList<KyHoc> filteredList = listKyHoc.stream()
                .filter(k -> (k.getNamHoc() == currentYear || k.getNamHoc() == currentYear - 1))
                .filter(k -> k.getHocKy() >= 1 && k.getHocKy() <= 3)
                .collect(Collectors.toCollection(javafx.collections.FXCollections::observableArrayList));

        cbKyHoc.setItems(filteredList);
        
        if (filteredList.isEmpty()) return;

        //  Tự động chọn Kỳ Học Hiện Tại dựa trên thời gian thực ---
        KyHoc currentSemester = null;
        int currentMonth = LocalDate.now().getMonthValue();
        
        // Xác định Kỳ/Năm dự kiến
        int targetKy = (currentMonth >= 9) ? 1 : (currentMonth <= 5 ? 2 : 3);
        int targetNam = (currentMonth >= 9) ? currentYear : currentYear - 1;

        // Tìm trong danh sách ĐÃ LỌC xem có kỳ nào khớp không
        for (KyHoc k : filteredList) {
            if (k.getHocKy() == targetKy && k.getNamHoc() == targetNam) {
                currentSemester = k;
                break;
            }
        }

        if (currentSemester != null) {
            cbKyHoc.getSelectionModel().select(currentSemester);
        } else {
            // Fallback: Nếu không tìm thấy kỳ hiện tại, chọn kỳ mới nhất trong danh sách đã lọc
            cbKyHoc.getSelectionModel().selectFirst(); 
        }
        
        // Tải dữ liệu môn học
        handleKyHocChange(); 
    }

    @FXML
    private void handleKyHocChange() {
        this.selectedKyHoc = cbKyHoc.getValue();
        if (selectedKyHoc != null) {
            loadData();
        }
    }

    private void loadData() {
        if (currentMaSV == null || selectedKyHoc == null) return;
        
        clearStatus(lblStatus);
        
        try {
            int idKy = selectedKyHoc.getId(); // Quan trọng: Lấy ID kỳ học thực tế từ DB
            
            // 1. Lấy danh sách Môn CÓ MỞ LỚP trong kỳ này (chưa đăng ký)
            ObservableList<MonHoc> available = SinhVienDangKyDAO.getAvailableMonHoc(currentMaSV, idKy);
            lvAvailableCourses.setItems(available);

            // 2. Lấy danh sách Môn ĐÃ ĐĂNG KÝ trong kỳ này
            ObservableList<MonHoc> registered = SinhVienDangKyDAO.getRegisteredMonHoc(currentMaSV, idKy);
            lvRegisteredCourses.setItems(registered);

            // Thông báo nếu trống
            if(available.isEmpty()) lvAvailableCourses.setPlaceholder(new Label("Không có môn nào mở lớp kỳ này."));
            else lvAvailableCourses.setPlaceholder(null);
            
            if(registered.isEmpty()) lvRegisteredCourses.setPlaceholder(new Label("Chưa đăng ký môn nào."));
            else lvRegisteredCourses.setPlaceholder(null);

        } catch (Exception e) {
            showError(lblStatus, "Lỗi tải dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        MonHoc selected = lvAvailableCourses.getSelectionModel().getSelectedItem();
        if (selected != null && selectedKyHoc != null) {
            // Gọi hàm đăng ký với ID kỳ học
            if (SinhVienDangKyDAO.register(currentMaSV, selected.getMaMon(), selectedKyHoc.getId())) {
                showSuccess(lblStatus, "Đăng ký thành công: " + selected.getTenMon());
                loadData(); // Refresh lại 2 danh sách
            } else {
                showError(lblStatus, "Đăng ký thất bại (Lớp đầy hoặc lỗi hệ thống).");
            }
        }
    }

    @FXML
    private void handleUnregister() {
        MonHoc selected = lvRegisteredCourses.getSelectionModel().getSelectedItem();
        if (selected != null && selectedKyHoc != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Hủy đăng ký môn: " + selected.getTenMon() + "?");
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                if (SinhVienDangKyDAO.unregister(currentMaSV, selected.getMaMon(), selectedKyHoc.getId())) {
                    showSuccess(lblStatus, "Đã hủy đăng ký.");
                    loadData(); // Refresh lại 2 danh sách
                } else {
                    showError(lblStatus, "Lỗi khi hủy đăng ký.");
                }
            }
        }
    }
    
    private void configureListView(ListView<MonHoc> listView) {
        listView.setCellFactory(lv -> new ListCell<MonHoc>() {
            @Override protected void updateItem(MonHoc item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    // Hiển thị: Mã môn - Tên môn (Số TC)
                    setText(item.getMaMon() + " - " + item.getTenMon() + " (" + item.getSoTinChi() + "TC)");
                }
            }
        });
    }
}