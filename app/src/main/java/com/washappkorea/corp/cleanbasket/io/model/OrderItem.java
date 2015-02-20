package com.washappkorea.corp.cleanbasket.io.model;


import android.os.Parcel;
import android.os.Parcelable;

public class OrderItem implements Parcelable {
    public int item_code;
    public String name;
    public String descr;
    public int category;
    public int price;
    public int count;
    public String img;
    public float discountRate;

    public OrderItem(int item_code, String name, int price, int category, String img) {
        this.item_code = item_code;
        this.name = name;
        this.category = category;
        this.price = price;
        this.img = img;
    }

    public OrderItem(Parcel source) {
        this.item_code = source.readInt();
        this.name = source.readString();
        this.descr = source.readString();
        this.category = source.readInt();
        this.price = source.readInt();
        this.count = source.readInt();
        this.img = source.readString();
        this.discountRate = source.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(item_code);
        dest.writeString(name);
        dest.writeString(descr);
        dest.writeInt(category);
        dest.writeInt(price);
        dest.writeInt(count);
        dest.writeString(img);
        dest.writeFloat(discountRate);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public OrderItem createFromParcel(Parcel in) {
            return new OrderItem(in);
        }

        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };
}
