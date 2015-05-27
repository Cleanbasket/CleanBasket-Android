package com.bridge4biz.laundry.ui;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.Feedback;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.model.Order;
import com.bridge4biz.laundry.io.request.GetRequest;
import com.bridge4biz.laundry.io.request.PostRequest;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class FeedbackActivity extends BaseActivity implements View.OnClickListener, CheckBox.OnCheckedChangeListener, Response.Listener<JSONObject>, RatingBar.OnRatingBarChangeListener {
    private static final String TAG = FeedbackActivity.class.getSimpleName();

    private Order mOrder;

    private ImageView mImageViewPdFace;
    private TextView mTextViewUserName;
    private RatingBar mRatingBar;
    private EditText mEditTextFree;
    private CheckBox mCheckBoxTime;
    private CheckBox mCheckBoxLaundry;
    private CheckBox mCheckBoxKindness;
    private RelativeLayout mButton;

    private int mOid;
    private int mRatingStar;

    private View mFeedbackFormView;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feedback);

        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(R.layout.action_layout);
        TextView customTitle = (TextView) getActionBar().getCustomView().findViewById(R.id.actionbar_title);
        ImageView backButton = (ImageView) getActionBar().getCustomView().findViewById(R.id.imageview_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        customTitle.setText(getString(R.string.feedback_title));

        mFeedbackFormView = findViewById(R.id.feedback_form);
        mProgressView = findViewById(R.id.feedback_progress);

        mImageViewPdFace = (ImageView) findViewById(R.id.imageview_pd_face);
        mTextViewUserName = (TextView) findViewById(R.id.textview_user_name);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        mEditTextFree = (EditText) findViewById(R.id.edittext_free_feedback);
        mCheckBoxTime = (CheckBox) findViewById(R.id.checkbox_late_feedback);
        mCheckBoxLaundry = (CheckBox) findViewById(R.id.checkbox_laundry_feedback);
        mCheckBoxKindness = (CheckBox) findViewById(R.id.checkbox_kindness_feedback);

        mButton = (RelativeLayout) findViewById(R.id.button_feedback_send);
        mButton.setOnClickListener(this);
        mRatingBar.setOnRatingBarChangeListener(this);

        mEditTextFree.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case R.id.finish:
                        if (checkAvailable())
                            sendFeedback();
                        return true;
                }

                return false;
            }
        });

        mCheckBoxTime.setOnCheckedChangeListener(this);
        mCheckBoxLaundry.setOnCheckedChangeListener(this);
        mCheckBoxKindness.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.checkbox_late_feedback:
                if (mCheckBoxTime.isChecked())
                    mCheckBoxTime.setBackgroundResource(R.drawable.button_green);
                else
                    mCheckBoxTime.setBackgroundResource(R.drawable.textview_back_grey);
                break;

            case R.id.checkbox_laundry_feedback:
                if (mCheckBoxLaundry.isChecked())
                    mCheckBoxLaundry.setBackgroundResource(R.drawable.button_green);
                else
                    mCheckBoxLaundry.setBackgroundResource(R.drawable.textview_back_grey);
                break;

            case R.id.checkbox_kindness_feedback:
                if (mCheckBoxKindness.isChecked())
                    mCheckBoxKindness.setBackgroundResource(R.drawable.button_green);
                else
                    mCheckBoxKindness.setBackgroundResource(R.drawable.textview_back_grey);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mRatingStar = -1;

        mOid = getIntent().getIntExtra("oid", 0);

        Log.i(TAG, mOid + "");

        confirmFeedback();
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

        CleanBasketApplication.getInstance().showToast(getString(R.string.feedback_empty));

        return false;
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        mRatingStar = (int) rating;
        Log.i(TAG, rating + "");
    }

    private Feedback makeFeedback() {
        Feedback feedback = new Feedback();

        if (mOrder != null)
            feedback.oid = mOrder.oid;
        else
            feedback.oid = mOid;

        feedback.rate = mRatingStar;
        feedback.memo = mEditTextFree.getText().toString();

        if (mCheckBoxTime.isChecked())
            feedback.time = 1;
        else
            feedback.time = 0;

        if (mCheckBoxLaundry.isChecked())
            feedback.quality = 1;
        else
            feedback.quality = 0;

        if (mCheckBoxKindness.isChecked())
            feedback.kindness = 1;
        else
            feedback.kindness = 0;

        return feedback;
    }

    private void confirmFeedback() {
        showProgress(true);

        GetRequest getRequest = new GetRequest(this);

        getRequest.setUrl(AddressManager.RATE_ORDER);
        getRequest.setParams("oid", mOid);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonData jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);

                switch (jsonData.constant) {
                    case Constants.DUPLICATION_FEEDBACK:
                        CleanBasketApplication.getInstance().showToast(getString(R.string.feedback_duplication));
                        showProgress(false);
                        finish();
                        break;
                    case Constants.SUCCESS:
                        showProgress(false);
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                CleanBasketApplication.getInstance().showToast(getString(R.string.general_error));
                showProgress(false);
                finish();
            }
        });
        RequestQueue.getInstance(this).addToRequestQueue(getRequest.doRequest());
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
                CleanBasketApplication.getInstance().showToast(getString(R.string.toast_error));
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
                CleanBasketApplication.getInstance().showToast(getString(R.string.feedback_failure));
                finish();
            case Constants.SUCCESS:
                CleanBasketApplication.getInstance().showToast(getString(R.string.feedback_success));
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
