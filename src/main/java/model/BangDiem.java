package model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BangDiem {
    // Sử dụng Property để tương thích tốt hơn với JavaFX nếu cần
    private final StringProperty maSV;
    private final StringProperty maMon;
    private final FloatProperty diemQT;
    private final FloatProperty diemThi;
    private final FloatProperty diemTB;

    /**
     * Constructor đầy đủ (ví dụ: khi đọc từ CSDL bao gồm cả diemTB).
     */
    public BangDiem(String maSV, String maMon, float diemQT, float diemThi, float diemTB) {
        this.maSV = new SimpleStringProperty(maSV);
        this.maMon = new SimpleStringProperty(maMon);
        this.diemQT = new SimpleFloatProperty(diemQT);
        this.diemThi = new SimpleFloatProperty(diemThi);
        this.diemTB = new SimpleFloatProperty(diemTB);
    }

    /**
     * Constructor dùng khi cập nhật điểm (không cần truyền diemTB).
     * @param maSV 
     * @param maMon 
     * @param diemQT 
     * @param diemThi 
     */
    public BangDiem(String maSV, String maMon, float diemQT, float diemThi) {
        this.maSV = new SimpleStringProperty(maSV);
        this.maMon = new SimpleStringProperty(maMon);
        this.diemQT = new SimpleFloatProperty(diemQT);
        this.diemThi = new SimpleFloatProperty(diemThi);
        this.diemTB = new SimpleFloatProperty(); // Khởi tạo rỗng, vì CSDL sẽ tính
    }


    // --- Getters ---
    public String getMaSV() { return maSV.get(); }
    public String getMaMon() { return maMon.get(); }
    public float getDiemQT() { return diemQT.get(); }
    public float getDiemThi() { return diemThi.get(); }
    public float getDiemTB() { return diemTB.get(); } // Getter cho diemTB vẫn cần

    // --- Setters (Cần thiết nếu muốn sửa đổi đối tượng sau khi tạo) ---
    public void setDiemQT(float diemQT) { this.diemQT.set(diemQT); }
    public void setDiemThi(float diemThi) { this.diemThi.set(diemThi); }
    // Không cần setter cho diemTB vì nó được CSDL tính

    // --- Property Methods (Hữu ích cho JavaFX binding) ---
    public StringProperty maSVProperty() { return maSV; }
    public StringProperty maMonProperty() { return maMon; }
    public FloatProperty diemQTProperty() { return diemQT; }
    public FloatProperty diemThiProperty() { return diemThi; }
    public FloatProperty diemTBProperty() { return diemTB; }
}