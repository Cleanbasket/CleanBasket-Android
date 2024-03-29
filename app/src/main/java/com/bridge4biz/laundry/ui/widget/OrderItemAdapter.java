package com.bridge4biz.laundry.ui.widget;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.model.OrderCategory;
import com.bridge4biz.laundry.io.model.OrderItem;
import com.bridge4biz.laundry.util.StringMatcher;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.readystatesoftware.viewbadger.BadgeView;
import com.squareup.picasso.Picasso;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderItemAdapter extends OrderItemAdapterHelper implements StickyGridHeadersSimpleAdapter, View.OnTouchListener {
    private static final String TAG = OrderItemAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private HashMap<Integer, OrderCategory> mOrderCategoryMap;
    private ArrayList<OrderItem> mFixedOrderItem;
    private final SpringListener mSpringListener = new SpringListener();
    private View v;

    private SpringSystem springSystem = SpringSystem.create();
    private Spring spring = springSystem.createSpring();

    public OrderItemAdapter(Context context, int resource) {
        super(context, resource);

        this.mContext = context;
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mOrderCategoryMap = new HashMap<Integer, OrderCategory>();

        spring.addListener(mSpringListener);
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

    public void clearItems() {
        for (int i = 0; i < getCount(); i++) {
            getItem(i).count = 0;
        }

        notifyDataSetChanged();
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent) {
        OrderItemViewHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_orderitem, parent, false);
            holder = new OrderItemViewHolder();
            holder.linearLayoutOrderItem = (LinearLayout) convertView.findViewById(R.id.layout_orderitem);
            holder.outlineImageView = (ImageView) convertView.findViewById(R.id.imageview_outline);
            holder.orderImageView = (ImageView) convertView.findViewById(R.id.imageview_orderitem);
            holder.orderItemContainer = (RelativeLayout) convertView.findViewById(R.id.orderitem_container);
            holder.extractImageView = (RelativeLayout) convertView.findViewById(R.id.imageview_extractitem);
            holder.infoImageView = (RelativeLayout) convertView.findViewById(R.id.imageview_info);
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

        holder.infoImageView.setOnTouchListener(this);
        holder.infoImageView.setTag(getItem(position));

        // 아이템이 1개 이상 선택되었을 경우
        if (getItem(position).count > 0) {
            holder.badgeView.setText(String.valueOf(getItem(position).count));
            holder.badgeView.show();
            holder.outlineImageView.setVisibility(View.INVISIBLE);
            if (CleanBasketApplication.getInstance().getDrawableByString(getItem(position).img + "_select") != 0)
                holder.orderImageView.setImageResource(CleanBasketApplication.getInstance().getDrawableByString(getItem(position).img + "_select"));
            else
                Picasso.with(mContext).load(Config.IMAGE_SERVER_ITEM + getItem(position).img + "_select.png").into(holder.orderImageView);
            holder.extractImageView.setVisibility(View.VISIBLE);
            holder.infoImageView.setVisibility(View.INVISIBLE);
            holder.extractImageView.setOnTouchListener(this);
            holder.extractImageView.setTag(getItem(position));
        }
        else {
            holder.badgeView.hide();
            holder.outlineImageView.setVisibility(View.VISIBLE);
            if (CleanBasketApplication.getInstance().getDrawableByString(getItem(position).img) != 0)
                holder.orderImageView.setImageResource(CleanBasketApplication.getInstance().getDrawableByString(getItem(position).img));
            else
                Picasso.with(mContext).load(Config.IMAGE_SERVER_ITEM + getItem(position).img + ".png").into(holder.orderImageView);
            holder.extractImageView.setVisibility(View.INVISIBLE);
            if (getItem(position).getInfo() > 0)
                holder.infoImageView.setVisibility(View.VISIBLE);
            else
                holder.infoImageView.setVisibility(View.INVISIBLE);
            holder.extractImageView.setOnClickListener(null);
        }

        holder.orderItemContainer.setOnTouchListener(this);
        holder.orderItemContainer.setTag(getItem(position));
//        holder.linearLayoutOrderItem.setOnClickListener(this);
//        holder.orderImageView.setImageResource(CleanBasketApplication.getInstance().getDrawableByString(getItem(position).img));
//        holder.orderImageView.setImageResource(CleanBasketApplication.getInstance().getDrawableByString(getItem(position).img));

        if (getItem(position).getDiscount_rate() > 0)
            holder.textViewDiscountInfo.setText(getItem(position).discount_rate * 100 + "%");
        else
            holder.textViewDiscountInfo.setVisibility(View.GONE);

        holder.textViewOrderItem.setText(getItem(position).getNameTag());

        holder.textViewOrderItemPrice.setText(getItem(position).getPriceTag());

        return convertView;
    }

    protected class OrderItemViewHolder {
        public LinearLayout linearLayoutOrderItem;
        public ImageView outlineImageView;
        public ImageView orderImageView;
        public RelativeLayout orderItemContainer;
        public RelativeLayout extractImageView;
        public RelativeLayout infoImageView;
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
            holder.imageView.setImageResource(CleanBasketApplication.getInstance().getDrawableByCategoryString(orderCategory.img));
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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.orderitem_container:
                addOrderItem((OrderItem) v.getTag());
                break;

            case R.id.imageview_extractitem:
                extractOrderItem((OrderItem) v.getTag());
                break;

            case R.id.imageview_info:
                popInfo(((OrderItem) v.getTag()).getInfo());
                break;
        }

        notifyDataSetChanged();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        this.v = v;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // When pressed start solving the spring to 1.
                spring.setEndValue(1);
                break;
            case MotionEvent.ACTION_UP:
                onClick(v);
            case MotionEvent.ACTION_CANCEL:
                // When released start solving the spring to 0.
                spring.setEndValue(0);
                break;
        }

        return true;
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

    private void popInfo(int popId) {
        Intent intent = new Intent();
        intent.setAction("com.bridge4biz.laundry.ui.InfoActivity");

        intent.putExtra("type", "item");
        intent.putExtra("popId", popId);

        mContext.startActivity(intent);
    }

    protected class SpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            // On each update of the spring value, we adjust the scale of the image view to match the
            // springs new value. We use the SpringUtil linear interpolation function mapValueFromRangeToRange
            // to translate the spring's 0 to 1 scale to a 100% to 50% scale range and apply that to the View
            // with setScaleX/Y. Note that rendering is an implementation detail of the application and not
            // Rebound itself. If you need Gingerbread compatibility consider using NineOldAndroids to update
            // your view properties in a backwards compatible manner.
            float mappedValue = (float) SpringUtil.mapValueFromRangeToRange(spring.getCurrentValue(), 0, 1, 1, 0.8);

            if (v != null) {
                v.setScaleX(mappedValue);
                v.setScaleY(mappedValue);
            }
        }
    }
}