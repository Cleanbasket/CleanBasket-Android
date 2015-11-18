package com.bridge4biz.laundry.ui.widget;


import android.content.Context;
import android.widget.ArrayAdapter;

import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.model.OrderItem;

import java.util.List;

public class OrderItemAdapterHelper extends ArrayAdapter<OrderItem> {
    private static final String TAG = OrderItemAdapterHelper.class.getSimpleName();

    public OrderItemAdapterHelper(Context context, int resource) {
        super(context, resource);
    }

    public OrderItemAdapterHelper(Context context, int resource, List<OrderItem> objects) {
        super(context, resource, objects);
    }

    /* 선택된 아이템의 전체 갯수를 반환합니다 */
    public int getItemNumber() {
        int number = 0;

        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).count > 0)
                number = number + getItem(i).count;
        }

        return number;
    }

    /* 선택된 아이템의 전체 금액을 반환합니다 */
    public int getItemTotal() {
        int total = 0;

        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).count > 0)
                total = total + getItem(i).count * getItem(i).price;
        }

        return total;
    }

    public String getMonetaryUnit() {
        int scope = 0;

        for (int i = 0; i < getCount(); i++) {
            scope += getItem(i).scope;
        }

        if (scope > 0)
            return CleanBasketApplication.getInstance().getString(R.string.range_monetary_unit);
        else
            return CleanBasketApplication.getInstance().getString(R.string.monetary_unit);
    }

    public int getScope() {
        int scope = 0;

        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).count > 0)
                scope += getItem(i).scope;
        }

        return scope;
    }
}
