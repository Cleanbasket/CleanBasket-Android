package com.washappkorea.corp.cleanbasket.io.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Notice {
    @DatabaseField(id = true) public int noid;
    @DatabaseField public String title;
    @DatabaseField public String content;
    @DatabaseField public String rdate;
}
