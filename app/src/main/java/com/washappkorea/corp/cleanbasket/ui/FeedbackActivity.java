package com.washappkorea.corp.cleanbasket.ui;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.Config;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.model.Feedback;
import com.washappkorea.corp.cleanbasket.io.model.JsonData;
import com.washappkorea.corp.cleanbasket.io.model.Order;
import com.washappkorea.corp.cleanbasket.io.request.GetRequest;
import com.washappkorea.corp.cleanbasket.io.request.PostRequest;
import com.washappkorea.corp.cleanbasket.util.AddressManager;
import com.washappkorea.corp.cleanbasket.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FeedbackActivity extends BaseActivity implements View.OnClickListener, Response.Listener<JSONObject>, RatingBar.OnRatingBarChangeListener {
    private static final String TAG = FeedbackActivity.class.getSimpleName();

    private Order mOrder;

    private ImageView mImageViewPdFace;
    private TextView mTextViewUserName;
    private RatingBar mRatingBar;
    private EditText mEditTextFree;
    private CheckBox mCheckBoxTime;
    private CheckBox mCheckBoxLaundry;
    private CheckBox mCheckBoxKindness;
    private Button mButton;

    private int mRatingStar;

    private View mFeedbackFormView;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feedback);

        getActionBar().setTitle(R.string.feedback_title);

        mFeedbackFormView = findViewById(R.id.feedback_form);
        mProgressView = findViewById(R.id.feedback_progress);

        mImageViewPdFace = (ImageView) findViewById(R.id.imageview_pd_face);
        mTextViewUserName = (TextView) findViewById(R.id.textview_user_name);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        mEditTextFree = (EditText) findViewById(R.id.edittext_free_feedback);
        mCheckBoxTime = (CheckBox) findViewById(R.id.checkbox_late_feedback);
        mCheckBoxLaundry = (CheckBox) findViewById(R.id.checkbox_laundry_feedback);
        mCheckBoxKindness = (CheckBox) findViewById(R.id.checkbox_kindness_feedback);
        mButton = (Button) findViewById(R.id.button_feedback_send);

        mButton.setOnClickListener(this);
        mRatingBar.setOnRatingBarChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mRatingStar = -1;
        int oid = getIntent().getIntExtra("oid", 0);
        Log.i(TAG, oid +"");
        getOrder(oid);
    }

    private void getOrder(final int oid) {
        showProgress(true);

        GetRequest getRequest = new GetRequest(this);
        getRequest.setUrl(AddressManager.GET_ORDER);
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
                        if (getOrderInfo(orders, oid) != null)
                            insertOrderInfo(getOrderInfo(orders, oid));
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getBaseContext(), getString(R.string.toast_error), Toast.LENGTH_SHORT);
                showProgress(false);
            }
        });
        RequestQueue.getInstance(this).addToRequestQueue(getRequest.doRequest());
    }

    private void insertOrderInfo(Order order) {
        if (order != null) {
            setPDFaceImage(mImageViewPdFace, order.dropoffInfo.img);
            mTextViewUserName.setText(order.dropoffInfo.name);
        }

        mOrder = order;
    }


    private Order getOrderInfo(ArrayList<Order> orders, int oid) {
        Order order = null;

        for (Order o : orders) {
            if (o.oid == oid)
                order = o;
        }

        return order;
    }

    private void setPDFaceImage(ImageView imageView, String imageInfo) {
        ImageLoader imageLoader = RequestQueue.getInstance(this).getImageLoader();
        imageLoader.get(Config.SERVER_ADDRESS + imageInfo,
                ImageLoader.getImageListener(
                        imageView, R.drawable.ic_sale, R.drawable.ic_sale
                ));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_feedback_send:
                if (checkAvailable())
                    sendFeedback();
                break;
        }
    }

    private boolean checkAvailable() {
        if (mRatingStar >= 0)
            return true;

        return false;
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        mRatingStar = (int) rating;
    }

    private Feedback makeFeedback() {
        Feedback feedback = new Feedback();

        feedback.oid = mOrder.oid;
        feedback.rate = mRatingStar;
        feedback.memo = mEditTextFree.getText().toString();

        if (mCheckBoxTime.isChecked())
            feedback.time = 0;
        else
            feedback.time = 1;

        if (mCheckBoxLaundry.isChecked())
            feedback.quality = 0;
        else
            feedback.quality = 1;

        if (mCheckBoxKindness.isChecked())
            feedback.kindness = 0;
        else
            feedback.kindness = 1;

        return feedback;
    }

    private void sendFeedback() {
        showProgress(true);

        Feedback feedback = makeFeedback();
        String body = CleanBasketApplication.getInstance().getGson().toJson(feedback);

        PostRequest postRequest = new PostRequest(this);

        try {
            JSONObject jsonObject = new JSONObject(body);
            postRequest.setParams(jsonObject);
        } catch (JSONException e) {

        }

        postRequest.setUrl(AddressManager.RATE_ORDER);
        postRequest.setListener(this, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getBaseContext(), getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        });
        RequestQueue.getInstance(this).addToRequestQueue(postRequest.doRequest());
    }

    @Override
    public void onResponse(JSONObject response) {
        showProgress(false);

        JsonData jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);
        switch (jsonData.constant) {
            case Constants.ERROR:
                Toast.makeText(this, R.string.feedback_failure, Toast.LENGTH_SHORT).show();
                finish();
            case Constants.SUCCESS:
                Toast.makeText(this, R.string.feedback_success, Toast.LENGTH_SHORT).show();
                finish();
                break;
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

            mFeedbackFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mFeedbackFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFeedbackFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
