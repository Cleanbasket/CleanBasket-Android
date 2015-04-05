package com.washappkorea.corp.cleanbasket.ui.widget;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.model.OrderCategory;
import com.washappkorea.corp.cleanbasket.io.model.OrderItem;
import com.washappkorea.corp.cleanbasket.util.StringMatcher;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class OrderItemAdapter extends OrderItemAdapterHelper implements StickyGridHeadersSimpleAdapter, View.OnClickListener {
    private static final String TAG = OrderItemAdapter.class.getSimpleName();

    private LayoutInflater mLayoutInflater;
    private HashMap<Integer, OrderCategory> mOrderCategoryMap;
    private ArrayList<OrderItem> mFixedOrderItem;
    private DecimalFormat mFormatKRW = new DecimalFormat("###,###,###");

    public OrderItemAdapter(Context context, int resource) {
        super(context, resource);

        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mOrderCategoryMap = new HashMap<Integer, OrderCategory>();
    }

    @Override
    public long getHeaderId(int position) {
        return getItem(position).category;
    }

    public ArrayList<OrderItem> getSelectedItems() {
        ArrayList<OrderItem> orderItems = new ArrayList<OrderItem>();

        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).category > 0 && getItem(i).count > 0)
                orderItems.add(getItem(i));
        }

        return orderItems;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent) {
        OrderItemViewHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_orderitem, parent, false);
            holder = new OrderItemViewHolder();
            holder.linearLayoutOrderItem = (LinearLayout) convertView.findViewById(R.id.layout_orderitem);
            holder.orderImageView = (ImageView) convertView.findViewById(R.id.imageview_orderitem);
            holder.extractImageView = (ImageView) convertView.findViewById(R.id.imageview_extractitem);
            holder.textViewDiscountInfo = (TextView) convertView.findViewById(R.id.textview_discount_info);
            holder.textViewOrderItem = (TextView) convertView.findViewById(R.id.textview_orderitem);
            holder.textViewOrderItemPrice = (TextView) convertView.findViewById(R.id.textview_orderitem_price);
            holder.badgeView = new BadgeView(getContext(), holder.orderImageView);
            holder.badgeView.setBadgeBackgroundColor(getContext().getResources().getColor(R.color.badge_color));

            int width = CleanBasketApplication.getInstance().getPx(27);
            holder.badgeView.setLayoutParams(new LinearLayout.LayoutParams(width, width));
            holder.badgeView.setBadgeMargin(13);
            convertView.setTag(holder);
        } else
            holder = (OrderItemViewHolder) convertView.getTag();

        // 아이템이 1개 이상 선택되었을 경우
        if (getItem(position).count > 0) {
            holder.badgeView.setText(String.valueOf(getItem(position).count));
            holder.badgeView.show();
            holder.orderImageView.setImageResource(CleanBasketApplication.getInstance().getDrawableByString(getItem(position).img + "_select"));
            holder.extractImageView.setVisibility(View.VISIBLE);
            holder.extractImageView.setOnClickListener(this);
            holder.extractImageView.setTag(getItem(position));
        }
        else {
            holder.badgeView.hide();
            holder.orderImageView.setImageResource(CleanBasketApplication.getInstance().getDrawableByString(getItem(position).img));
            holder.extractImageView.setVisibility(View.INVISIBLE);
            holder.extractImageView.setOnClickListener(null);
        }

        holder.linearLayoutOrderItem.setOnClickListener(this);
        holder.linearLayoutOrderItem.setTag(getItem(position));
//        holder.orderImageView.setImageResource(CleanBasketApplication.getInstance().getDrawableByString(getItem(position).img));
//        holder.orderImageView.setImageResource(CleanBasketApplication.getInstance().getDrawableByString(getItem(position).img));
        if (getItem(position).discount_rate > 0)
            holder.textViewDiscountInfo.setText(getItem(position).discount_rate * 100 + "%");
        holder.textViewOrderItem.setText(CleanBasketApplication.getInstance().getStringByString(getItem(position).descr));
        holder.textViewOrderItemPrice.setText(mFormatKRW.format((double) getItem(position).price) + getContext().getString(R.string.monetary_unit));

        return convertView;
    }

    protected class OrderItemViewHolder {
        public LinearLayout linearLayoutOrderItem;
        public ImageView orderImageView;
        public ImageView extractImageView;
        public TextView textViewDiscountInfo;
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
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageview_category);
            holder.textView = (TextView) convertView.findViewById(R.id.textview_header);
            convertView.setTag(holder);
        } else
            holder = (HeaderViewHolder) convertView.getTag();

        OrderCategory orderCategory;

        try {
            orderCategory = mOrderCategoryMap.get(getItem(position).category);

            holder.imageView.setImageResource(CleanBasketApplication.getInstance().getDrawableByCategoryString(orderCategory.name));
            holder.textView.setText(CleanBasketApplication.getInstance().getStringByString(orderCategory.name));
        } catch (NullPointerException e) {
            holder.textView.setText(R.string.etc);
        }

        return convertView;
    }

    protected class HeaderViewHolder {
        public ImageView imageView;
        public TextView textView;
    }

    public void setFixedOrderItem(ArrayList<OrderItem> orderItems) {
        mFixedOrderItem = orderItems;
    }

    public void setOrderCategoryList(ArrayList<OrderCategory> orderCategoryList) {
        mOrderCategoryMap.clear();

        for (OrderCategory orderCategory : orderCategoryList)
            mOrderCategoryMap.put(orderCategory.id, orderCategory);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<OrderItem> filteredItems = new ArrayList<OrderItem>();

                if(constraint.length() == 0)
                    filteredItems = mFixedOrderItem;
                else {
                    for (OrderItem orderItem : mFixedOrderItem) {
                        if (StringMatcher.match(CleanBasketApplication.getInstance().getStringByString(orderItem.descr), constraint.toString()))
                            filteredItems.add(orderItem);
                    }
                }

                results.count = filteredItems.size();
                results.values = filteredItems;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();
                addAll((ArrayList<OrderItem>) results.values);

                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_orderitem:
                addOrderItem((OrderItem) v.getTag());
//                ((OrderItem) v.getTag()).count++;
                break;

            case R.id.imageview_extractitem:
                extractOrderItem((OrderItem) v.getTag());
//                ((OrderItem) v.getTag()).count--;
                break;
        }

        notifyDataSetChanged();
    }

    private void addOrderItem(OrderItem orderItem) {
        for (int i = 0; i < getCount(); i++) {
            if(getItem(i).item_code == orderItem.item_code)
                getItem(i).count++;
        }
    }

    private void extractOrderItem(OrderItem orderItem) {
        for (int i = 0; i < getCount(); i++) {
            if(getItem(i).item_code == orderItem.item_code)
                getItem(i).count--;
        }
    }
}