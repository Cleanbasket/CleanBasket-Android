package com.bridge4biz.laundry.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.model.Order;
import com.bridge4biz.laundry.io.request.GetRequest;
import com.bridge4biz.laundry.ui.widget.OrderStateAdapter;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OrderHistoryActivity extends BaseActivity {
    private TextView mTextViewEmpty;
    private ListView mOrderStateListView;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        mOrderStateListView = (ListView) findViewById(R.id.listview_order_state);
        mTextViewEmpty = (TextView) findViewById(R.id.textview_no_item);
        mProgressView = findViewById(R.id.loading_progress);

        mOrderStateListView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getOrderStatus();
    }

    private void getOrderStatus() {
        showProgress(true);

        GetRequest getRequest = new GetRequest(this);
        getRequest.setUrl(AddressManager.GET_ALL_ORDER);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonData jsonData;

                try {
                    jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response, JsonData.class);
                } catch (JsonSyntaxException e) {
                    return;
                }

                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        showProgress(false);
                        ArrayList<Order> orders = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, new TypeToken<ArrayList<Order>>(){}.getType());
                        insertOrderStatus(orders);
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CleanBasketApplication.getInstance().showToast(getString(R.string.toast_error));
                showProgress(false);
            }
        });
        RequestQueue.getInstance(this).addToRequestQueue(getRequest.doRequest());
    }

    /**
     * 받아온 주문 정보를 리스트뷰에 넣습니다. 이때 알람을 띄울 수 있도록 DB에 저장합니다.
     * @param orders
     */
    private void insertOrderStatus(ArrayList<Order> orders) {
        if(orders != null && orders.size() > 0) {
            Collections.sort(orders, new Comparator<Order>() {
                @Override
                public int compare(Order lhs, Order rhs) {
                    return rhs.oid - lhs.oid;
                }
            });

            OrderStateAdapter orderStatusAdapter = new OrderStateAdapter(this, 0, orders);
            mOrderStateListView.setAdapter(orderStatusAdapter);
            mTextViewEmpty.setVisibility(View.GONE);
        }
        else {
            // 리스트뷰에 아이템이 남아 있을 경우 모두 제거
            if (mOrderStateListView != null) {
                OrderStateAdapter orderStatusAdapter = new OrderStateAdapter(this, 0, orders);
                mOrderStateListView.setAdapter(orderStatusAdapter);
            }
            mTextViewEmpty.setVisibility(View.VISIBLE);
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

            mOrderStateListView.setVisibility(show ? View.GONE : View.VISIBLE);
            mOrderStateListView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mOrderStateListView.setVisibility(show ? View.GONE : View.VISIBLE);
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
