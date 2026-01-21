package model;

import java.sql.Timestamp;

public class KyHoc {
    private int id;
    private int hocKy;
    private int namHoc;
    private Timestamp ngayBatDauNhapDiem;
    private Timestamp ngayKetThucNhapDiem;

    public KyHoc(int id, int hocKy, int namHoc, Timestamp ngayBatDauNhapDiem, Timestamp ngayKetThucNhapDiem) {
        this.id = id;
        this.hocKy = hocKy;
        this.namHoc = namHoc;
        this.ngayBatDauNhapDiem = ngayBatDauNhapDiem;
        this.ngayKetThucNhapDiem = ngayKetThucNhapDiem;
    }

    public boolean isTrongThoiGianNhapDiem() {
        long now = System.currentTimeMillis();
        return now >= ngayBatDauNhapDiem.getTime() && now <= ngayKetThucNhapDiem.getTime();
    }

    // Getters & Setters
    public int getId() { return id; }
    public int getHocKy() { return hocKy; }
    public int getNamHoc() { return namHoc; }
    public Timestamp getNgayBatDauNhapDiem() { return ngayBatDauNhapDiem; } // Thêm getter này
    public Timestamp getNgayKetThucNhapDiem() { return ngayKetThucNhapDiem; }

    @Override
    public String toString() {
        return "Học kỳ " + hocKy + " - Năm " + namHoc;
    }
    

}