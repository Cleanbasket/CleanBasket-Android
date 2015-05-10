package com.bridge4biz.laundry.io.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class OrderItem extends BaseDaoEnabled<OrderItem, Integer> implements Parcelable {
    @DatabaseField(id = true) public int item_code;
    @DatabaseField public int category;
    @DatabaseField public String name;
    @DatabaseField public String descr;
    @DatabaseField public int price;
    @DatabaseField public int count;
    @DatabaseField public String img;
    @DatabaseField public double discount_rate;

    public OrderItem() {

    }

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
        this.discount_rate = source.readFloat();
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
        dest.writeDouble(discount_rate);
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
