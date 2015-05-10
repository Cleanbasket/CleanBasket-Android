package com.bridge4biz.laundry.ui.dialog;


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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.listener.NetworkErrorListener;
import com.bridge4biz.laundry.io.model.Coupon;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.request.GetRequest;
import com.bridge4biz.laundry.ui.OrderInfoFragment;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class CouponDialog extends DialogFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = CouponDialog.class.getSimpleName();

    private OnCouponSetListener mOnCouponSetListener;

    private ListView mCouponListView;
    private Button mCancelButton;
    private View mCouponFormView;
    private View mProgressView;

    private CouponAdapter mCouponAdapter;

    public interface OnCouponSetListener {
        void onCouponSet(CouponDialog dialog, Coupon coupon);
    }

    public static CouponDialog newInstance(OnCouponSetListener onCouponSetListener) {
        CouponDialog ild = new CouponDialog();
        ild.initialize(onCouponSetListener);

        return ild;
    }

    public void initialize(OnCouponSetListener onCouponSetListener) {
        this.mOnCouponSetListener = onCouponSetListener;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getShowsDialog()) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

//        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
//        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
//        getDialog().getWindow().setLayout(width, height);
//        getDialog().setCanceledOnTouchOutside(false);

        View rootView = inflater.inflate(R.layout.dialog_coupon, container, false);

        mCouponListView = (ListView) rootView.findViewById(R.id.listview_coupon_list);
        mCancelButton = (Button) rootView.findViewById(R.id.button_cancel_coupon_list);

        mCouponFormView = rootView.findViewById(R.id.coupon_form);
        mProgressView = rootView.findViewById(R.id.loading_progress);

        if (getTag().equals(OrderInfoFragment.COUPON_DIALOG_TAG))
            mCouponListView.setOnItemClickListener(this);
        else
            mCancelButton.setText(R.string.label_back);

        return rootView;
    }

    private void getCoupon() {
        showProgress(true);
        GetRequest getRequest = new GetRequest(getActivity());
        getRequest.setUrl(AddressManager.GET_COUPON);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonData jsonData = null;

                try {
                    jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response, JsonData.class);
                } catch (JsonSyntaxException e) {
                    return;
                }

                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        ArrayList<Coupon> coupons = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, new TypeToken<ArrayList<Coupon>>(){}.getType());
                        insertCoupon(coupons);
                        showProgress(false);
                        break;
                }
            }
        }, new NetworkErrorListener(getActivity()));
        RequestQueue.getInstance(getActivity()).addToRequestQueue(getRequest.doRequest());
    }

    private void insertCoupon(ArrayList<Coupon> coupons) {
        mCouponAdapter = new CouponAdapter(getActivity(), R.layout.item_coupon, coupons);
        mCouponListView.setAdapter(mCouponAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getCoupon();

        mCancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_cancel_coupon_list:
                mOnCouponSetListener.onCouponSet(this, null);
                dismiss();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mOnCouponSetListener.onCouponSet(this, mCouponAdapter.getItem(position));
        dismiss();
    }

    class CouponAdapter extends ArrayAdapter<Coupon> {
        private LayoutInflater mLayoutInflater;

        CouponAdapter(Context context, int resource, List<Coupon> objects) {
            super(context, resource, objects);

            this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CouponListViewHolder holder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_coupon, parent, false);
                holder = new CouponListViewHolder();
                holder.textViewCouponPrice = (TextView) convertView.findViewById(R.id.textview_coupon_price);
                holder.textViewCouponTitle = (TextView) convertView.findViewById(R.id.textview_coupon_title);
                holder.textViewCouponValidateTime = (TextView) convertView.findViewById(R.id.textview_coupon_validate_time);
                convertView.setTag(holder);
            } else
                holder = (CouponListViewHolder) convertView.getTag();

            holder.textViewCouponPrice.setText((getItem(position).value) + getString(R.string.monetary_unit));
            holder.textViewCouponTitle.setText(getItem(position).name);
            if (getItem(position).start_date.equals("") || getItem(position).equals(""))
                holder.textViewCouponValidateTime.setText(getString(R.string.coupon_invalidate));
            else
                holder.textViewCouponValidateTime.setText(getItem(position).start_date + " " + getString(R.string.time_tilde) + " " + getItem(position).end_date);

            return convertView;
        }

        protected class CouponListViewHolder {
            public TextView textViewCouponPrice;
            public TextView textViewCouponTitle;
            public TextView textViewCouponValidateTime;
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

            mCouponFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mCouponFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCouponFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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