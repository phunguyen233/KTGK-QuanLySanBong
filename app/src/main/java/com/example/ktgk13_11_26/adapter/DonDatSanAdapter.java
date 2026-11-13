package com.example.ktgk13_11_26.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.ktgk13_11_26.R;
import com.example.ktgk13_11_26.Utils;
import com.example.ktgk13_11_26.model.DonDatSan;

import java.util.ArrayList;
import java.util.List;

/** Adapter hiển thị danh sách đơn đặt sân (item_don_dat.xml). */
public class DonDatSanAdapter extends BaseAdapter {

    private final Context context;
    private final List<DonDatSan> ds = new ArrayList<>();

    public DonDatSanAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<DonDatSan> moi) {
        ds.clear();
        ds.addAll(moi);
        notifyDataSetChanged();
    }

    @Override public int getCount() { return ds.size(); }
    @Override public DonDatSan getItem(int position) { return ds.get(position); }
    @Override public long getItemId(int position) { return ds.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_don_dat, parent, false);
        }
        DonDatSan don = getItem(position);

        ((TextView) convertView.findViewById(R.id.tvNguoiDat)).setText(don.getHoTen());
        ((TextView) convertView.findViewById(R.id.tvTenSan)).setText("Sân: " + don.getTenSan());
        ((TextView) convertView.findViewById(R.id.tvNgayGio)).setText(
                "Ngày " + Utils.formatNgay(don.getNgayThue())
                        + " • Bắt đầu " + Utils.formatGio(don.getGioBatDau())
                        + " • " + don.getSoGio() + " giờ");
        ((TextView) convertView.findViewById(R.id.tvTongTien)).setText(
                "Tổng tiền: " + Utils.formatTien(don.getTongTien()));
        return convertView;
    }
}
