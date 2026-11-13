package com.example.ktgk;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddPitchActivity extends AppCompatActivity {
    private EditText edtCode, edtName, edtAddress, edtPrice;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pitch);
        db = new DatabaseHelper(this);

        edtCode = findViewById(R.id.edtCode);
        edtName = findViewById(R.id.edtName);
        edtAddress = findViewById(R.id.edtAddress);
        edtPrice = findViewById(R.id.edtPrice);
        Button btnSave = findViewById(R.id.btnSavePitch);
        Button btnCancel = findViewById(R.id.btnCancelPitch);

        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void save() {
        String code = edtCode.getText().toString().trim().toUpperCase();
        String name = edtName.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();

        if (code.isEmpty()) { error(edtCode, "Vui lòng nhập mã sân"); return; }
        if (name.isEmpty()) { error(edtName, "Vui lòng nhập tên sân"); return; }
        if (address.isEmpty()) { error(edtAddress, "Vui lòng nhập địa chỉ"); return; }
        if (priceStr.isEmpty()) { error(edtPrice, "Vui lòng nhập giá thuê"); return; }

        long price;
        try {
            price = Long.parseLong(priceStr);
        } catch (NumberFormatException e) {
            error(edtPrice, "Giá thuê không hợp lệ");
            return;
        }
        if (price <= 0) { error(edtPrice, "Giá thuê phải lớn hơn 0"); return; }
        if (db.codeExists(code)) { error(edtCode, "Mã sân đã tồn tại"); return; }

        db.insertPitch(code, name, address, price);
        Toast.makeText(this, "Đã thêm sân " + name, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void error(EditText e, String msg) {
        e.setError(msg);
        e.requestFocus();
    }
}
