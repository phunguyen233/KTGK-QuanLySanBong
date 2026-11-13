package com.example.ktgk13_11_26.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ktgk13_11_26.model.DonDatSan;
import com.example.ktgk13_11_26.model.SanBong;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp duy nhất làm việc với SQLite. Mọi màn hình (Activity) đều gọi qua lớp này
 * để đọc / ghi dữ liệu -> dữ liệu nằm trong file database trên máy nên
 * tắt app mở lại vẫn còn.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "quanlysanbong.db";
    private static final int DB_VERSION = 1;

    // Bảng sân bóng
    private static final String T_SAN = "san_bong";
    private static final String C_MA_SAN = "ma_san";
    private static final String C_TEN_SAN = "ten_san";
    private static final String C_DIA_CHI = "dia_chi";
    private static final String C_GIA_THUE = "gia_thue";
    private static final String C_TRANG_THAI = "trang_thai";

    // Bảng đơn đặt sân
    private static final String T_DON = "don_dat";
    private static final String C_ID = "id";
    private static final String C_HO_TEN = "ho_ten";
    private static final String C_SDT = "so_dien_thoai";
    private static final String C_NGAY = "ngay_thue";
    private static final String C_GIO_BD = "gio_bat_dau";
    private static final String C_SO_GIO = "so_gio";
    private static final String C_TONG_TIEN = "tong_tien";

    // Singleton: cả app dùng chung 1 đối tượng DatabaseHelper
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /** Chỉ chạy 1 lần duy nhất khi file database chưa tồn tại (lần đầu cài app). */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_SAN + " ("
                + C_MA_SAN + " TEXT PRIMARY KEY, "
                + C_TEN_SAN + " TEXT NOT NULL, "
                + C_DIA_CHI + " TEXT NOT NULL, "
                + C_GIA_THUE + " INTEGER NOT NULL, "
                + C_TRANG_THAI + " INTEGER NOT NULL DEFAULT 0)");

        db.execSQL("CREATE TABLE " + T_DON + " ("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + C_MA_SAN + " TEXT NOT NULL, "
                + C_HO_TEN + " TEXT NOT NULL, "
                + C_SDT + " TEXT NOT NULL, "
                + C_NGAY + " TEXT NOT NULL, "
                + C_GIO_BD + " INTEGER NOT NULL, "
                + C_SO_GIO + " INTEGER NOT NULL, "
                + C_TONG_TIEN + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + C_MA_SAN + ") REFERENCES " + T_SAN + "(" + C_MA_SAN + "))");

        // Dữ liệu mẫu để chạy thử
        themSan(db, new SanBong("S01", "Sân mini số 1", "12 Nguyễn Trãi, Hà Đông", 200000, 0));
        themSan(db, new SanBong("S02", "Sân mini số 2", "12 Nguyễn Trãi, Hà Đông", 250000, 0));
        themSan(db, new SanBong("S03", "Sân 11 người", "96A Trần Phú, Hà Đông", 800000, 0));
    }

    /** Chạy khi tăng DB_VERSION. Ở đây đơn giản là xóa bảng cũ và tạo lại. */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_DON);
        db.execSQL("DROP TABLE IF EXISTS " + T_SAN);
        onCreate(db);
    }

    // =====================================================================
    //                              SÂN BÓNG
    // =====================================================================

    private void themSan(SQLiteDatabase db, SanBong san) {
        ContentValues v = new ContentValues();
        v.put(C_MA_SAN, san.getMaSan());
        v.put(C_TEN_SAN, san.getTenSan());
        v.put(C_DIA_CHI, san.getDiaChi());
        v.put(C_GIA_THUE, san.getGiaThue());
        v.put(C_TRANG_THAI, san.getTrangThai());
        db.insert(T_SAN, null, v);
    }

    /** @return true nếu thêm thành công */
    public boolean themSan(SanBong san) {
        ContentValues v = new ContentValues();
        v.put(C_MA_SAN, san.getMaSan());
        v.put(C_TEN_SAN, san.getTenSan());
        v.put(C_DIA_CHI, san.getDiaChi());
        v.put(C_GIA_THUE, san.getGiaThue());
        v.put(C_TRANG_THAI, SanBong.TRANG_THAI_TRONG);
        return getWritableDatabase().insert(T_SAN, null, v) != -1;
    }

    public boolean tonTaiMaSan(String maSan) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT 1 FROM " + T_SAN + " WHERE " + C_MA_SAN + " = ? COLLATE NOCASE",
                new String[]{maSan})) {
            return c.moveToFirst();
        }
    }

    public List<SanBong> layDanhSachSan() {
        List<SanBong> ds = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM " + T_SAN + " ORDER BY " + C_MA_SAN, null)) {
            while (c.moveToNext()) {
                ds.add(docSan(c));
            }
        }
        return ds;
    }

    public SanBong laySan(String maSan) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT * FROM " + T_SAN + " WHERE " + C_MA_SAN + " = ?", new String[]{maSan})) {
            return c.moveToFirst() ? docSan(c) : null;
        }
    }

    private SanBong docSan(Cursor c) {
        return new SanBong(
                c.getString(c.getColumnIndexOrThrow(C_MA_SAN)),
                c.getString(c.getColumnIndexOrThrow(C_TEN_SAN)),
                c.getString(c.getColumnIndexOrThrow(C_DIA_CHI)),
                c.getLong(c.getColumnIndexOrThrow(C_GIA_THUE)),
                c.getInt(c.getColumnIndexOrThrow(C_TRANG_THAI)));
    }

    /** Số đơn đặt hiện có của 1 sân. */
    public int demDonCuaSan(String maSan) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + T_DON + " WHERE " + C_MA_SAN + " = ?",
                new String[]{maSan})) {
            c.moveToFirst();
            return c.getInt(0);
        }
    }

    public boolean xoaSan(String maSan) {
        return getWritableDatabase().delete(T_SAN, C_MA_SAN + " = ?", new String[]{maSan}) > 0;
    }

    /**
     * Tính lại trạng thái sân: có ít nhất 1 đơn đặt -> "Đã được đặt", không còn đơn -> "Còn trống".
     * Được gọi mỗi khi thêm đơn hoặc hủy đơn.
     */
    public void capNhatTrangThaiSan(String maSan) {
        int trangThai = demDonCuaSan(maSan) > 0 ? SanBong.TRANG_THAI_DA_DAT : SanBong.TRANG_THAI_TRONG;
        ContentValues v = new ContentValues();
        v.put(C_TRANG_THAI, trangThai);
        getWritableDatabase().update(T_SAN, v, C_MA_SAN + " = ?", new String[]{maSan});
    }

    // =====================================================================
    //                            ĐƠN ĐẶT SÂN
    // =====================================================================

    /**
     * Kiểm tra sân đã có lịch trùng thời gian chưa.
     * Hai khoảng [bd1, kt1) và [bd2, kt2) giao nhau khi: bd1 < kt2 VÀ bd2 < kt1.
     */
    public boolean biTrungLich(String maSan, String ngay, int gioBatDau, int soGio) {
        int gioKetThuc = gioBatDau + soGio * 60;
        String sql = "SELECT COUNT(*) FROM " + T_DON
                + " WHERE " + C_MA_SAN + " = ? AND " + C_NGAY + " = ?"
                // Lưu ý: rawQuery truyền tham số dạng chuỗi (TEXT) -> phải CAST sang số,
                // nếu không SQLite sẽ so sánh số với chuỗi và cho kết quả sai.
                + " AND " + C_GIO_BD + " < CAST(? AS INTEGER)"
                + " AND (" + C_GIO_BD + " + " + C_SO_GIO + " * 60) > CAST(? AS INTEGER)";
        try (Cursor c = getReadableDatabase().rawQuery(sql, new String[]{
                maSan, ngay, String.valueOf(gioKetThuc), String.valueOf(gioBatDau)})) {
            c.moveToFirst();
            return c.getInt(0) > 0;
        }
    }

    /** Thêm đơn rồi cập nhật trạng thái sân. @return id đơn mới, -1 nếu lỗi */
    public long themDon(DonDatSan don) {
        ContentValues v = new ContentValues();
        v.put(C_MA_SAN, don.getMaSan());
        v.put(C_HO_TEN, don.getHoTen());
        v.put(C_SDT, don.getSoDienThoai());
        v.put(C_NGAY, don.getNgayThue());
        v.put(C_GIO_BD, don.getGioBatDau());
        v.put(C_SO_GIO, don.getSoGio());
        v.put(C_TONG_TIEN, don.getTongTien());
        long id = getWritableDatabase().insert(T_DON, null, v);
        if (id != -1) {
            capNhatTrangThaiSan(don.getMaSan());
        }
        return id;
    }

    /** Hủy (xóa) đơn rồi cập nhật lại trạng thái sân tương ứng. */
    public boolean huyDon(int id) {
        DonDatSan don = layDon(id);
        if (don == null) return false;
        boolean ok = getWritableDatabase().delete(T_DON, C_ID + " = ?",
                new String[]{String.valueOf(id)}) > 0;
        if (ok) {
            capNhatTrangThaiSan(don.getMaSan());
        }
        return ok;
    }

    private static final String SELECT_DON_JOIN_SAN =
            "SELECT d.*, s." + C_TEN_SAN + ", s." + C_DIA_CHI + ", s." + C_GIA_THUE
                    + " FROM " + T_DON + " d LEFT JOIN " + T_SAN + " s ON d." + C_MA_SAN + " = s." + C_MA_SAN;

    public List<DonDatSan> layDanhSachDon() {
        List<DonDatSan> ds = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(SELECT_DON_JOIN_SAN
                + " ORDER BY d." + C_NGAY + " DESC, d." + C_GIO_BD + " ASC", null)) {
            while (c.moveToNext()) {
                ds.add(docDon(c));
            }
        }
        return ds;
    }

    public DonDatSan layDon(int id) {
        try (Cursor c = getReadableDatabase().rawQuery(SELECT_DON_JOIN_SAN
                + " WHERE d." + C_ID + " = ?", new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? docDon(c) : null;
        }
    }

    private DonDatSan docDon(Cursor c) {
        DonDatSan don = new DonDatSan(
                c.getInt(c.getColumnIndexOrThrow(C_ID)),
                c.getString(c.getColumnIndexOrThrow(C_MA_SAN)),
                c.getString(c.getColumnIndexOrThrow(C_HO_TEN)),
                c.getString(c.getColumnIndexOrThrow(C_SDT)),
                c.getString(c.getColumnIndexOrThrow(C_NGAY)),
                c.getInt(c.getColumnIndexOrThrow(C_GIO_BD)),
                c.getInt(c.getColumnIndexOrThrow(C_SO_GIO)),
                c.getLong(c.getColumnIndexOrThrow(C_TONG_TIEN)));
        don.setThongTinSan(
                c.getString(c.getColumnIndexOrThrow(C_TEN_SAN)),
                c.getString(c.getColumnIndexOrThrow(C_DIA_CHI)),
                c.getLong(c.getColumnIndexOrThrow(C_GIA_THUE)));
        return don;
    }
}
