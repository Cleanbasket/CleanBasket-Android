package com.bridge4biz.laundry.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.model.Order;
import com.bridge4biz.laundry.io.request.PostRequest;
import com.bridge4biz.laundry.ui.BaseActivity;
import com.bridge4biz.laundry.ui.OrderStatusFragment;
import com.bridge4biz.laundry.ui.dialog.CalculationDialog;
import com.bridge4biz.laundry.ui.dialog.ItemListDialog;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;
import com.bridge4biz.laundry.util.DateTimeFactory;
import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import java.util.List;

public class OrderStateAdapter extends ArrayAdapter<Order> implements View.OnClickListener {
    private Context mContext;

    public static final int PICK_UP_WAIT = 0;
    public static final int PICK_UP_MAN_SELECTED = 1;
    public static final int PICK_UP_FINISH = 2;
    public static final int DELIVERY_MAN_SELECTED = 3;
    public static final int DELIVERY_FINISH = 4;

    public static final int IN_PERSON_CARD = 0;
    public static final int IN_PERSON_CASH = 1;
    public static final int ACCOUNT_TRANSFER = 2;
    public static final int IN_APP_PAYMENT = 3;
    public static final int IN_APP_PAYMENT_FINISH = 6;

    public OrderStateAdapter(Context context, int resource, List<Order> objects) {
        super(context, resource, objects);

        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OrderStatusViewHolder holder;

        if (convertView == null) {
            convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_order_history, parent, false);

            holder = new OrderStatusViewHolder();
            holder.textViewOrderStatus = (TextView) convertView.findViewById(R.id.textview_order_mileage);
            holder.textViewOrderNumber = (TextView) convertView.findViewById(R.id.textview_order_number_status);
            holder.textViewPickUpDateTime = (TextView) convertView.findViewById(R.id.textview_pick_up_time_status);
            holder.textViewDropOffDateTime = (TextView) convertView.findViewById(R.id.textview_drop_off_time_status);
            holder.textViewTotal = (TextView) convertView.findViewById(R.id.textview_total_status);
            holder.textViewTotalNumber = (TextView) convertView.findViewById(R.id.textview_item_number_status);
            holder.textViewPaymentMethod = (TextView) convertView.findViewById(R.id.textview_payment_method);

            holder.textViewTotal.setOnClickListener(this);
            holder.textViewTotalNumber.setOnClickListener(this);
            holder.textViewPaymentMethod.setOnClickListener(this);

            convertView.setTag(holder);
        } else
            holder = (OrderStatusViewHolder) convertView.getTag();
        
        holder.textViewOrderStatus.setText(getStringByState(getItem(position).state));
        holder.textViewOrderNumber.setText(String.format("%s %d", getString(R.string.order_number), getItem(position).oid));
        holder.textViewPickUpDateTime.setText(
                DateTimeFactory.getInstance().getDate(mContext, getItem(position).pickup_date) + " " +
                        DateTimeFactory.getInstance().getTime(mContext, getItem(position).pickup_date) + getString(R.string.time_tilde) +
                        DateTimeFactory.getInstance().getPlusOneTime(mContext, getItem(position).pickup_date));
        holder.textViewDropOffDateTime.setText(
                DateTimeFactory.getInstance().getDate(mContext, getItem(position).dropoff_date) + " " +
                        DateTimeFactory.getInstance().getTime(mContext, getItem(position).dropoff_date) + getString(R.string.time_tilde) +
                        DateTimeFactory.getInstance().getPlusOneTime(mContext, getItem(position).dropoff_date));
        holder.textViewTotal.setText(
                getString(R.string.label_total) + " " +
                        CleanBasketApplication.mFormatKRW.format(getItem(position).getTotal()) + getString(R.string.monetary_unit));
        holder.textViewTotalNumber.setText(
                getString(R.string.label_item) + " " +
                        getItem(position).getTotalNumberFromOrder() + getString(R.string.item_unit));
        holder.textViewPaymentMethod.setText(getStringByPaymentState(getItem(position).payment_method));

