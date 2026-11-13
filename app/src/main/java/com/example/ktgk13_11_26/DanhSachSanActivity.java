package com.example.ktgk13_11_26;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ktgk13_11_26.adapter.SanBongAdapter;
import com.example.ktgk13_11_26.data.DatabaseHelper;
import com.example.ktgk13_11_26.model.SanBong;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Chức năng 1: hiển thị danh sách sân, thêm sân, xóa sân. */
public class DanhSachSanActivity extends BaseActivity {

    private DatabaseHelper db;
    private SanBongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_san);
        setupToolbar("Danh sách sân bóng", true);

        db = DatabaseHelper.getInstance(this);

        ListView lv = findViewById(R.id.lvSan);
        lv.setEmptyView(findViewById(R.id.tvTrong));
        adapter = new SanBongAdapter(this, this::xacNhanXoa);
        lv.setAdapter(adapter);

        // Bấm vào 1 sân -> mở màn hình đặt sân, truyền mã sân qua Intent
        lv.setOnItemClickListener((parent, view, position, id) -> {
            SanBong san = adapter.getItem(position);
            Intent intent = new Intent(this, DatSanActivity.class);
            intent.putExtra(DatSanActivity.EXTRA_MA_SAN, san.getMaSan());
            startActivity(intent);
        });

        findViewById(R.id.fabThemSan).setOnClickListener(v ->
                startActivity(new Intent(this, ThemSanActivity.class)));
    }

    /** onResume chạy mỗi khi màn hình hiện lại (vd sau khi thêm sân xong) -> tải lại dữ liệu. */
    @Override
    protected void onResume() {
        super.onResume();
        adapter.setData(db.layDanhSachSan());
    }

    private void xacNhanXoa(SanBong san) {
        int soDon = db.demDonCuaSan(san.getMaSan());
        if (soDon > 0) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Không thể xóa")
                    .setMessage("Sân \"" + san.getTenSan() + "\" đang có " + soDon
                            + " đơn đặt. Hãy hủy các đơn này trước khi xóa sân.")
                    .setPositiveButton("Đóng", null)
                    .show();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa sân")
                .setMessage("Bạn có chắc muốn xóa sân \"" + san.getTenSan() + "\"?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> {
                    if (db.xoaSan(san.getMaSan())) {
                        Toast.makeText(this, "Đã xóa sân", Toast.LENGTH_SHORT).show();
                        adapter.setData(db.layDanhSachSan());
                    }
                })
                .show();
    }
}
