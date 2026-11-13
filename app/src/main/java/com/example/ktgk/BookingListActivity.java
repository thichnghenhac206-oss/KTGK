package com.example.ktgk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class BookingListActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private final List<Booking> data = new ArrayList<>();
    private BookingAdapter adapter;
    private TextView tvEmpty;
    private int currentFilterStatus = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_list);
        db = new DatabaseHelper(this);

        ListView lv = findViewById(R.id.lvBookings);
        tvEmpty = findViewById(R.id.tvEmptyBooking);
        Button btnNew = findViewById(R.id.btnNewBooking);
        Button btnBack = findViewById(R.id.btnBackList);
        TabLayout tabLayout = findViewById(R.id.tabBookingFilter);

        adapter = new BookingAdapter(this, data);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(this, BookingDetailActivity.class);
            i.putExtra(Utils.EXTRA_BOOKING_ID, data.get(position).getId());
            startActivity(i);
        });

        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 1:
                            currentFilterStatus = Booking.STATUS_BOOKED;
                            break;
                        case 2:
                            currentFilterStatus = Booking.STATUS_CANCELLED;
                            break;
                        default:
                            currentFilterStatus = -1;
                            break;
                    }
                    loadData();
                }

                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        btnNew.setOnClickListener(v -> startActivity(new Intent(this, BookingActivity.class)));
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        data.clear();
        data.addAll(db.getBookingsFiltered(currentFilterStatus));
        adapter.notifyDataSetChanged();
        if (tvEmpty != null) {
            if (currentFilterStatus == Booking.STATUS_BOOKED) {
                tvEmpty.setText("Chưa có đơn đặt sân nào");
            } else if (currentFilterStatus == Booking.STATUS_CANCELLED) {
                tvEmpty.setText("Chưa có đơn bị hủy nào");
            } else {
                tvEmpty.setText("Chưa có đơn đặt sân nào");
            }
            tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}
