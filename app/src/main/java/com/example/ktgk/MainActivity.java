package com.example.ktgk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private final List<Pitch> data = new ArrayList<>();
    private PitchAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        ListView lv = findViewById(R.id.lvPitches);
        tvEmpty = findViewById(R.id.tvEmpty);
        Button btnAdd = findViewById(R.id.btnAddPitch);
        Button btnBook = findViewById(R.id.btnBook);
        Button btnOrders = findViewById(R.id.btnOrders);

        adapter = new PitchAdapter(this, data, this::confirmDelete);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(this, BookingActivity.class);
            i.putExtra(Utils.EXTRA_PITCH_ID, data.get(position).getId());
            startActivity(i);
        });

        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddPitchActivity.class)));
        btnBook.setOnClickListener(v -> startActivity(new Intent(this, BookingActivity.class)));
        btnOrders.setOnClickListener(v -> startActivity(new Intent(this, BookingListActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        data.clear();
        data.addAll(db.getAllPitches());
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmDelete(Pitch p) {
        int count = db.countBookingsOfPitch(p.getId());
        String msg = "Xóa sân \"" + p.getName() + "\"?";
        if (count > 0) {
            msg += "\nSân này đang có " + count + " đơn đặt, các đơn này cũng sẽ bị xóa.";
        }
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage(msg)
                .setPositiveButton("Xóa", (d, w) -> {
                    db.deletePitch(p.getId());
                    Toast.makeText(this, "Đã xóa sân", Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
