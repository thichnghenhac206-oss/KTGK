package com.example.ktgk;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private List<Pitch> pitches;

    private Spinner spPitch;
    private EditText edtName, edtPhone, edtHours;
    private Button btnDate, btnTime;
    private TextView tvTotal;

    private int year = -1, month, day;
    private int hour = -1, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        db = new DatabaseHelper(this);

        spPitch = findViewById(R.id.spPitch);
        edtName = findViewById(R.id.edtCustomerName);
        edtPhone = findViewById(R.id.edtPhone);
        edtHours = findViewById(R.id.edtHours);
        btnDate = findViewById(R.id.btnPickDate);
        btnTime = findViewById(R.id.btnPickTime);
        tvTotal = findViewById(R.id.tvTotal);
        Button btnSave = findViewById(R.id.btnSaveBooking);
        Button btnBack = findViewById(R.id.btnBackBooking);

        pitches = db.getAllPitches();
        if (pitches.isEmpty()) {
            Toast.makeText(this, "Chưa có sân nào, hãy thêm sân trước", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        List<String> labels = new ArrayList<>();
        for (Pitch p : pitches) {
            labels.add(p.getCode() + " - " + p.getName() + " (" + Utils.money(p.getPricePerHour()) + "/giờ)");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPitch.setAdapter(adapter);

        long preId = getIntent().getLongExtra(Utils.EXTRA_PITCH_ID, -1);
        for (int i = 0; i < pitches.size(); i++) {
            if (pitches.get(i).getId() == preId) {
                spPitch.setSelection(i);
                break;
            }
        }

        spPitch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { updateTotal(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        edtHours.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { updateTotal(); }
        });

        btnDate.setOnClickListener(v -> pickDate());
        btnTime.setOnClickListener(v -> pickTime());
        btnSave.setOnClickListener(v -> save());
        btnBack.setOnClickListener(v -> finish());
        updateTotal();
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        int y = year >= 0 ? year : c.get(Calendar.YEAR);
        int m = year >= 0 ? month : c.get(Calendar.MONTH);
        int d = year >= 0 ? day : c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dlg = new DatePickerDialog(this, (view, yy, mm, dd) -> {
            year = yy; month = mm; day = dd;
            btnDate.setText(String.format(Locale.US, "%02d/%02d/%04d", dd, mm + 1, yy));
        }, y, m, d);
        dlg.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dlg.show();
    }

    private void pickTime() {
        int h = hour >= 0 ? hour : 18;
        int m = hour >= 0 ? minute : 0;
        new TimePickerDialog(this, (view, hh, mm) -> {
            hour = hh; minute = mm;
            btnTime.setText(String.format(Locale.US, "%02d:%02d", hh, mm));
        }, h, m, true).show();
    }

    private void updateTotal() {
        long total = 0;
        try {
            int hours = Integer.parseInt(edtHours.getText().toString().trim());
            Pitch p = pitches.get(spPitch.getSelectedItemPosition());
            if (hours > 0) total = p.getPricePerHour() * hours;
        } catch (Exception ignored) { }
        tvTotal.setText("Tổng tiền: " + Utils.money(total));
    }

    private void save() {
        Pitch pitch = pitches.get(spPitch.getSelectedItemPosition());
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String hoursStr = edtHours.getText().toString().trim();

        if (name.isEmpty()) { error(edtName, "Vui lòng nhập họ tên"); return; }
        if (phone.isEmpty()) { error(edtPhone, "Vui lòng nhập số điện thoại"); return; }
        if (!phone.matches("0\\d{9}")) { error(edtPhone, "Số điện thoại gồm 10 số, bắt đầu bằng 0"); return; }
        if (year < 0) { Toast.makeText(this, "Vui lòng chọn ngày thuê", Toast.LENGTH_SHORT).show(); return; }
        if (hour < 0) { Toast.makeText(this, "Vui lòng chọn giờ bắt đầu", Toast.LENGTH_SHORT).show(); return; }
        if (hoursStr.isEmpty()) { error(edtHours, "Vui lòng nhập số giờ thuê"); return; }

        int hours;
        try {
            hours = Integer.parseInt(hoursStr);
        } catch (NumberFormatException e) {
            error(edtHours, "Số giờ không hợp lệ");
            return;
        }
        if (hours <= 0) { error(edtHours, "Số giờ thuê phải lớn hơn 0"); return; }

        int startMin = hour * 60 + minute;
        int endMin = startMin + hours * 60;
        if (endMin > 24 * 60) { error(edtHours, "Giờ kết thúc vượt quá 24:00 trong ngày"); return; }

        Calendar start = Calendar.getInstance();
        start.set(year, month, day, hour, minute, 0);
        if (start.before(Calendar.getInstance())) {
            Toast.makeText(this, "Thời gian bắt đầu phải ở tương lai", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year);
        String startTime = Utils.toTime(startMin);

        Booking conflict = db.findConflict(pitch.getId(), date, startMin, endMin);
        if (conflict != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Sân đã có người đặt")
                    .setMessage(pitch.getName() + " đã được đặt ngày " + conflict.getDate() + " từ "
                            + conflict.getStartTime() + " đến " + conflict.getEndTime()
                            + ".\nVui lòng chọn khung giờ khác.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        long total = pitch.getPricePerHour() * hours;
        Booking b = new Booking(0, pitch.getId(), pitch.getName(), name, phone, date, startTime, hours, total);
        db.insertBooking(b);

        Toast.makeText(this, "Đặt sân thành công!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, BookingListActivity.class));
        finish();
    }

    private void error(EditText e, String msg) {
        e.setError(msg);
        e.requestFocus();
    }
}
