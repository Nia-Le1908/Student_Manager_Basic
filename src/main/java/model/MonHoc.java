package model;

import java.util.Objects;
import javafx.beans.property.*;

public class MonHoc {
    private final StringProperty maMon;
    private final StringProperty tenMon;
    private final IntegerProperty soTinChi;
    private final StringProperty loaiMon;
    private final IntegerProperty hocKy;
    private final StringProperty monTienQuyet;
    private final ObjectProperty<Khoa> khoa; // Thêm thuộc tính Khoa

    /**
     * Constructor đầy đủ (bao gồm Khoa).
     */
    public MonHoc(String maMon, String tenMon, int soTinChi, String loaiMon, int hocKy, String monTienQuyet, Khoa khoa) {
        this.maMon = new SimpleStringProperty(maMon);
        this.tenMon = new SimpleStringProperty(tenMon);
        this.soTinChi = new SimpleIntegerProperty(soTinChi);
        this.loaiMon = new SimpleStringProperty(loaiMon);
        this.hocKy = new SimpleIntegerProperty(hocKy);
        this.monTienQuyet = new SimpleStringProperty(monTienQuyet == null ? "" : monTienQuyet);
        this.khoa = new SimpleObjectProperty<>(khoa); // Khởi tạo Khoa
    }


     /**
     * Constructor đơn giản chỉ với mã và tên (dùng cho ComboBox).
     */
     public MonHoc(String maMon, String tenMon) {
        this.maMon = new SimpleStringProperty(maMon);
        this.tenMon = new SimpleStringProperty(tenMon);
        this.soTinChi = new SimpleIntegerProperty(0);
        this.loaiMon = new SimpleStringProperty("");
        this.hocKy = new SimpleIntegerProperty(0);
        this.monTienQuyet = new SimpleStringProperty("");
        this.khoa = new SimpleObjectProperty<>(null); // Khoa là null ban đầu
    }


    // --- Getters ---
    public String getMaMon() { return maMon.get(); }
    public String getTenMon() { return tenMon.get(); }
    public int getSoTinChi() { return soTinChi.get(); }
    public String getLoaiMon() { return loaiMon.get(); }
    public int getHocKy() { return hocKy.get(); }
    public String getMonTienQuyet() { return monTienQuyet.get(); }
    public Khoa getKhoa() { return khoa.get(); } // Getter cho Khoa

     // --- Setters ---
    public void setMaMon(String maMon) { this.maMon.set(maMon); }
    public void setTenMon(String tenMon) { this.tenMon.set(tenMon); }
    public void setSoTinChi(int soTinChi) { this.soTinChi.set(soTinChi); }
    public void setLoaiMon(String loaiMon) { this.loaiMon.set(loaiMon); }
    public void setHocKy(int hocKy) { this.hocKy.set(hocKy); }
    public void setMonTienQuyet(String monTienQuyet) { this.monTienQuyet.set(monTienQuyet == null ? "" : monTienQuyet); }
    public void setKhoa(Khoa khoa) { this.khoa.set(khoa); } // Setter cho Khoa

    // --- Property Methods ---
    public StringProperty maMonProperty() { return maMon; }
    public StringProperty tenMonProperty() { return tenMon; }
    public IntegerProperty soTinChiProperty() { return soTinChi; }
    public StringProperty loaiMonProperty() { return loaiMon; }
    public IntegerProperty hocKyProperty() { return hocKy; }
    public StringProperty monTienQuyetProperty() { return monTienQuyet; }
    public ObjectProperty<Khoa> khoaProperty() { return khoa; } // Property cho Khoa

    // --- equals() and hashCode() ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonHoc monHoc = (MonHoc) o;
        return Objects.equals(getMaMon(), monHoc.getMaMon());
    }
    @Override
    public int hashCode() { return Objects.hash(getMaMon()); }

     @Override
     public String toString() {
         String ten = getTenMon();
         return getMaMon() + (ten != null && !ten.isEmpty() ? " - " + ten : "");
     }
}