package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Khoa {
    private final StringProperty maKhoa;
    private final StringProperty tenKhoa;

    public Khoa(String maKhoa, String tenKhoa) {
        this.maKhoa = new SimpleStringProperty(maKhoa);
        this.tenKhoa = new SimpleStringProperty(tenKhoa);
    }

    // --- Getters ---
    public String getMaKhoa() { return maKhoa.get(); }
    public String getTenKhoa() { return tenKhoa.get(); }

    // --- Setters ---
    public void setMaKhoa(String value) { maKhoa.set(value); }
    public void setTenKhoa(String value) { tenKhoa.set(value); }

    // --- Property methods ---
    public StringProperty maKhoaProperty() { return maKhoa; }
    public StringProperty tenKhoaProperty() { return tenKhoa; }

    @Override
    public String toString() {
        // Hiển thị tên khoa trong ComboBox
        return getTenKhoa();
    }
}
