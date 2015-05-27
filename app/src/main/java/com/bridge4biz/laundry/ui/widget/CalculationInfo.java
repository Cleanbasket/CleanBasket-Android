package com.bridge4biz.laundry.ui.widget;

public class CalculationInfo {
    public static final int PRE_TOTAL = 0;
    public static final int COST = 1;
    public static final int SALE = 2;
    public static final int MILEAGE = 3;
    public static final int COUPON = 4;
    public static final int TOTAL = 5;

    public String image;
    public String name;
    public int price;
    public int type;

    public CalculationInfo(String image, String name, int price, int type) {
        this.image = image;
        this.name = name;
        this.price = price;
        this.type = type;
    }
}
