package com.bridge4biz.laundry.io.model;

public class OrderItemInfo {
    public int item_code = 0;
    public String title = "";
    public String content = "";
    public String img = "";

    public OrderItemInfo() {

    }

    public OrderItemInfo(String title, String content, String img) {
        this.title = title;
        this.content = content;
        this.img = img;
    }

    public OrderItemInfo(int item_code, String title, String content, String img) {
        this.item_code = item_code;
        this.title = title;
        this.content = content;
        this.img = img;
    }

    public int getItem_code() {
        return item_code;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImg() {
        return img;
    }
}
