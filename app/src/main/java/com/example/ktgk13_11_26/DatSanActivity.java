package com.example.ktgk13_11_26;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ktgk13_11_26.data.DatabaseHelper;
import com.example.ktgk13_11_26.model.DonDatSan;
import com.example.ktgk13_11_26.model.SanBong;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Chức năng 2: chọn sân, nhập thông tin, chọn ngày giờ, tính tiền, kiểm tra trùng lịch, lưu đơn. */
public class DatSanActivity extends BaseActivity {

    /** Key để màn hình khác truyền sẵn mã sân qua Intent. */
    public static final String EXTRA_MA_SAN = "extra_ma_san";

    private DatabaseHelper db;
    private List<SanBong> dsSan;

    private Spinner spSan;
    private TextInputLayout tilHoTen, tilSdt, tilSoGio;
    private Button btnNgay, btnGio;
    private TextView tvGiaThue, tvTongTien;

    private String ngayChon = null;   // yyyy-MM-dd, null = chưa chọn
    private int gioChon = -1;         // số phút từ 00:00, -1 = chưa chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dat_san);
        setupToolbar("Đặt sân", true);

        db = DatabaseHelper.getInstance(this);
        spSan = findViewById(R.id.spSan);
        tilHoTen = findViewById(R.id.tilHoTen);
        tilSdt = findViewById(R.id.tilSdt);
        tilSoGio = findViewById(R.id.tilSoGio);
        btnNgay = findViewById(R.id.btnChonNgay);
        btnGio = findViewById(R.id.btnChonGio);
        tvGiaThue = findViewById(R.id.tvGiaThue);
        tvTongTien = findViewById(R.id.tvTongTien);

