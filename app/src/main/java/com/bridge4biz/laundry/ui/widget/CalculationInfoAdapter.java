package com.bridge4biz.laundry.ui.widget;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.model.AuthUser;
import com.bridge4biz.laundry.ui.UserFragment;

import java.util.List;


public class CalculationInfoAdapter extends ArrayAdapter<CalculationInfo> {
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public AuthUser mAuthUser;
    public int mTotal;
    public int mResource;

    public CalculationInfoAdapter(Context context, int resource, List<CalculationInfo> objects) {
        super(context, resource, objects);

        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CalculationInfoHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(mResource, parent, false);
            holder = new CalculationInfoHolder();
            holder.line = (View) convertView.findViewById(R.id.line_top);
            holder.imageViewCalculationInfo = (ImageView) convertView.findViewById(R.id.imageview_calculation_info);
            holder.textViewCalculationInfo = (TextView) convertView.findViewById(R.id.textview_calculation_label);
            holder.textViewCalculationInfoDetail = (TextView) convertView.findViewById(R.id.textview_calculation_label_detail);
            holder.textViewCalculation = (TextView) convertView.findViewById(R.id.textview_calculation);
            holder.buttonUse = (TextView) convertView.findViewById(R.id.button_use);
            convertView.setTag(holder);
        } else
            holder = (CalculationInfoHolder) convertView.getTag();

        holder.line.setVisibility(View.GONE);
        holder.textViewCalculationInfoDetail.setVisibility(View.GONE);

        switch (getItem(position).type) {
            case CalculationInfo.TOTAL:
                if (mResource == R.layout.item_calculation_info)
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.order_info_background));
                holder.line.setVisibility(View.VISIBLE);
                holder.imageViewCalculationInfo.setImageResource(0);
                holder.textViewCalculation.setVisibility(View.VISIBLE);
                holder.textViewCalculationInfoDetail.setVisibility(View.GONE);
                holder.buttonUse.setVisibility(View.GONE);
                break;

            case CalculationInfo.PRE_TOTAL:
                holder.imageViewCalculationInfo.setImageResource(0);
            case CalculationInfo.COST:
            case CalculationInfo.SALE:
                if (mResource == R.layout.item_calculation_info)
                    convertView.setBackgroundColor(mContext.getResources().getColor(R.color.caculation_item_backgound_color));
                holder.textViewCalculation.setVisibility(View.VISIBLE);
                holder.textViewCalculationInfoDetail.setVisibility(View.GONE);
                holder.buttonUse.setVisibility(View.GONE);
                break;

            case CalculationInfo.MILEAGE:
                if (mResource == R.layout.item_calculation_info) {
                    if (mAuthUser != null)
                        holder.textViewCalculationInfoDetail.setText(mContext.getString(R.string.mileage_accumulation) + " " + CleanBasketApplication.mFormatKRW.format(mTotal * getAccumulationRate(mAuthUser.user_class)));
                    else
                        holder.textViewCalculationInfoDetail.setText(mContext.getString(R.string.join_recommendation));
                }
                holder.textViewCalculationInfoDetail.setVisibility(View.VISIBLE);
            case CalculationInfo.COUPON:
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.caculation_item_backgound_color));
                holder.textViewCalculation.setVisibility(View.GONE);
                holder.buttonUse.setVisibility(View.VISIBLE);
                holder.buttonUse.setTag(getItem(position).type);
                if (getItem(position).price > 0)
                    holder.buttonUse.setText(CleanBasketApplication.mFormatKRW.format(getItem(position).price) + mContext.getString(R.string.monetary_unit));
                else
                    holder.buttonUse.setText(mContext.getString(R.string.button_label_use));
                break;
        }

        if (getItem(position).image != null)
            holder.imageViewCalculationInfo.setImageResource(getDrawableByString(getItem(position).image));

        holder.textViewCalculationInfo.setText(getItem(position).name);
        holder.textViewCalculation.setText(getItem(position).getPriceTag());
        holder.textViewCalculation.setTextSize(TypedValue.COMPLEX_UNIT_PX, getItem(position).getTextSize());

        return convertView;
    }

    private float getAccumulationRate(Integer user_class) {
        switch (user_class) {
            case UserFragment.BRONZE:
                return (float) Config.CLASS_CLEAN_BASKET;
            case UserFragment.SILVER:
                return (float) Config.CLASS_SILVER_BASKET;
            case UserFragment.GOLD:
                return (float) Config.CLASS_GOLD_BASKET;
            case UserFragment.LOVE:
                return (float) Config.CLASS_LOVE_BASKET;
        }

        return (float) Config.CLASS_CLEAN_BASKET;
    }
    /* 이름으로 아이콘을 가져옵니다 */
    public int getDrawableByString(String name) {
        return getContext().getResources().getIdentifier("ic_order_" + name, "drawable", getContext().getPackageName());
    }

    public int getTotal() {
        int total = 0;

        for (int i = 0; i < getCount(); i++) {
            switch (getItem(i).type) {
                case CalculationInfo.COST:
                    total = total + getItem(i).price;
                    break;

                case CalculationInfo.SALE:
                case CalculationInfo.MILEAGE:
                case CalculationInfo.COUPON:
                    total = total - getItem(i).price;
                    break;
            }
        }

        return total;
    }

    protected class CalculationInfoHolder {
        public View line;
        public ImageView imageViewCalculationInfo;
        public TextView textViewCalculationInfo;
        public TextView textViewCalculationInfoDetail;
        public TextView textViewCalculation;
        public TextView buttonUse;
    }

    public int getPriceByType(int type) {
        if (getCalculationInfoByType(type) != null)
            return getCalculationInfoByType(type).price;

        return 0;
    }

    public CalculationInfo getCalculationInfoByType(int type) {
        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).type == type)
                return getItem(i);
        }

        return null;
    }
}