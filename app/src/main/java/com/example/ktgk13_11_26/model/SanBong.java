package com.example.ktgk13_11_26.model;

/**
 * Model (lớp dữ liệu) cho 1 sân bóng.
 * Mỗi đối tượng SanBong tương ứng với 1 dòng trong bảng san_bong của SQLite.
 */
public class SanBong {
    public static final int TRANG_THAI_TRONG = 0;   // Còn trống
    public static final int TRANG_THAI_DA_DAT = 1;  // Đã được đặt

    private String maSan;
    private String tenSan;
    private String diaChi;
    private long giaThue;      // giá thuê theo giờ (VNĐ)
    private int trangThai;

    public SanBong(String maSan, String tenSan, String diaChi, long giaThue, int trangThai) {
        this.maSan = maSan;
        this.tenSan = tenSan;
        this.diaChi = diaChi;
        this.giaThue = giaThue;
        this.trangThai = trangThai;
    }

    public String getMaSan() { return maSan; }
    public String getTenSan() { return tenSan; }
    public String getDiaChi() { return diaChi; }
    public long getGiaThue() { return giaThue; }
    public int getTrangThai() { return trangThai; }

    public boolean isDaDat() { return trangThai == TRANG_THAI_DA_DAT; }

    public String getTenTrangThai() {
        return isDaDat() ? "Đã được đặt" : "Còn trống";
    }

    /** Spinner (ArrayAdapter) sẽ gọi toString() để hiển thị mỗi dòng. */
    @Override
    public String toString() {
        return maSan + " - " + tenSan + " (" + getTenTrangThai() + ")";
    }
}
