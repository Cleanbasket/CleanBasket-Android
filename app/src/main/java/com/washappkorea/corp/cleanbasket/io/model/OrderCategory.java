package com.washappkorea.corp.cleanbasket.io.model;


import io.realm.RealmObject;

public class OrderCategory extends RealmObject {
    public int id;
    public String name;
    public String img;

    public OrderCategory(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
