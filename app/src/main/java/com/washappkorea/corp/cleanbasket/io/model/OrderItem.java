package com.washappkorea.corp.cleanbasket.io.model;


public class OrderItem {
    public int item_code;
    public String name;
    public String descr;
    public int category;
    public int price;
    public int count;
    public String img;
    public float discountRate;

    public OrderItem(String name, int price, int category, String img) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.img = img;
    }
}
