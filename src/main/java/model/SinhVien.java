package model;

import java.time.LocalDate;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SinhVien {
    private final StringProperty maSV;
    private final StringProperty hoTen;
    private final ObjectProperty<LocalDate> ngaySinh;
    private final StringProperty gioiTinh;
    private final StringProperty queQuan;
    private final ObjectProperty<Khoa> khoa; 
    private final StringProperty lop;
    private final DoubleProperty diemTichLuy;

    // Constructor updated to accept Khoa object
    public SinhVien(String maSV, String hoTen, LocalDate ngaySinh, String gioiTinh, String queQuan, Khoa khoa, String lop) {
        this.maSV = new SimpleStringProperty(maSV);
        this.hoTen = new SimpleStringProperty(hoTen);
        this.ngaySinh = new SimpleObjectProperty<>(ngaySinh);
        this.gioiTinh = new SimpleStringProperty(gioiTinh);
        this.queQuan = new SimpleStringProperty(queQuan);
        this.khoa = new SimpleObjectProperty<>(khoa); 
        this.lop = new SimpleStringProperty(lop);
        this.diemTichLuy = new SimpleDoubleProperty(0.0);
    }

    // --- Getters & Setters ---
    public String getMaSV() { return maSV.get(); }
    public void setMaSV(String value) { maSV.set(value); }
    public StringProperty maSVProperty() { return maSV; }

    public String getHoTen() { return hoTen.get(); }
    public void setHoTen(String value) { hoTen.set(value); }
    public StringProperty hoTenProperty() { return hoTen; }

    public LocalDate getNgaySinh() { return ngaySinh.get(); }
    public void setNgaySinh(LocalDate value) { ngaySinh.set(value); }
    public ObjectProperty<LocalDate> ngaySinhProperty() { return ngaySinh; }

    public String getGioiTinh() { return gioiTinh.get(); }
    public void setGioiTinh(String value) { gioiTinh.set(value); }
    public StringProperty gioiTinhProperty() { return gioiTinh; }

    public String getQueQuan() { return queQuan.get(); }
    public void setQueQuan(String value) { queQuan.set(value); }
    public StringProperty queQuanProperty() { return queQuan; }

    // Updated getter/setter/property for Khoa
    public Khoa getKhoa() { return khoa.get(); }
    public void setKhoa(Khoa value) { khoa.set(value); }
    public ObjectProperty<Khoa> khoaProperty() { return khoa; }

    public String getLop() { return lop.get(); }
    public void setLop(String value) { lop.set(value); }
    public StringProperty lopProperty() { return lop; }

    // Getter/Setter/Property for diemTichLuy (Double)
    public double getDiemTichLuy() { return diemTichLuy.get(); }
    public void setDiemTichLuy(double value) { diemTichLuy.set(value); }
    public DoubleProperty diemTichLuyProperty() { return diemTichLuy; }


    // For ComboBox display
    @Override
    public String toString() {
         // Hiển thị cả mã và tên trong ComboBox để dễ chọn
         return String.format("%s - %s", getMaSV(), getHoTen());
     }
}