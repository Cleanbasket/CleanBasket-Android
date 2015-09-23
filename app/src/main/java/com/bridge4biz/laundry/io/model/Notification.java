package com.bridge4biz.laundry.io.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Notification {
    public static final int EVENT_ALARM = 0;
    public static final int MESSAGE_ALARM = 1;
    public static final int PICKUP_ALARM = 2;
    public static final int DROPOFF_ALARM = 3;
    public static final int COUPON_ALARM = 4;
    public static final int FEEDBACK_ALARM = 5;
    public static final int MODIFY_ALARM = 6;
    public static final int MILEAGE_ALARM = 7;
    public static final int PAYMENT_ALARM = 8;
    public static final int PAYMENT_CANCEL_ALARM = 9;

    public static final String UID = "uid";

    @DatabaseField(allowGeneratedIdInsert = true, generatedId = true) public int nid;
    @DatabaseField public int uid;
    @DatabaseField public int oid;
    @DatabaseField public int type;
    @DatabaseField public String title;
    @DatabaseField public String message;
    @DatabaseField public String image;
    @DatabaseField public int value;
    @DatabaseField public String date;
    @DatabaseField public boolean check;

    public Notification() {
    }
}
