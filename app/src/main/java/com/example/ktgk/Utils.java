package com.example.ktgk;

import java.text.NumberFormat;
import java.util.Locale;

public class Utils {
    public static final String EXTRA_PITCH_ID = "pitch_id";
    public static final String EXTRA_BOOKING_ID = "booking_id";

    public static String money(long value) {
        return NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(value) + " đ";
    }

    public static int toMinutes(String hhmm) {
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    public static String toTime(int minutes) {
        return String.format(Locale.US, "%02d:%02d", minutes / 60, minutes % 60);
    }
}
