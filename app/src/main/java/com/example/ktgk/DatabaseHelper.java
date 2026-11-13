package com.example.ktgk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "football.db";
    private static final int DB_VERSION = 2;

    private static final String T_PITCH = "pitches";
    private static final String T_BOOKING = "bookings";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PITCH + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "code TEXT NOT NULL UNIQUE, " +
                "name TEXT NOT NULL, " +
                "address TEXT NOT NULL, " +
                "price INTEGER NOT NULL, " +
                "status INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE " + T_BOOKING + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pitch_id INTEGER NOT NULL, " +
                "pitch_name TEXT NOT NULL, " +
                "customer_name TEXT NOT NULL, " +
                "phone TEXT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "start_time TEXT NOT NULL, " +
                "hours INTEGER NOT NULL, " +
                "total INTEGER NOT NULL, " +
                "status INTEGER NOT NULL DEFAULT 1)");

        seed(db, "S001", "Sân Mini A", "12 Nguyễn Trãi, Hà Nội", 200000);
        seed(db, "S002", "Sân Mini B", "45 Cầu Giấy, Hà Nội", 250000);
        seed(db, "S003", "Sân 7 người C", "88 Láng Hạ, Hà Nội", 400000);
    }

    private void seed(SQLiteDatabase db, String code, String name, String address, long price) {
        ContentValues v = new ContentValues();
        v.put("code", code);
        v.put("name", name);
        v.put("address", address);
        v.put("price", price);
        v.put("status", Pitch.FREE);
        db.insert(T_PITCH, null, v);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE " + T_BOOKING + " ADD COLUMN status INTEGER NOT NULL DEFAULT 1");
            } catch (Exception ignored) {
                db.execSQL("DROP TABLE IF EXISTS " + T_BOOKING);
                db.execSQL("DROP TABLE IF EXISTS " + T_PITCH);
                onCreate(db);
            }
        }
    }

    public boolean codeExists(String code) {
        Cursor c = getReadableDatabase().query(T_PITCH, new String[]{"id"}, "code=? COLLATE NOCASE",
                new String[]{code}, null, null, null);
        try {
            return c.moveToFirst();
        } finally {
            c.close();
        }
    }

    public long insertPitch(String code, String name, String address, long price) {
        ContentValues v = new ContentValues();
        v.put("code", code);
        v.put("name", name);
        v.put("address", address);
        v.put("price", price);
        v.put("status", Pitch.FREE);
        return getWritableDatabase().insert(T_PITCH, null, v);
    }

    public List<Pitch> getAllPitches() {
        List<Pitch> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(T_PITCH, null, null, null, null, null, "id ASC");
        try {
            while (c.moveToNext()) list.add(pitchFromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    public Pitch getPitch(long id) {
        Cursor c = getReadableDatabase().query(T_PITCH, null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            return c.moveToFirst() ? pitchFromCursor(c) : null;
        } finally {
            c.close();
        }
    }

    public int countBookingsOfPitch(long pitchId) {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + T_BOOKING + " WHERE pitch_id=? AND status=1",
                new String[]{String.valueOf(pitchId)});
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    public void deletePitch(long pitchId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(T_BOOKING, "pitch_id=?", new String[]{String.valueOf(pitchId)});
            db.delete(T_PITCH, "id=?", new String[]{String.valueOf(pitchId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private Pitch pitchFromCursor(Cursor c) {
        return new Pitch(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("code")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getString(c.getColumnIndexOrThrow("address")),
                c.getLong(c.getColumnIndexOrThrow("price")),
                c.getInt(c.getColumnIndexOrThrow("status")));
    }

    public Booking findConflict(long pitchId, String date, int startMin, int endMin) {
        Cursor c = getReadableDatabase().query(T_BOOKING, null, "pitch_id=? AND date=? AND status=1",
                new String[]{String.valueOf(pitchId), date}, null, null, null);
        try {
            while (c.moveToNext()) {
                Booking b = bookingFromCursor(c);
                int s = Utils.toMinutes(b.getStartTime());
                int e = s + b.getHours() * 60;
                if (startMin < e && s < endMin) return b;
            }
        } finally {
            c.close();
        }
        return null;
    }

    public long insertBooking(Booking b) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues v = new ContentValues();
            v.put("pitch_id", b.getPitchId());
            v.put("pitch_name", b.getPitchName());
            v.put("customer_name", b.getCustomerName());
            v.put("phone", b.getPhone());
            v.put("date", b.getDate());
            v.put("start_time", b.getStartTime());
            v.put("hours", b.getHours());
            v.put("total", b.getTotal());
            v.put("status", b.getStatus());
            long id = db.insert(T_BOOKING, null, v);
            refreshPitchStatus(db, b.getPitchId());
            db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    public List<Booking> getAllBookings() {
        return getBookingsFiltered(-1);
    }

    public List<Booking> getBookingsFiltered(int statusFilter) {
        List<Booking> list = new ArrayList<>();
        String selection = statusFilter == -1 ? null : "status=?";
        String[] selectionArgs = statusFilter == -1 ? null : new String[]{String.valueOf(statusFilter)};
        Cursor c = getReadableDatabase().query(T_BOOKING, null, selection, selectionArgs, null, null, "id DESC");
        try {
            while (c.moveToNext()) list.add(bookingFromCursor(c));
        } finally {
            c.close();
        }
        return list;
    }

    public Booking getBooking(long id) {
        Cursor c = getReadableDatabase().query(T_BOOKING, null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            return c.moveToFirst() ? bookingFromCursor(c) : null;
        } finally {
            c.close();
        }
    }

    public void cancelBooking(long bookingId) {
        Booking b = getBooking(bookingId);
        if (b == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues v = new ContentValues();
            v.put("status", Booking.STATUS_CANCELLED);
            db.update(T_BOOKING, v, "id=?", new String[]{String.valueOf(bookingId)});
            refreshPitchStatus(db, b.getPitchId());
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void refreshPitchStatus(SQLiteDatabase db, long pitchId) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + T_BOOKING + " WHERE pitch_id=? AND status=1",
                new String[]{String.valueOf(pitchId)});
        int count = 0;
        try {
            if (c.moveToFirst()) count = c.getInt(0);
        } finally {
            c.close();
        }
        ContentValues v = new ContentValues();
        v.put("status", count > 0 ? Pitch.BOOKED : Pitch.FREE);
        db.update(T_PITCH, v, "id=?", new String[]{String.valueOf(pitchId)});
    }

    private Booking bookingFromCursor(Cursor c) {
        int statusCol = c.getColumnIndex("status");
        int status = statusCol != -1 ? c.getInt(statusCol) : Booking.STATUS_BOOKED;
        return new Booking(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getLong(c.getColumnIndexOrThrow("pitch_id")),
                c.getString(c.getColumnIndexOrThrow("pitch_name")),
                c.getString(c.getColumnIndexOrThrow("customer_name")),
                c.getString(c.getColumnIndexOrThrow("phone")),
                c.getString(c.getColumnIndexOrThrow("date")),
                c.getString(c.getColumnIndexOrThrow("start_time")),
                c.getInt(c.getColumnIndexOrThrow("hours")),
                c.getLong(c.getColumnIndexOrThrow("total")),
                status);
    }
}
