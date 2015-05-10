package com.bridge4biz.laundry.io.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class District extends BaseDaoEnabled<District, Integer> {
    public static final String ID = "dcid";

    @DatabaseField(id = true) public int dcid;
    @DatabaseField public String city;
    @DatabaseField public String district;
    @DatabaseField public String dong;

    public District() {

    }
}