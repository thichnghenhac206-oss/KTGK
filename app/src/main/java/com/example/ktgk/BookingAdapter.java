package com.example.ktgk;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class BookingAdapter extends BaseAdapter {
    private final Context context;
    private final List<Booking> data;

    public BookingAdapter(Context context, List<Booking> data) {
        this.context = context;
        this.data = data;
    }

    @Override public int getCount() { return data.size(); }
    @Override public Object getItem(int position) { return data.get(position); }
    @Override public long getItemId(int position) { return data.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView != null ? convertView
                : LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        Booking b = data.get(position);

        ((TextView) v.findViewById(R.id.tvBkCustomer)).setText(b.getCustomerName());
        ((TextView) v.findViewById(R.id.tvBkPitch)).setText("Sân: " + b.getPitchName());
        ((TextView) v.findViewById(R.id.tvBkDate)).setText("Ngày thuê: " + b.getDate());
        ((TextView) v.findViewById(R.id.tvBkTime)).setText(
                "Giờ bắt đầu: " + b.getStartTime() + "  |  Số giờ: " + b.getHours());
        ((TextView) v.findViewById(R.id.tvBkTotal)).setText("Tổng tiền: " + Utils.money(b.getTotal()));

        TextView tvStatus = v.findViewById(R.id.tvBkStatus);
        if (tvStatus != null) {
            tvStatus.setText(b.getStatusText());
            if (b.getStatus() == Booking.STATUS_CANCELLED) {
                tvStatus.setTextColor(Color.parseColor("#C62828"));
                tvStatus.setBackgroundResource(R.drawable.bg_chip_booked);
            } else {
                tvStatus.setTextColor(Color.parseColor("#1B7F4B"));
                tvStatus.setBackgroundResource(R.drawable.bg_chip_free);
            }
        }

        return v;
    }
}
