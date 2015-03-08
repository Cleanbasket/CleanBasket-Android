package com.washappkorea.corp.cleanbasket.io.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class AppInfo extends BaseDaoEnabled<AppInfo, Integer> {
    public static final String ID = "aiid";

    @DatabaseField(id = true) public int aiid;
    @DatabaseField public int order_item_ver;
    @DatabaseField public String android_app_ver;
    @DatabaseField public String ios_app_ver;

    public AppInfo() {

    }
}