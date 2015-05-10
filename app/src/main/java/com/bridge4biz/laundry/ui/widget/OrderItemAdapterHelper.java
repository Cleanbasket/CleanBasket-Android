package com.bridge4biz.laundry.ui.widget;


import android.content.Context;
import android.widget.ArrayAdapter;

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
}
