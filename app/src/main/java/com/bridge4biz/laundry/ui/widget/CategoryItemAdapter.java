package com.bridge4biz.laundry.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.model.OrderCategory;

public class CategoryItemAdapter extends ArrayAdapter<OrderCategory> {
    private LayoutInflater mLayoutInflater;
    private Integer mSelectedPostion = 0;

    public CategoryItemAdapter(Context context, int resource) {
        super(context, resource);

        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void selectItem(int position) {
        getItem(mSelectedPostion).isSelected = false;

        getItem(position).isSelected = true;

        mSelectedPostion = position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.custom_category_header, parent, false);
            holder = new HeaderViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.textview_header);
            convertView.setTag(holder);
        } else
            holder = (HeaderViewHolder) convertView.getTag();

        OrderCategory orderCategory;

        try {
            orderCategory = getItem(position);

            if (getItem(position).isSelected)
                convertView.setBackgroundColor(getContext().getResources().getColor(android.R.color.white));
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
}
