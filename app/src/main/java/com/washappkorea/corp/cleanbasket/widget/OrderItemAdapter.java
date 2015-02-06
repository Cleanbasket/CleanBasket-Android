package com.washappkorea.corp.cleanbasket.widget;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.model.OrderCategory;
import com.washappkorea.corp.cleanbasket.io.model.OrderItem;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderItemAdapter extends ArrayAdapter<OrderItem> implements StickyGridHeadersSimpleAdapter, View.OnClickListener {
    private static final String TAG = OrderItemAdapter.class.getSimpleName();

    private LayoutInflater mLayoutInflater;
    private HashMap<Integer, OrderCategory> mOrderCategoryMap;

    public OrderItemAdapter(Context context, int resource) {
        super(context, resource);

        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mOrderCategoryMap = new HashMap<Integer, OrderCategory>();
    }

    @Override
    public long getHeaderId(int position) {
        return getItem(position).category;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent) {
        OrderItemViewHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_orderitem, parent, false);
            holder = new OrderItemViewHolder();
            holder.orderImageView = (ImageView) convertView.findViewById(R.id.imageview_orderitem);
            holder.extractImageView = (ImageView) convertView.findViewById(R.id.imageview_extractitem);
            holder.textViewOrderItem = (TextView) convertView.findViewById(R.id.textview_orderitem);
            holder.textViewOrderItemPrice = (TextView) convertView.findViewById(R.id.textview_orderitem_price);
            holder.badgeView = new BadgeView(getContext(), holder.orderImageView);
            convertView.setTag(holder);
        } else {
            holder = (OrderItemViewHolder) convertView.getTag();
        }

        if(getItem(position).count > 0) {
            holder.badgeView.setText(String.valueOf(getItem(position).count));
            holder.badgeView.show();
            holder.extractImageView.setVisibility(View.VISIBLE);
            holder.extractImageView.setOnClickListener(this);
            holder.extractImageView.setTag(getItem(position));
        }
        else {
            holder.badgeView.hide();
            holder.extractImageView.setVisibility(View.INVISIBLE);
            holder.extractImageView.setOnClickListener(null);
        }

        holder.orderImageView.setImageResource(getDrawableByString(getItem(position).img));
        holder.orderImageView.setOnClickListener(this);
        holder.orderImageView.setTag(getItem(position));
        holder.textViewOrderItem.setText(getItem(position).name);
        holder.textViewOrderItemPrice.setText(String.valueOf(getItem(position).price) + getContext().getString(R.string.monetary_unit));

        return convertView;
    }

    protected class OrderItemViewHolder {
        public ImageView orderImageView;
        public ImageView extractImageView;
        public TextView textViewOrderItem;
        public TextView textViewOrderItemPrice;
        public BadgeView badgeView;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.custom_header, parent, false);
            holder = new HeaderViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.textview_header);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        OrderCategory orderCategory;

        try {
            orderCategory = mOrderCategoryMap.get(getItem(position).category);

            holder.textView.setText(orderCategory.name);
        } catch (NullPointerException e) {
            holder.textView.setText(R.string.etc);
        }

        return convertView;
    }

    protected class HeaderViewHolder {
        public TextView textView;
    }

    public void setOrderCategoryList(ArrayList<OrderCategory> orderCategoryList) {
        mOrderCategoryMap.clear();

        for(OrderCategory orderCategory : orderCategoryList) {
            mOrderCategoryMap.put(orderCategory.id, orderCategory);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_orderitem:
                ((OrderItem) v.getTag()).count++;
                break;
            case R.id.imageview_extractitem:
                ((OrderItem) v.getTag()).count--;
                break;
        }

        notifyDataSetChanged();
    }

    private int getDrawableByString(String name) {
        return getContext().getResources().getIdentifier(name, "drawable", getContext().getPackageName());
    }
}
