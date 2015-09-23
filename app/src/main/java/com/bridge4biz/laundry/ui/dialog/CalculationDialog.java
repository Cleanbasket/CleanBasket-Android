package com.bridge4biz.laundry.ui.dialog;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
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
import android.widget.Button;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.Coupon;
import com.bridge4biz.laundry.io.model.Order;
import com.bridge4biz.laundry.io.model.OrderItem;
import com.bridge4biz.laundry.io.request.PostRequest;
import com.bridge4biz.laundry.ui.widget.CalculationInfo;
import com.bridge4biz.laundry.ui.widget.CalculationInfoAdapter;
import com.bridge4biz.laundry.util.AddressManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
            getDialog().setCanceledOnTouchOutside(true);
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
        mCalculationInfoAdapter = new CalculationInfoAdapter(getActivity(), R.layout.item_dialog_calculation_info, calculationInfos);
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

    private void updateOrder(Order order) {
        PostRequest postRequest = new PostRequest(getActivity());
        String body = CleanBasketApplication.getInstance().getGson().toJson(order);

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