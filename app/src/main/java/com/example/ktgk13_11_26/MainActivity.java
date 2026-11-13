package com.example.ktgk13_11_26;

import android.content.Intent;
import android.os.Bundle;

/** Màn hình chính (menu): điều hướng tới 3 chức năng bằng Intent. */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar("Quản lý sân bóng", false);

        findViewById(R.id.btnDanhSachSan).setOnClickListener(v ->
                startActivity(new Intent(this, DanhSachSanActivity.class)));
        findViewById(R.id.btnDatSan).setOnClickListener(v ->
                startActivity(new Intent(this, DatSanActivity.class)));
        findViewById(R.id.btnDanhSachDon).setOnClickListener(v ->
                startActivity(new Intent(this, DanhSachDonActivity.class)));
    }
}
