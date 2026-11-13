package com.example.ktgk13_11_26;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ktgk13_11_26.data.DatabaseHelper;
import com.example.ktgk13_11_26.model.DonDatSan;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Chức năng 3: xem chi tiết đơn và hủy đơn. Nhận id đơn qua Intent. */
public class ChiTietDonActivity extends BaseActivity {

    public static final String EXTRA_DON_ID = "extra_don_id";

    private DatabaseHelper db;
    private DonDatSan don;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_don);
        setupToolbar("Chi tiết đơn đặt sân", true);

        db = DatabaseHelper.getInstance(this);
        int id = getIntent().getIntExtra(EXTRA_DON_ID, -1);
        don = db.layDon(id);
        if (don == null) {
            Toast.makeText(this, "Không tìm thấy đơn đặt sân", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setText(R.id.tvMaDon, "#" + don.getId());
        setText(R.id.tvNguoiDat, don.getHoTen());
        setText(R.id.tvSdt, don.getSoDienThoai());
        setText(R.id.tvSan, don.getMaSan() + " - " + don.getTenSan());
        setText(R.id.tvDiaChi, don.getDiaChi());
        setText(R.id.tvNgay, Utils.formatNgay(don.getNgayThue()));
        setText(R.id.tvGio, Utils.formatGio(don.getGioBatDau()) + " - " + Utils.formatGio(don.getGioKetThuc()));
        setText(R.id.tvSoGio, don.getSoGio() + " giờ");
        setText(R.id.tvGiaThue, Utils.formatTien(don.getGiaThue()) + "/giờ");
        setText(R.id.tvTongTien, Utils.formatTien(don.getTongTien()));

        findViewById(R.id.btnHuyDon).setOnClickListener(v -> xacNhanHuy());
    }

    private void setText(int viewId, String value) {
        ((TextView) findViewById(viewId)).setText(value);
    }

    private void xacNhanHuy() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Hủy đơn đặt sân")
                .setMessage("Bạn có chắc muốn hủy đơn của " + don.getHoTen() + "?")
                .setNegativeButton("Không", null)
                .setPositiveButton("Hủy đơn", (d, w) -> {
                    if (db.huyDon(don.getId())) {   // xóa đơn + cập nhật trạng thái sân
                        Toast.makeText(this, "Đã hủy đơn", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Hủy đơn thất bại", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
