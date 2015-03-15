package com.washappkorea.corp.cleanbasket.io.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Alarm {
    public static final String OrderID = "oid";

    @DatabaseField(allowGeneratedIdInsert = true, generatedId = true) public int aid;
    @DatabaseField public int oid;
    @DatabaseField public int type;
    @DatabaseField public long date;

    public Alarm() {

    }

    public Alarm(int oid, int type, long date) {
        this.oid = oid;
        this.type = type;
        this.date = date;
    }
}