        holder.textViewTotal.setTag(getItem(position));
        holder.textViewTotalNumber.setTag(getItem(position));
        holder.textViewPaymentMethod.setTag(getItem(position));

        return convertView;
    }

    @Override
    public void onClick(View v) {
        Order o = (Order) v.getTag();

        switch (v.getId()) {
            case R.id.textview_total_status:
                CalculationDialog calculationDialog =
                        CalculationDialog.newInstance(o, null);

                calculationDialog.show(
                        ((BaseActivity) mContext).getSupportFragmentManager(),
                        OrderStatusFragment.TOTAL_DIALOG_TAG);
                break;

            case R.id.textview_item_number_status:
                ItemListDialog itemListDialog =
                        ItemListDialog.newInstance(o.item, null);

                itemListDialog.show(
                        ((BaseActivity) mContext).getSupportFragmentManager(),
                        OrderStatusFragment.ITEM_LIST_DIALOG_TAG);
                break;

            case R.id.textview_payment_method:
                if (o.payment_method == IN_APP_PAYMENT_FINISH)
                    getInAppPaymentResult(o.oid);
                else if (o.payment_method == IN_APP_PAYMENT)
                    CleanBasketApplication.getInstance().showToast(getString(R.string.payment_payment_unpaid));
                else
                    CleanBasketApplication.getInstance().showToast(getString(R.string.payment_result_etc));
                break;
        }
    }

    private void getInAppPaymentResult(Integer oid) {
        PostRequest postRequest = new PostRequest(mContext);
        postRequest.setParams("oid", oid);
        postRequest.setUrl(AddressManager.GET_PAYMENT_RESULT);
        postRequest.setListener(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JsonData jsonData;

                try {
                    jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);
                } catch (JsonSyntaxException e) {
                    return;
                }

                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        String tid = jsonData.data;
                        if (tid == null)
                            CleanBasketApplication.getInstance().showToast(getString(R.string.toast_error));
                        else {
                            tid = tid.replaceAll("\"", "");
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.NICE_PAYMENT_ADDRESS + tid + Config.NICE_PAYMENT_ADDRESS_SUFFIX));
                            mContext.startActivity(intent);
                        }
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CleanBasketApplication.getInstance().showToast(getString(R.string.toast_error));
            }
        });
        RequestQueue.getInstance(mContext).addToRequestQueue(postRequest.doRequest());
    }

    private String getStringByState(Integer state) {
        String stateString = "";

        switch (state) {
            case PICK_UP_WAIT:
                stateString = getString(R.string.pick_up_wait);
                break;
            case PICK_UP_MAN_SELECTED:
                stateString = getString(R.string.pick_up_man_selected);
                break;
            case PICK_UP_FINISH:
                stateString = getString(R.string.pick_up_finish);
                break;
            case DELIVERY_MAN_SELECTED:
                stateString = getString(R.string.deliverer_selected);
                break;
            case DELIVERY_FINISH:
                stateString = getString(R.string.deliverer_finish);
                break;
        }

        return stateString;
    }

    protected class OrderStatusViewHolder {
        public TextView textViewOrderStatus;
        public TextView textViewOrderNumber;
        public TextView textViewPickUpDateTime;
        public TextView textViewDropOffDateTime;
        public TextView textViewTotal;
        public TextView textViewTotalNumber;
        public TextView textViewPaymentMethod;
    }

    protected String getString(int r) {
        return mContext.getString(r);
    }

    private String getStringByPaymentState(int state) {
        String stateString = "";

        switch (state) {
            case IN_PERSON_CARD:
                stateString = getString(R.string.payment_card);
                break;
            case IN_PERSON_CASH:
                stateString = getString(R.string.payment_cash);
                break;
            case ACCOUNT_TRANSFER:
                stateString = getString(R.string.payment_transfer);
                break;
            case IN_APP_PAYMENT:
                stateString = getString(R.string.payment_in_app);
                break;
            case IN_APP_PAYMENT_FINISH:
                stateString = getString(R.string.payment_in_app_finish);
                break;
        }

        return stateString;
    }
}
