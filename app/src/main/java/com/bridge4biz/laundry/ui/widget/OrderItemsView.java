package com.bridge4biz.laundry.ui.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.AdapterView;

import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

public class OrderItemsView extends StickyGridHeadersGridView implements AdapterView.OnItemClickListener {
    private static final String TAG = OrderItemsView.class.getSimpleName();

    public OrderItemsView(Context context) {
        super(context);
    }

    public OrderItemsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OrderItemsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}