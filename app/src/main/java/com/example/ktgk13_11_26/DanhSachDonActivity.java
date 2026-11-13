package com.example.ktgk13_11_26;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.example.ktgk13_11_26.adapter.DonDatSanAdapter;
import com.example.ktgk13_11_26.data.DatabaseHelper;

/** Chức năng 3: danh sách đơn đặt sân. Bấm 1 đơn -> xem chi tiết. */
public class DanhSachDonActivity extends BaseActivity {

    private DatabaseHelper db;
    private DonDatSanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_don);
        setupToolbar("Đơn đặt sân", true);

        db = DatabaseHelper.getInstance(this);
        ListView lv = findViewById(R.id.lvDon);
        lv.setEmptyView(findViewById(R.id.tvTrong));
        adapter = new DonDatSanAdapter(this);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, ChiTietDonActivity.class);
            intent.putExtra(ChiTietDonActivity.EXTRA_DON_ID, adapter.getItem(position).getId());
            startActivity(intent);
        });

        findViewById(R.id.fabDatSan).setOnClickListener(v ->
                startActivity(new Intent(this, DatSanActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setData(db.layDanhSachDon());   // tải lại sau khi đặt / hủy đơn
    }
}
