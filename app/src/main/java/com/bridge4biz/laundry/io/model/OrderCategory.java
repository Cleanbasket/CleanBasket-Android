package com.bridge4biz.laundry.io.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class OrderCategory extends BaseDaoEnabled<OrderCategory, Integer> {
    @DatabaseField(id = true) public int id;
    @DatabaseField public String name;
    @DatabaseField public String img;

    public OrderCategory() {

    }
}
