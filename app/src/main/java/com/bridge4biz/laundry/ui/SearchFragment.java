package com.bridge4biz.laundry.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.model.OrderItem;
import com.bridge4biz.laundry.ui.widget.OrderItemAdapterHelper;
import com.readystatesoftware.viewbadger.BadgeView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment{
    public static final String TAG = SearchFragment.class.getSimpleName();

    private ArrayList<OrderItem> orderItems;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_search);

        return rootView;
    }


    protected class ItemListAdapter extends OrderItemAdapterHelper {
        private LayoutInflater mLayoutInflater;

        ItemListAdapter(Context context, int resource, List<OrderItem> objects) {
            super(context, resource, objects);

            this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            OrderItemListViewHolder holder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_orderitem_search, parent, false);

                holder = new OrderItemListViewHolder();
                holder.imageViewOrderItem = (ImageView) convertView.findViewById(R.id.imageview_orderitem);
                holder.textViewOrderItem = (TextView) convertView.findViewById(R.id.textview_orderitem);
                holder.textViewOrderItemPrice = (TextView) convertView.findViewById(R.id.textview_orderitem_price);
                convertView.setTag(holder);
            } else
                holder = (OrderItemListViewHolder) convertView.getTag();

            if (getItem(position).count > 0) {
                holder.badgeView.setText(String.valueOf(getItem(position).count));
                holder.badgeView.show();
            }

            holder.textViewOrderItem.setText(CleanBasketApplication.getInstance().getStringByString(getItem(position).descr));
            holder.textViewOrderItemPrice.setText(String.valueOf(CleanBasketApplication.mFormatKRW.format((double) getItem(position).price)) + getContext().getString(R.string.monetary_unit));

            return convertView;
        }

        protected class OrderItemListViewHolder {
            public ImageView imageViewOrderItem;
            public TextView textViewOrderItem;
            public TextView textViewOrderItemPrice;
            public BadgeView badgeView;
        }
    }
}
