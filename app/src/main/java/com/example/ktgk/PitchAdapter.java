package com.example.ktgk;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class PitchAdapter extends BaseAdapter {
    public interface Listener {
        void onDelete(Pitch pitch);
    }

    private final Context context;
    private final List<Pitch> data;
    private final Listener listener;

    public PitchAdapter(Context context, List<Pitch> data, Listener listener) {
        this.context = context;
        this.data = data;
        this.listener = listener;
    }

    @Override public int getCount() { return data.size(); }
    @Override public Object getItem(int position) { return data.get(position); }
    @Override public long getItemId(int position) { return data.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView != null ? convertView
                : LayoutInflater.from(context).inflate(R.layout.item_pitch, parent, false);
        final Pitch p = data.get(position);

        ((TextView) v.findViewById(R.id.tvPitchTitle)).setText(p.getCode() + " - " + p.getName());
        ((TextView) v.findViewById(R.id.tvPitchAddress)).setText("Địa chỉ: " + p.getAddress());
        ((TextView) v.findViewById(R.id.tvPitchPrice)).setText("Giá: " + Utils.money(p.getPricePerHour()) + "/giờ");

        TextView tvStatus = v.findViewById(R.id.tvPitchStatus);
        tvStatus.setText(p.getStatusText());
        if (p.getStatus() == Pitch.BOOKED) {
            tvStatus.setTextColor(Color.parseColor("#C62828"));
            tvStatus.setBackgroundResource(R.drawable.bg_chip_booked);
        } else {
            tvStatus.setTextColor(Color.parseColor("#1B7F4B"));
            tvStatus.setBackgroundResource(R.drawable.bg_chip_free);
        }

        Button btnDelete = v.findViewById(R.id.btnDeletePitch);
        btnDelete.setOnClickListener(view -> listener.onDelete(p));
        return v;
    }
}
