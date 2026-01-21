package model;

import javafx.beans.property.*;

/**
 * Lớp đại diện cho thông tin chi tiết đăng ký môn học (dùng cho Admin xem xét).
 */
public class DangKyChiTiet {
    private final StringProperty maGV;
    private final StringProperty tenGV;
    private final StringProperty maMon;
    private final StringProperty tenMon;
    private final StringProperty tenKhoa; // Khoa của môn học
    private final StringProperty trangThai;

    public DangKyChiTiet(String maGV, String tenGV, String maMon, String tenMon, String tenKhoa, String trangThai) {
        this.maGV = new SimpleStringProperty(maGV);
        this.tenGV = new SimpleStringProperty(tenGV);
        this.maMon = new SimpleStringProperty(maMon);
        this.tenMon = new SimpleStringProperty(tenMon);
        this.tenKhoa = new SimpleStringProperty(tenKhoa != null ? tenKhoa : "N/A");
        this.trangThai = new SimpleStringProperty(trangThai);
    }

    // --- Getters ---
    public String getMaGV() { return maGV.get(); }
    public String getTenGV() { return tenGV.get(); }
    public String getMaMon() { return maMon.get(); }
    public String getTenMon() { return tenMon.get(); }
    public String getTenKhoa() { return tenKhoa.get(); }
    public String getTrangThai() { return trangThai.get(); }

    // --- Property Methods ---
    public StringProperty maGVProperty() { return maGV; }
    public StringProperty tenGVProperty() { return tenGV; }
    public StringProperty maMonProperty() { return maMon; }
    public StringProperty tenMonProperty() { return tenMon; }
    public StringProperty tenKhoaProperty() { return tenKhoa; }
    public StringProperty trangThaiProperty() { return trangThai; }

    // Setters (nếu cần cập nhật UI)
    public void setTrangThai(String trangThai) { this.trangThai.set(trangThai); }
}
