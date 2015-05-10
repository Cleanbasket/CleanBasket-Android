package com.bridge4biz.laundry.io.model;


import java.io.Serializable;
import java.util.ArrayList;

public class Order implements Serializable {
	public Integer oid = 0;
	public String order_number = "";
	public Integer state = 0;
	public String phone = "";
	public String address = "";
	public String addr_building = "";
	public String memo = "";
	public Integer price = 0;
	public Integer dropoff_price = 0;
	public String pickup_date = "";
	public String dropoff_date = "";
    public Integer mileage = 0;
    public Integer sale = 0;
    public Integer payment_method = 0;
    public Deliverer pickupInfo;
    public Deliverer dropoffInfo;
	public ArrayList<OrderItem> item;
	public ArrayList<Coupon> coupon;

    public Order() {

    }

    public Order(Integer oid, String pickup_date, String dropoff_date) {
        this.oid = oid;
        this.pickup_date = pickup_date;
        this.dropoff_date = dropoff_date;
    }
}