package com.example.ktgk;

public class Pitch {
    public static final int FREE = 0;
    public static final int BOOKED = 1;

    private long id;
    private String code;
    private String name;
    private String address;
    private long pricePerHour;
    private int status;

    public Pitch(long id, String code, String name, String address, long pricePerHour, int status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.address = address;
        this.pricePerHour = pricePerHour;
        this.status = status;
    }

    public long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public long getPricePerHour() { return pricePerHour; }
    public int getStatus() { return status; }
    public String getStatusText() { return status == BOOKED ? "Đã được đặt" : "Còn trống"; }
}
