package com.bridge4biz.laundry.ui.widget;

import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;

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
    public int scope;
    public int type;

    public CalculationInfo(String image, String name, int price, int type) {
        this.image = image;
        this.name = name;
        this.price = price;
        this.type = type;
    }

    public CalculationInfo(String image, String name, int price, int scope, int type) {
        this.image = image;
        this.name = name;
        this.price = price;
        this.scope = scope;
        this.type = type;
    }

    public float getTextSize() {
        switch (type) {
            case TOTAL:
                return CleanBasketApplication.getInstance().getResources().getDimension(R.dimen.textview_big_dimen);
        }

        return CleanBasketApplication.getInstance().getResources().getDimension(R.dimen.textview_small_dimen);
    }

    public String getPriceTag() {
        if (scope == 0)
            return CleanBasketApplication.mFormatKRW.format((double) price) + CleanBasketApplication.getInstance().getString(R.string.monetary_unit);
        else
            return CleanBasketApplication.mFormatKRW.format((double) price) + CleanBasketApplication.getInstance().getString(R.string.range_monetary_unit);
    }
}
