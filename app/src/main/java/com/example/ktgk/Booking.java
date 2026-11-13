package com.example.ktgk;

public class Booking {
    public static final int STATUS_CANCELLED = 0;
    public static final int STATUS_BOOKED = 1;

    private long id;
    private long pitchId;
    private String pitchName;
    private String customerName;
    private String phone;
    private String date;
    private String startTime;
    private int hours;
    private long total;
    private int status;

    public Booking(long id, long pitchId, String pitchName, String customerName, String phone,
                   String date, String startTime, int hours, long total, int status) {
        this.id = id;
        this.pitchId = pitchId;
        this.pitchName = pitchName;
        this.customerName = customerName;
        this.phone = phone;
        this.date = date;
        this.startTime = startTime;
        this.hours = hours;
        this.total = total;
        this.status = status;
    }

    public Booking(long id, long pitchId, String pitchName, String customerName, String phone,
                   String date, String startTime, int hours, long total) {
        this(id, pitchId, pitchName, customerName, phone, date, startTime, hours, total, STATUS_BOOKED);
    }

    public long getId() { return id; }
    public long getPitchId() { return pitchId; }
    public String getPitchName() { return pitchName; }
    public String getCustomerName() { return customerName; }
    public String getPhone() { return phone; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public int getHours() { return hours; }
    public long getTotal() { return total; }
    public int getStatus() { return status; }

    public String getStatusText() {
        return status == STATUS_CANCELLED ? "Đã hủy" : "Đã đặt";
    }

    public String getEndTime() {
        return Utils.toTime(Utils.toMinutes(startTime) + hours * 60);
    }
}
