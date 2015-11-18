package com.bridge4biz.laundry.io.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class OrderItem extends BaseDaoEnabled<OrderItem, Integer> implements Parcelable {
    public static final String CATEGORY_COLUMN = "category";

    @DatabaseField(id = true) public int item_code;
    @DatabaseField public int category;
    @DatabaseField public String name;
    @DatabaseField public String descr;
    @DatabaseField public int price;
    @DatabaseField public int scope;
    @DatabaseField public int count;
    @DatabaseField public String img;
    @DatabaseField public int info;
    @DatabaseField public double discount_rate;

    public OrderItem() {

    }

    public OrderItem(int item_code, String name, int price, int scope, int category, String img, int info) {
        this.item_code = item_code;
        this.name = name;
        this.category = category;
        this.price = price;
        this.scope = scope;
        this.img = img;
        this.info = info;
    }

    public OrderItem(Parcel source) {
        this.item_code = source.readInt();
        this.name = source.readString();
        this.descr = source.readString();
        this.category = source.readInt();
        this.price = source.readInt();
        this.scope = source.readInt();
        this.count = source.readInt();
        this.img = source.readString();
        this.info = source.readInt();
        this.discount_rate = source.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getInfo() {
        return info;
    }

    public double getDiscount_rate() {
        return discount_rate;
    }

    public int getPrice() {
        return (int) (price * (1 - discount_rate));
    }

    public int getCount() {
        return count;
    }

    public int getTotalPrice() {
        return getPrice() * getCount();
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getNameTag() {
        if (CleanBasketApplication.getInstance().getStringByString(descr) != null)
            return CleanBasketApplication.getInstance().getStringByString(descr);
        else
            return name;
    }

    public String getPriceTag() {
        if (scope == 0)
            return CleanBasketApplication.mFormatKRW.format((double) price) + CleanBasketApplication.getInstance().getString(R.string.monetary_unit);
        else
            return CleanBasketApplication.mFormatKRW.format((double) price) + CleanBasketApplication.getInstance().getString(R.string.range_monetary_unit);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(item_code);
        dest.writeString(name);
        dest.writeString(descr);
        dest.writeInt(category);
        dest.writeInt(price);
        dest.writeInt(scope);
        dest.writeInt(count);
        dest.writeString(img);
        dest.writeInt(info);
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
