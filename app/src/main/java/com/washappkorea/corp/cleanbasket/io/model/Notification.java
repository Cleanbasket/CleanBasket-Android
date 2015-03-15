package com.washappkorea.corp.cleanbasket.io.model;


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

    @DatabaseField(allowGeneratedIdInsert = true, generatedId = true) public int nid;
    @DatabaseField public int oid;
    @DatabaseField public int type;
    @DatabaseField public String title;
    @DatabaseField public String message;
    @DatabaseField public String image;
    @DatabaseField public int value;
    @DatabaseField public String date;
    @DatabaseField public boolean check;
}
