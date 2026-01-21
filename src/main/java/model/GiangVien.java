package model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class GiangVien {
    private final StringProperty maGV;
    private final StringProperty hoTen;
    private final StringProperty email;
    private final ObjectProperty<Khoa> khoa; // Sử dụng ObjectProperty<Khoa>

    // Constructor chuẩn xác với 4 tham số
    public GiangVien(String maGV, String hoTen, String email, Khoa khoa) {
        this.maGV = new SimpleStringProperty(maGV);
        this.hoTen = new SimpleStringProperty(hoTen);
        this.email = new SimpleStringProperty(email == null ? "" : email); // Xử lý null
        this.khoa = new SimpleObjectProperty<>(khoa); // Khởi tạo ObjectProperty
    }

    // --- Getters ---
    public String getMaGV() { return maGV.get(); }
    public String getHoTen() { return hoTen.get(); }
    public String getEmail() { return email.get(); }
    public Khoa getKhoa() { return khoa.get(); } // Trả về đối tượng Khoa

    // --- Setters ---
    public void setMaGV(String value) { maGV.set(value); } // Thường không thay đổi khóa chính
    public void setHoTen(String value) { hoTen.set(value); }
    public void setEmail(String value) { email.set(value == null ? "" : value); } // Xử lý null
    public void setKhoa(Khoa value) { khoa.set(value); } // Set đối tượng Khoa

    // --- Property Methods ---
    public StringProperty maGVProperty() { return maGV; }
    public StringProperty hoTenProperty() { return hoTen; }
    public StringProperty emailProperty() { return email; }
    public ObjectProperty<Khoa> khoaProperty() { return khoa; } // Trả về ObjectProperty<Khoa>

    // toString để hiển thị trong ComboBox (nếu cần)
    @Override
    public String toString() {
        return maGV.get() + " - " + hoTen.get();
    }
}