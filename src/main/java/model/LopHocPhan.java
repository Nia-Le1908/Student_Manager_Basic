package model;

import javafx.beans.property.*;

public class LopHocPhan {
    private final IntegerProperty id;
    private final StringProperty maGV;
    private final StringProperty maMon;
    
    // Thêm ID kỳ học để map với DB
    private final IntegerProperty idKyHoc;
    // Giữ lại học kỳ và năm học để hiển thị (lấy từ JOIN)
    private final IntegerProperty hocKy;
    private final IntegerProperty namHoc;
    
    private final StringProperty maLopHanhChinh;
    private final StringProperty tenLopHocPhan;
    private final StringProperty loaiLop;
    private final StringProperty trangThai;
    private final StringProperty tenGV;
    private final StringProperty tenMon;
    private final StringProperty lyDoMoLop;

    public LopHocPhan(int id, String maGV, String maMon, int idKyHoc, int hocKy, int namHoc, 
                      String maLopHanhChinh, String tenLopHocPhan, String loaiLop, 
                      String trangThai, String tenGV, String tenMon, String lyDoMoLop) {
        this.id = new SimpleIntegerProperty(id);
        this.maGV = new SimpleStringProperty(maGV);
        this.maMon = new SimpleStringProperty(maMon);
        this.idKyHoc = new SimpleIntegerProperty(idKyHoc); // Mới
        this.hocKy = new SimpleIntegerProperty(hocKy);
        this.namHoc = new SimpleIntegerProperty(namHoc);
        this.maLopHanhChinh = new SimpleStringProperty(maLopHanhChinh);
        this.tenLopHocPhan = new SimpleStringProperty(tenLopHocPhan);
        this.loaiLop = new SimpleStringProperty(loaiLop);
        this.trangThai = new SimpleStringProperty(trangThai);
        this.tenGV = new SimpleStringProperty(tenGV);
        this.tenMon = new SimpleStringProperty(tenMon);
        this.lyDoMoLop = new SimpleStringProperty(lyDoMoLop);
    }

    public String getTenHienThi() {
        if ("NguyenVong".equalsIgnoreCase(getLoaiLop())) {
            return getTenLopHocPhan();
        } else {
            return getMaLopHanhChinh();
        }
    }

    public boolean isDaKhoaDiem() {
        return "DA_KHOA_DIEM".equals(getTrangThai());
    }
    // --- Getters ---
    public int getId() { return id.get(); }
    public String getMaGV() { return maGV.get(); }
    public String getMaMon() { return maMon.get(); }
    public int getIdKyHoc() { return idKyHoc.get(); } // Mới
    public int getHocKy() { return hocKy.get(); }
    public int getNamHoc() { return namHoc.get(); }
    public String getMaLopHanhChinh() { return maLopHanhChinh.get(); }
    public String getTenLopHocPhan() { return tenLopHocPhan.get(); }
    public String getLoaiLop() { return loaiLop.get(); }
    public String getTrangThai() { return trangThai.get(); }
    public String getTenGV() { return tenGV.get(); }
    public String getTenMon() { return tenMon.get(); }
    public String getLyDoMoLop() { return lyDoMoLop.get(); }

    // --- Property Getters ---
    public IntegerProperty idProperty() { return id; }
    public StringProperty maGVProperty() { return maGV; }
    public StringProperty maMonProperty() { return maMon; }
    public IntegerProperty hocKyProperty() { return hocKy; }
    public IntegerProperty namHocProperty() { return namHoc; }
    public StringProperty maLopHanhChinhProperty() { return maLopHanhChinh; }
    public StringProperty tenLopHocPhanProperty() { return tenLopHocPhan; }
    public StringProperty loaiLopProperty() { return loaiLop; }
    public StringProperty trangThaiProperty() { return trangThai; }
    public StringProperty tenGVProperty() { return tenGV; }
    public StringProperty tenMonProperty() { return tenMon; }
    public StringProperty lyDoMoLopProperty() { return lyDoMoLop; }
    
    public void setTrangThai(String status) { this.trangThai.set(status); }
}