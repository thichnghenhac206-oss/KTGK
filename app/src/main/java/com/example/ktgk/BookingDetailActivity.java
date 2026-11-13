package com.example.ktgk;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class BookingDetailActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private Booking booking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);
        db = new DatabaseHelper(this);

        long id = getIntent().getLongExtra(Utils.EXTRA_BOOKING_ID, -1);
        booking = db.getBooking(id);
        if (booking == null) {
            Toast.makeText(this, "Không tìm thấy đơn đặt sân", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tvDtId)).setText("Mã đơn: #" + booking.getId());
        ((TextView) findViewById(R.id.tvDtCustomer)).setText("Người đặt: " + booking.getCustomerName());
        ((TextView) findViewById(R.id.tvDtPhone)).setText("Số điện thoại: " + booking.getPhone());
        ((TextView) findViewById(R.id.tvDtPitch)).setText("Sân: " + booking.getPitchName());
        ((TextView) findViewById(R.id.tvDtDate)).setText("Ngày thuê: " + booking.getDate());
        ((TextView) findViewById(R.id.tvDtTime)).setText("Thời gian: " + booking.getStartTime()
                + " - " + booking.getEndTime());
        ((TextView) findViewById(R.id.tvDtHours)).setText("Số giờ thuê: " + booking.getHours());
        ((TextView) findViewById(R.id.tvDtTotal)).setText("Tổng tiền: " + Utils.money(booking.getTotal()));

        TextView tvStatus = findViewById(R.id.tvDtStatus);
        if (tvStatus != null) {
            tvStatus.setText(booking.getStatusText());
            if (booking.getStatus() == Booking.STATUS_CANCELLED) {
                tvStatus.setTextColor(Color.parseColor("#C62828"));
                tvStatus.setBackgroundResource(R.drawable.bg_chip_booked);
            } else {
                tvStatus.setTextColor(Color.parseColor("#1B7F4B"));
                tvStatus.setBackgroundResource(R.drawable.bg_chip_free);
            }
        }

        Button btnCancel = findViewById(R.id.btnCancelBooking);
        Button btnBack = findViewById(R.id.btnBackDetail);
        btnBack.setOnClickListener(v -> finish());

        if (booking.getStatus() == Booking.STATUS_CANCELLED) {
            btnCancel.setVisibility(View.GONE);
        } else {
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Hủy đơn đặt sân")
                    .setMessage("Bạn có chắc muốn hủy đơn này không?")
                    .setPositiveButton("Hủy đơn", (d, w) -> {
                        db.cancelBooking(booking.getId());
                        Toast.makeText(this, "Đã hủy đơn đặt sân", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Không", null)
                    .show());
        }
    }
}
