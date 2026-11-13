package com.example.ktgk13_11_26;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.ktgk13_11_26.data.DatabaseHelper;
import com.example.ktgk13_11_26.model.SanBong;
import com.google.android.material.textfield.TextInputLayout;

/** Chức năng 1: form thêm sân bóng mới. */
public class ThemSanActivity extends BaseActivity {

    private TextInputLayout tilMa, tilTen, tilDiaChi, tilGia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_san);
        setupToolbar("Thêm sân bóng", true);

        tilMa = findViewById(R.id.tilMaSan);
        tilTen = findViewById(R.id.tilTenSan);
        tilDiaChi = findViewById(R.id.tilDiaChi);
        tilGia = findViewById(R.id.tilGiaThue);

        Utils.xoaLoiKhiNhap(tilMa, tilTen, tilDiaChi, tilGia);
        findViewById(R.id.btnLuu).setOnClickListener(v -> luuSan());
    }

    private String text(TextInputLayout til) {
        return til.getEditText() == null ? "" : til.getEditText().getText().toString().trim();
    }

    private void luuSan() {
        // Xóa lỗi cũ
        tilMa.setError(null);
        tilTen.setError(null);
        tilDiaChi.setError(null);
        tilGia.setError(null);

        String ma = text(tilMa).toUpperCase();
        String ten = text(tilTen);
        String diaChi = text(tilDiaChi);
        String giaStr = text(tilGia);
        DatabaseHelper db = DatabaseHelper.getInstance(this);

        boolean hopLe = true;
        if (TextUtils.isEmpty(ma)) {
            tilMa.setError("Vui lòng nhập mã sân");
            hopLe = false;
        } else if (db.tonTaiMaSan(ma)) {
            tilMa.setError("Mã sân đã tồn tại");
            hopLe = false;
        }
        if (TextUtils.isEmpty(ten)) {
            tilTen.setError("Vui lòng nhập tên sân");
            hopLe = false;
        }
        if (TextUtils.isEmpty(diaChi)) {
            tilDiaChi.setError("Vui lòng nhập địa chỉ");
            hopLe = false;
        }
        long gia = 0;
        if (TextUtils.isEmpty(giaStr)) {
            tilGia.setError("Vui lòng nhập giá thuê");
            hopLe = false;
        } else {
            try {
                gia = Long.parseLong(giaStr);
                if (gia <= 0) {
                    tilGia.setError("Giá thuê phải lớn hơn 0");
                    hopLe = false;
                }
            } catch (NumberFormatException e) {
                tilGia.setError("Giá thuê không hợp lệ");
                hopLe = false;
            }
        }
        if (!hopLe) return;

        if (db.themSan(new SanBong(ma, ten, diaChi, gia, SanBong.TRANG_THAI_TRONG))) {
            Toast.makeText(this, "Đã thêm sân " + ten, Toast.LENGTH_SHORT).show();
            finish();   // quay lại danh sách, onResume() bên đó sẽ tải lại
        } else {
            Toast.makeText(this, "Lưu thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
        }
    }
}