        // 1. Đổ danh sách sân vào Spinner
        dsSan = db.layDanhSachSan();
        ArrayAdapter<SanBong> spAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dsSan);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSan.setAdapter(spAdapter);

        if (dsSan.isEmpty()) {
            Toast.makeText(this, "Chưa có sân nào, hãy thêm sân trước", Toast.LENGTH_LONG).show();
            findViewById(R.id.btnDatSan).setEnabled(false);
        }

        // 2. Nếu được mở từ danh sách sân -> chọn sẵn sân đó (dữ liệu nhận qua Intent)
        String maSanTruyenVao = getIntent().getStringExtra(EXTRA_MA_SAN);
        if (maSanTruyenVao != null) {
            for (int i = 0; i < dsSan.size(); i++) {
                if (dsSan.get(i).getMaSan().equals(maSanTruyenVao)) {
                    spSan.setSelection(i);
                    break;
                }
            }
        }

        // 3. Mỗi khi đổi sân hoặc đổi số giờ -> tính lại tổng tiền
        spSan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tinhTongTien();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        if (tilSoGio.getEditText() != null) {
            tilSoGio.getEditText().addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
                @Override public void afterTextChanged(Editable s) { tinhTongTien(); }
            });
        }

        Utils.xoaLoiKhiNhap(tilHoTen, tilSdt, tilSoGio);
        btnNgay.setOnClickListener(v -> chonNgay());
        btnGio.setOnClickListener(v -> chonGio());
        findViewById(R.id.btnDatSan).setOnClickListener(v -> luuDon());

        tinhTongTien();
    }

    private SanBong sanDangChon() {
        Object o = spSan.getSelectedItem();
        return o instanceof SanBong ? (SanBong) o : null;
    }

    private String text(TextInputLayout til) {
        return til.getEditText() == null ? "" : til.getEditText().getText().toString().trim();
    }

    /** Đọc số giờ, trả về -1 nếu rỗng / không phải số. */
    private int docSoGio() {
        try {
            return Integer.parseInt(text(tilSoGio));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Tổng tiền = Giá thuê theo giờ × Số giờ thuê */
    private void tinhTongTien() {
        SanBong san = sanDangChon();
        int soGio = docSoGio();
        if (san == null) {
            tvGiaThue.setText("Giá thuê: --");
            tvTongTien.setText("Tổng tiền: 0 đ");
            return;
        }
        tvGiaThue.setText("Giá thuê: " + Utils.formatTien(san.getGiaThue()) + "/giờ");
        long tong = soGio > 0 ? san.getGiaThue() * soGio : 0;
        tvTongTien.setText("Tổng tiền: " + Utils.formatTien(tong));
    }

    private void chonNgay() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            ngayChon = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            btnNgay.setText("Ngày thuê: " + Utils.formatNgay(ngayChon));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        // Không cho chọn ngày trong quá khứ
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void chonGio() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, h, m) -> {
            gioChon = h * 60 + m;
            btnGio.setText("Giờ bắt đầu: " + Utils.formatGio(gioChon));
        }, c.get(Calendar.HOUR_OF_DAY), 0, true).show();
    }

    private String homNay() {
        Calendar c = Calendar.getInstance();
        return String.format(Locale.US, "%04d-%02d-%02d",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    private void luuDon() {
        tilHoTen.setError(null);
        tilSdt.setError(null);
        tilSoGio.setError(null);

        SanBong san = sanDangChon();
        String hoTen = text(tilHoTen);
        String sdt = text(tilSdt);
        int soGio = docSoGio();

        // ---------- Kiểm tra dữ liệu bắt buộc / hợp lệ ----------
        if (san == null) {
            Toast.makeText(this, "Vui lòng chọn sân", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean hopLe = true;
        if (TextUtils.isEmpty(hoTen)) {
            tilHoTen.setError("Vui lòng nhập họ tên");
            hopLe = false;
        }
        if (TextUtils.isEmpty(sdt)) {
            tilSdt.setError("Vui lòng nhập số điện thoại");
            hopLe = false;
        } else if (!sdt.matches("^0\\d{9}$")) {
            tilSdt.setError("Số điện thoại phải gồm 10 chữ số, bắt đầu bằng 0");
            hopLe = false;
        }
        if (soGio <= 0) {
            tilSoGio.setError("Số giờ thuê phải là số nguyên lớn hơn 0");
            hopLe = false;
        }
        if (!hopLe) return;

        if (ngayChon == null) {
            Toast.makeText(this, "Vui lòng chọn ngày thuê", Toast.LENGTH_SHORT).show();
            return;
        }
        if (gioChon < 0) {
            Toast.makeText(this, "Vui lòng chọn giờ bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (gioChon + soGio * 60 > 24 * 60) {
            tilSoGio.setError("Giờ kết thúc vượt quá 24:00 trong ngày");
            return;
        }
        if (ngayChon.equals(homNay())) {
            Calendar now = Calendar.getInstance();
            int phutHienTai = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            if (gioChon < phutHienTai) {
                Toast.makeText(this, "Giờ bắt đầu đã qua, vui lòng chọn giờ khác", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // ---------- Kiểm tra trùng lịch ----------
        if (db.biTrungLich(san.getMaSan(), ngayChon, gioChon, soGio)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Trùng lịch")
                    .setMessage(san.getTenSan() + " đã có người đặt trong khoảng "
                            + Utils.formatGio(gioChon) + " - " + Utils.formatGio(gioChon + soGio * 60)
                            + " ngày " + Utils.formatNgay(ngayChon) + ".\nVui lòng chọn thời gian khác.")
                    .setPositiveButton("Đóng", null)
                    .show();
            return;
        }

        // ---------- Lưu ----------
        long tongTien = san.getGiaThue() * soGio;
        DonDatSan don = new DonDatSan(0, san.getMaSan(), hoTen, sdt, ngayChon, gioChon, soGio, tongTien);
        if (db.themDon(don) != -1) {
            Toast.makeText(this, "Đặt sân thành công! Tổng tiền " + Utils.formatTien(tongTien),
                    Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Lưu đơn thất bại", Toast.LENGTH_SHORT).show();
        }
    }
}
