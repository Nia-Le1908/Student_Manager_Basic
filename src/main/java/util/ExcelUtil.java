package util;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.KetQuaHocTap;
import model.SinhVien;
import model.Khoa;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {

    // --- PHẦN 1: XỬ LÝ SINH VIÊN ---

    public static void exportSinhVienToExcel(List<SinhVien> list, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu danh sách sinh viên");
        fileChooser.setInitialFileName("DanhSachSinhVien.xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("SinhVien");
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Mã SV", "Họ Tên", "Ngày Sinh", "Giới Tính", "Quê Quán", "Khoa", "Lớp"};
                
                CellStyle headerStyle = createHeaderStyle(workbook);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                int rowNum = 1;
                for (SinhVien sv : list) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(sv.getMaSV());
                    row.createCell(1).setCellValue(sv.getHoTen());
                    row.createCell(2).setCellValue(sv.getNgaySinh() != null ? sv.getNgaySinh().toString() : "");
                    row.createCell(3).setCellValue(sv.getGioiTinh());
                    row.createCell(4).setCellValue(sv.getQueQuan());
                    row.createCell(5).setCellValue(sv.getKhoa() != null ? sv.getKhoa().getMaKhoa() : "");
                    row.createCell(6).setCellValue(sv.getLop());
                }
                for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<SinhVien> importSinhVienFromExcel(Stage stage) {
        List<SinhVien> list = new ArrayList<>();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file Excel Sinh viên");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);
                
                // **VALIDATE HEADER**: Kiểm tra xem có đúng file Sinh viên không
                Row headerRow = sheet.getRow(0);
                if (headerRow == null || !getCellValue(headerRow.getCell(0)).equalsIgnoreCase("Mã SV")) {
                    showErrorAlert("File không hợp lệ!", "Vui lòng chọn đúng file Danh sách Sinh viên (Cột A phải là 'Mã SV').");
                    return list;
                }

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    String maSV = getCellValue(row.getCell(0));
                    String hoTen = getCellValue(row.getCell(1));
                    String ngaySinhStr = getCellValue(row.getCell(2));
                    String gioiTinh = getCellValue(row.getCell(3));
                    String queQuan = getCellValue(row.getCell(4));
                    String maKhoa = getCellValue(row.getCell(5));
                    String lop = getCellValue(row.getCell(6));

                    if (!maSV.isEmpty()) {
                        LocalDate ngaySinh = null;
                        try { if (!ngaySinhStr.isEmpty()) ngaySinh = LocalDate.parse(ngaySinhStr); } catch (Exception e) {}
                        list.add(new SinhVien(maSV, hoTen, ngaySinh, gioiTinh, queQuan, new Khoa(maKhoa, ""), lop));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Lỗi đọc file", e.getMessage());
            }
        }
        return list;
    }

    // --- PHẦN 2: XỬ LÝ BẢNG ĐIỂM ---

    public static void exportBangDiemToExcel(List<KetQuaHocTap> list, String tenLop, String tenMon, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Xuất Bảng Điểm");
        fileChooser.setInitialFileName("BangDiem_" + tenLop + "_" + tenMon + ".xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("BangDiem");
                
                Row infoRow = sheet.createRow(0);
                infoRow.createCell(0).setCellValue("Lớp: " + tenLop);
                infoRow.createCell(2).setCellValue("Môn: " + tenMon);

                Row headerRow = sheet.createRow(2);
                String[] headers = {"Mã SV", "Họ Tên", "Điểm QT", "Điểm Thi", "Điểm TB", "Ghi Chú"};
                
                CellStyle headerStyle = createHeaderStyle(workbook);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                int rowNum = 3;
                for (KetQuaHocTap kq : list) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(kq.getMaSV());
                    row.createCell(1).setCellValue(kq.getHoTenSV());
                    if(kq.getDiemQT() >= 0) row.createCell(2).setCellValue(kq.getDiemQT());
                    if(kq.getDiemThi() >= 0) row.createCell(3).setCellValue(kq.getDiemThi());
                    String formula = "ROUND(C" + rowNum + "*0.4 + D" + rowNum + "*0.6, 1)";
                    row.createCell(4).setCellFormula(formula); 
                }
                for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<KetQuaHocTap> importBangDiemFromExcel(Stage stage) {
        List<KetQuaHocTap> list = new ArrayList<>();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Nhập Bảng Điểm từ Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);
                
                // **VALIDATE HEADER**: Kiểm tra xem có đúng file Bảng điểm không
                // File điểm có header ở dòng index 2
                Row headerRow = sheet.getRow(2);
                if (headerRow == null || !getCellValue(headerRow.getCell(2)).contains("Điểm")) {
                     showErrorAlert("File không hợp lệ!", "Vui lòng chọn đúng file Bảng điểm (Có cột 'Điểm QT', 'Điểm Thi').");
                     return list;
                }

                int startRow = 3; 
                for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    String maSV = getCellValue(row.getCell(0));
                    String diemQTStr = getCellValue(row.getCell(2));
                    String diemThiStr = getCellValue(row.getCell(3));

                    if (!maSV.isEmpty()) {
                        float diemQT = -1, diemThi = -1;
                        try { if(!diemQTStr.isEmpty()) diemQT = Float.parseFloat(diemQTStr); } catch(Exception e){}
                        try { if(!diemThiStr.isEmpty()) diemThi = Float.parseFloat(diemThiStr); } catch(Exception e){}
                        KetQuaHocTap kq = new KetQuaHocTap(0, maSV, "", "", "", 0, 0, 0, diemQT, diemThi, 0f, "");
                        list.add(kq);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Lỗi đọc file", e.getMessage());
            }
        }
        return list;
    }

    // --- HELPER METHODS ---

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: 
                if (DateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue().toString();
                double val = cell.getNumericCellValue();
                if (val == (long) val) return String.format("%d", (long) val);
                return String.valueOf(val);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try { return String.valueOf(cell.getNumericCellValue()); } catch (Exception e) { return cell.getCellFormula(); }
            default: return "";
        }
    }

    private static void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}