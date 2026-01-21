package model;

import javafx.beans.property.*;

public class KetQuaHocTap {
    private final IntegerProperty id;
    private final StringProperty maSV;
    private final StringProperty hoTenSV; 
    private final StringProperty maMon;
    private final StringProperty tenMon;  
    @SuppressWarnings("unused")
    private final IntegerProperty soTinChi;
    @SuppressWarnings("unused")
    private final IntegerProperty hocKy;
    @SuppressWarnings("unused")
    private final IntegerProperty namHoc;
    private final FloatProperty diemQT;
    private final FloatProperty diemThi;
    private final FloatProperty diemTB;
    @SuppressWarnings("unused")
    private final StringProperty trangThai;

    public KetQuaHocTap(int id, String maSV, String hoTenSV, String maMon, String tenMon, 
                        int soTinChi, int hocKy, int namHoc, 
                        Float diemQT, Float diemThi, Float diemTB, String trangThai) {
        this.id = new SimpleIntegerProperty(id);
        this.maSV = new SimpleStringProperty(maSV);
        this.hoTenSV = new SimpleStringProperty(hoTenSV);
        this.maMon = new SimpleStringProperty(maMon);
        this.tenMon = new SimpleStringProperty(tenMon);
        this.soTinChi = new SimpleIntegerProperty(soTinChi);
        this.hocKy = new SimpleIntegerProperty(hocKy);
        this.namHoc = new SimpleIntegerProperty(namHoc);
        this.diemQT = new SimpleFloatProperty(diemQT != null ? diemQT : 0.0f);
        this.diemThi = new SimpleFloatProperty(diemThi != null ? diemThi : 0.0f);
        this.diemTB = new SimpleFloatProperty(diemTB != null ? diemTB : 0.0f);
        this.trangThai = new SimpleStringProperty(trangThai);
    }

    // Getters
    public int getId() { return id.get(); }
    public String getMaSV() { return maSV.get(); }
    public String getHoTenSV() { return hoTenSV.get(); }
    public String getMaMon() { return maMon.get(); }
    public String getTenMon() { return tenMon.get(); }
    public float getDiemQT() { return diemQT.get(); }
    public float getDiemThi() { return diemThi.get(); }
    public float getDiemTB() { return diemTB.get(); }
    
    // Setters (Cho việc cập nhật trên UI)
    public void setDiemQT(float diem) { this.diemQT.set(diem); }
    public void setDiemThi(float diem) { this.diemThi.set(diem); }
    public void setDiemTB(float diem) { this.diemTB.set(diem); } // Cột này thường readonly do DB tự tính

    // Property Methods
    public StringProperty maSVProperty() { return maSV; }
    public StringProperty hoTenSVProperty() { return hoTenSV; }
    public FloatProperty diemQTProperty() { return diemQT; }
    public FloatProperty diemThiProperty() { return diemThi; }
    public FloatProperty diemTBProperty() { return diemTB; }
}