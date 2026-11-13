package com.example.ktgk13_11_26.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.ktgk13_11_26.R;
import com.example.ktgk13_11_26.Utils;
import com.example.ktgk13_11_26.model.SanBong;

import java.util.ArrayList;
import java.util.List;

/** Adapter: "cầu nối" biến List<SanBong> thành từng dòng giao diện (item_san_bong.xml) trong ListView. */
public class SanBongAdapter extends BaseAdapter {

    public interface OnXoaListener {
        void onXoa(SanBong san);
    }

    private final Context context;
    private final List<SanBong> ds = new ArrayList<>();
    private final OnXoaListener xoaListener;

    public SanBongAdapter(Context context, OnXoaListener xoaListener) {
        this.context = context;
        this.xoaListener = xoaListener;
    }

    public void setData(List<SanBong> moi) {
        ds.clear();
        ds.addAll(moi);
        notifyDataSetChanged();   // báo ListView vẽ lại
    }

    @Override public int getCount() { return ds.size(); }
    @Override public SanBong getItem(int position) { return ds.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_san_bong, parent, false);
        }
        SanBong san = getItem(position);

        TextView tvTen = convertView.findViewById(R.id.tvTenSan);
        TextView tvMa = convertView.findViewById(R.id.tvMaSan);
        TextView tvDiaChi = convertView.findViewById(R.id.tvDiaChi);
        TextView tvGia = convertView.findViewById(R.id.tvGiaThue);
        TextView tvTrangThai = convertView.findViewById(R.id.tvTrangThai);
        ImageButton btnXoa = convertView.findViewById(R.id.btnXoa);

        tvTen.setText(san.getTenSan());
        tvMa.setText("Mã sân: " + san.getMaSan());
        tvDiaChi.setText("Địa chỉ: " + san.getDiaChi());
        tvGia.setText("Giá thuê: " + Utils.formatTien(san.getGiaThue()) + "/giờ");
        tvTrangThai.setText(san.getTenTrangThai());
        tvTrangThai.setTextColor(ContextCompat.getColor(context,
                san.isDaDat() ? R.color.trang_thai_da_dat : R.color.trang_thai_trong));

        btnXoa.setOnClickListener(v -> xoaListener.onXoa(san));
        return convertView;
    }
}
