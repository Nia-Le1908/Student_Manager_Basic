package model;

import javafx.beans.property.*;

public class LopHanhChinh {
    private final StringProperty maLop;
    private final StringProperty tenLop;
    private final ObjectProperty<Khoa> khoa; // Liên kết với Khoa
    private final IntegerProperty khoaHoc;   // Khóa học (VD: 2024)

    public LopHanhChinh(String maLop, String tenLop, Khoa khoa, int khoaHoc) {
        this.maLop = new SimpleStringProperty(maLop);
        this.tenLop = new SimpleStringProperty(tenLop);
        this.khoa = new SimpleObjectProperty<>(khoa);
        this.khoaHoc = new SimpleIntegerProperty(khoaHoc);
    }

    // --- Getters & Setters ---
    public String getMaLop() { return maLop.get(); }
    public void setMaLop(String value) { maLop.set(value); }
    public StringProperty maLopProperty() { return maLop; }

    public String getTenLop() { return tenLop.get(); }
    public void setTenLop(String value) { tenLop.set(value); }
    public StringProperty tenLopProperty() { return tenLop; }

    public Khoa getKhoa() { return khoa.get(); }
    public void setKhoa(Khoa value) { khoa.set(value); }
    public ObjectProperty<Khoa> khoaProperty() { return khoa; }

    public int getKhoaHoc() { return khoaHoc.get(); }
    public void setKhoaHoc(int value) { khoaHoc.set(value); }
    public IntegerProperty khoaHocProperty() { return khoaHoc; }

    @Override
    public String toString() {
        return getMaLop(); // Hiển thị mã lớp trong ComboBox
    }
}