package com.washappkorea.corp.cleanbasket.io.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Notification {
    @DatabaseField(id = true) public int nid;
    @DatabaseField public int oid;
    @DatabaseField public int type;
    @DatabaseField public String message;
    @DatabaseField public int value;
    @DatabaseField public String date;
    @DatabaseField public boolean check;
}
