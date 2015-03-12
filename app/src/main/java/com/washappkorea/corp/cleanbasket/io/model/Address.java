package com.washappkorea.corp.cleanbasket.io.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Address {
    public static final String ID = "adrid";

    @DatabaseField(id = true) public int adrid;
    @DatabaseField public String address;
    @DatabaseField public String address_detail;

    public Address() {

    }

    public Address(String address, String address_detail) {
        this.address = address;
        this.address_detail = address_detail;
    }
}
