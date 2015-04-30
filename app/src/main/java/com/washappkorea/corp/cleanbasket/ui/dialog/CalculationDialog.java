package com.washappkorea.corp.cleanbasket.ui.dialog;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.model.Coupon;
import com.washappkorea.corp.cleanbasket.io.model.Order;
import com.washappkorea.corp.cleanbasket.io.model.OrderItem;
import com.washappkorea.corp.cleanbasket.io.request.PostRequest;
import com.washappkorea.corp.cleanbasket.util.AddressManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CalculationDialog extends DialogFragment implements View.OnClickListener, Response.Listener, Response.ErrorListener {
    private static final String TAG = CalculationDialog.class.getSimpleName();
    public static final String MODIFY_TAG = "MODIFY_" + CalculationDialog.class.getSimpleName();

    private ListView mCalculationListView;
    private Button mAcceptButton;
    private View mCalculationFormView;
    private View mProgressView;

    private Order mOrder;

    private CalculationInfoAdapter mCalculationInfoAdapter;
    private CalculationListener mCalculationListener;

    public interface CalculationListener {
        void onCalculationFinish(CalculationDialog dialog);
    }

    public static CalculationDialog newInstance(Order order, CalculationListener calculationListener) {
        CalculationDialog cd = new CalculationDialog();
        cd.initialize(order, calculationListener);

        return cd;
    }

    public void initialize(Order order, CalculationListener calculationListener) {
        this.mOrder = order;
        this.mCalculationListener = calculationListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getShowsDialog()) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

//        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
//        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
//        getDialog().getWindow().setLayout(width, height);
//        getDialog().setCanceledOnTouchOutside(false);

        View rootView = inflater.inflate(R.layout.dialog_calculation_info, container, false);

        mCalculationListView = (ListView) rootView.findViewById(R.id.listview_calculation_list);
        mAcceptButton = (Button) rootView.findViewById(R.id.button_accpet_calculation_list);

        mCalculationFormView = rootView.findViewById(R.id.calculation_form);
        mProgressView = rootView.findViewById(R.id.loading_progress);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<CalculationInfo> calculationInfos = new ArrayList<CalculationInfo>();
        calculationInfos.add(new CalculationInfo(null, getString(R.string.label_item) + " " + getOrderItemCount() + getString(R.string.item_unit), getOrderPreTotal(), CalculationInfo.PRE_TOTAL));
        if (mOrder.dropoff_price > 0)
            calculationInfos.add(new CalculationInfo("pick_up_cost", getString(R.string.pick_up_cost), mOrder.dropoff_price, CalculationInfo.COST));
        if (mOrder.sale > 0)
            calculationInfos.add(new CalculationInfo(null, getString(R.string.sale_label), mOrder.sale, CalculationInfo.SALE));
        if (mOrder.mileage > 0)
            calculationInfos.add(new CalculationInfo("mileage", getString(R.string.mileage), mOrder.mileage, CalculationInfo.MILEAGE));
        if (getCouponTotal() > 0)
            calculationInfos.add(new CalculationInfo("coupon", getString(R.string.coupon), getCouponTotal(), CalculationInfo.COUPON));
        mCalculationInfoAdapter = new CalculationInfoAdapter(getActivity(), R.layout.item_calculation_info, calculationInfos);
        mCalculationInfoAdapter.add(new CalculationInfo(null, getString(R.string.label_total), getOrderPreTotal() + mCalculationInfoAdapter.getTotal(), CalculationInfo.TOTAL));

        mCalculationListView.setAdapter(mCalculationInfoAdapter);

        mAcceptButton.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        int height = getDialog().getWindow().getAttributes().height;
        getDialog().getWindow().setLayout(width, height);

        final Drawable d = new ColorDrawable(Color.BLACK);
        d.setAlpha(150);

        getDialog().getWindow().setBackgroundDrawable(d);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_accpet_calculation_list:
                if (getTag().equals(MODIFY_TAG))
                    updateOrder(mOrder);
                else
                    dismiss();
                break;
        }
    }

    private void updateOrder(Order mOrder) {
        PostRequest postRequest = new PostRequest(getActivity());
        String body = CleanBasketApplication.getInstance().getGson().toJson(mOrder);

        try {
            JSONObject jsonObject = new JSONObject(body);
            postRequest.setParams(jsonObject);
        } catch (JSONException e) {
            return;
        }

        postRequest.setUrl(AddressManager.MODIFY_ORDER_ITEM);
        postRequest.setListener(this, this);
        RequestQueue.getInstance(getActivity()).addToRequestQueue(postRequest.doRequest());
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        CleanBasketApplication.getInstance().showToast(getString(R.string.general_error));
    }

    @Override
    public void onResponse(Object o) {
        CleanBasketApplication.getInstance().showToast(getString(R.string.order_modify_success));

        mCalculationListener.onCalculationFinish(this);

        dismiss();
    }

    private int getOrderItemCount() {
        if (mOrder.item == null)
            return 0;

        int total = 0;

        for (OrderItem or : mOrder.item) {
            total = total + or.count;
        }

        return total;
    }

    private int getOrderPreTotal() {
        if (mOrder.item == null)
            return 0;

        int total = 0;

        for (OrderItem or : mOrder.item) {
            total = total + or.price * or.count;
        }

        return total;
    }

    private int getCouponTotal() {
        if (mOrder.coupon == null)
            return 0;

        int total = 0;

        for (Coupon coupon : mOrder.coupon) {
            total = total + coupon.value;
        }

        return total;
    }

    protected class CalculationInfo {
        public static final int PRE_TOTAL = 0;
        public static final int COST = 1;
        public static final int SALE = 2;
        public static final int MILEAGE = 3;
        public static final int COUPON = 4;
        public static final int TOTAL = 5;

        String image;
        String name;
        int price;
        int type;

        public CalculationInfo(String image, String name, int price, int type) {
            this.image = image;
            this.name = name;
            this.price = price;
            this.type = type;
        }
    }

    protected class CalculationInfoAdapter extends ArrayAdapter<CalculationInfo> {
        private LayoutInflater mLayoutInflater;

        public CalculationInfoAdapter(Context context, int resource, List<CalculationInfo> objects) {
            super(context, resource, objects);

            this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CalculationInfoHolder holder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_calculation_info, parent, false);
                holder = new CalculationInfoHolder();
                holder.imageViewCalculationInfo = (ImageView) convertView.findViewById(R.id.imageview_calculation_info);
                holder.textViewCalculationInfo = (TextView) convertView.findViewById(R.id.textview_calculation_label);
                holder.textViewCalculationInfoDetail = (TextView) convertView.findViewById(R.id.textview_calculation_label_detail);
                holder.textViewCalculation = (TextView) convertView.findViewById(R.id.textview_calculation);
                holder.buttonUse = (Button) convertView.findViewById(R.id.button_use);
                convertView.setTag(holder);
            } else
                holder = (CalculationInfoHolder) convertView.getTag();

            switch (getItem(position).type) {
                case CalculationInfo.PRE_TOTAL:
                case CalculationInfo.COST:
                case CalculationInfo.SALE:
                case CalculationInfo.TOTAL:
                    holder.textViewCalculation.setVisibility(View.VISIBLE);
                    holder.textViewCalculationInfoDetail.setVisibility(View.GONE);
                    holder.buttonUse.setVisibility(View.GONE);
                    break;

                case CalculationInfo.MILEAGE:
                case CalculationInfo.COUPON:
                    holder.textViewCalculation.setVisibility(View.VISIBLE);
                    holder.textViewCalculationInfoDetail.setVisibility(View.GONE);
                    holder.buttonUse.setVisibility(View.GONE);
                    holder.textViewCalculation.setText(getItem(position).price + getString(R.string.monetary_unit));
            }

            if (getItem(position).image != null)
                holder.imageViewCalculationInfo.setImageResource(getDrawableByString(getItem(position).image));
            else
                holder.imageViewCalculationInfo.setImageResource(0);
            holder.textViewCalculationInfo.setTextColor(getResources().getColor(R.color.dialog_text));
            holder.textViewCalculationInfo.setText(getItem(position).name);
            holder.textViewCalculation.setTextColor(getResources().getColor(R.color.dialog_text));
            holder.textViewCalculation.setText(getItem(position).price + getString(R.string.monetary_unit));

            return convertView;
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
            public ImageView imageViewCalculationInfo;
            public TextView textViewCalculationInfo;
            public TextView textViewCalculationInfoDetail;
            public TextView textViewCalculation;
            public Button buttonUse;
        }

        /* 이름으로 아이콘을 가져옵니다 */
        public int getDrawableByString(String name) {
            return getContext().getResources().getIdentifier("ic_order_" + name, "drawable", getContext().getPackageName());
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mCalculationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mCalculationFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCalculationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}