package com.example.ktgk13_11_26.model;

/**
 * Model cho 1 đơn đặt sân (1 dòng trong bảng don_dat).
 * tenSan, diaChi, giaThue không lưu trong bảng don_dat mà được lấy bằng JOIN với bảng san_bong.
 */
public class DonDatSan {
    private int id;
    private String maSan;
    private String hoTen;
    private String soDienThoai;
    private String ngayThue;   // định dạng yyyy-MM-dd (dễ so sánh / sắp xếp)
    private int gioBatDau;     // số phút tính từ 00:00 (vd 18:30 -> 1110)
    private int soGio;
    private long tongTien;

    // Thông tin lấy kèm từ bảng san_bong (JOIN)
    private String tenSan;
    private String diaChi;
    private long giaThue;

    public DonDatSan(int id, String maSan, String hoTen, String soDienThoai,
                     String ngayThue, int gioBatDau, int soGio, long tongTien) {
        this.id = id;
        this.maSan = maSan;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.ngayThue = ngayThue;
        this.gioBatDau = gioBatDau;
        this.soGio = soGio;
        this.tongTien = tongTien;
    }

    public int getId() { return id; }
    public String getMaSan() { return maSan; }
    public String getHoTen() { return hoTen; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getNgayThue() { return ngayThue; }
    public int getGioBatDau() { return gioBatDau; }
    public int getSoGio() { return soGio; }
    public long getTongTien() { return tongTien; }
    public int getGioKetThuc() { return gioBatDau + soGio * 60; }

    public String getTenSan() { return tenSan; }
    public String getDiaChi() { return diaChi; }
    public long getGiaThue() { return giaThue; }

    public void setThongTinSan(String tenSan, String diaChi, long giaThue) {
        this.tenSan = tenSan;
        this.diaChi = diaChi;
        this.giaThue = giaThue;
    }
}
