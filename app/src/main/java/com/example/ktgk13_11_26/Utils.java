package com.example.ktgk13_11_26;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.Locale;

/** Các hàm định dạng dùng chung cho nhiều màn hình. */
public final class Utils {
    private Utils() {}

    /** 250000 -> "250.000 đ" */
    public static String formatTien(long tien) {
        return NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(tien) + " đ";
    }

    /** 1110 phút -> "18:30" */
    public static String formatGio(int phut) {
        return String.format(Locale.US, "%02d:%02d", phut / 60, phut % 60);
    }

    /** "2026-11-13" -> "13/11/2026" */
    public static String formatNgay(String yyyyMMdd) {
        String[] p = yyyyMMdd.split("-");
        if (p.length != 3) return yyyyMMdd;
        return p[2] + "/" + p[1] + "/" + p[0];
    }

    /** Khi người dùng gõ lại vào ô đang báo lỗi -> tự xóa thông báo lỗi. */
    public static void xoaLoiKhiNhap(TextInputLayout... ds) {
        for (TextInputLayout til : ds) {
            if (til.getEditText() == null) continue;
            til.getEditText().addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
                @Override public void afterTextChanged(Editable s) { til.setError(null); }
            });
        }
    }
}
