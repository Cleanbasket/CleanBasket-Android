package com.washappkorea.corp.cleanbasket.io.model;


import java.util.ArrayList;

public class Order {
	public Integer oid = 0;
	public Integer pickup_man;
	public Integer dropoff_man = 0;
	public String order_number = "";
	public Integer state = 0;
	public String phone = "";
	public String address = "";
	public String addr_number = "";
	public String addr_building = "";
	public String addr_remainder = "";
	public String memo = "";
	public Integer price = 0;
	public Integer dropoff_price = 0;
	public String pickup_date = "";
	public String dropoff_date = "";
	public String rdate = "";
    public Integer mileage = 0;
    public Deliverer pickupInfo;
    public Deliverer dropoffInfo;
	public ArrayList<OrderItem> item;
	public ArrayList<Coupon> coupons;
}