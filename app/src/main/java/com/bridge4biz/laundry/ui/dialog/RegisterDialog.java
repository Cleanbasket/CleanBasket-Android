package com.bridge4biz.laundry.ui.dialog;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.toolbox.RequestFuture;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.model.UserData;
import com.bridge4biz.laundry.io.request.PostRequest;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;
import com.bridge4biz.laundry.util.InputValidationChecker;
import com.bridge4biz.laundry.util.UserEmailFetcher;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class RegisterDialog extends DialogFragment implements EditText.OnEditorActionListener {
    private static final String TAG = RegisterDialog.class.getSimpleName();

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mPasswordRepeatView;
    private View mRegisterFormView;
    private View mProgressView;

    private OnRegisterListener mOnRegisterListener;

    public static RegisterDialog newInstance(OnRegisterListener onRegisterListener) {
        RegisterDialog ed = new RegisterDialog();
        ed.initialize(onRegisterListener);

        return ed;
    }

    public void initialize(OnRegisterListener onRegisterListener) {
        this.mOnRegisterListener = onRegisterListener;
    }

    public interface OnRegisterListener {
        void onRegister(String email);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if (getShowsDialog()) {
//            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        }

        getDialog().setTitle(Html.fromHtml("<font color='#FFFFFF'>" + getString(R.string.action_sign_up) + "</font>"));
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        int color = getResources().getColor(R.color.dialog_color);
        int titleDividerId = getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = getDialog().getWindow().getDecorView().findViewById(titleDividerId);
        titleDivider.setBackgroundColor(color);

        View rootView = inflater.inflate(R.layout.dialog_register, container, false);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) rootView.findViewById(R.id.email);
        mPasswordView = (EditText) rootView.findViewById(R.id.password);
        mPasswordRepeatView = (EditText) rootView.findViewById(R.id.password_repeat);

        mEmailView.setOnEditorActionListener(this);
        mPasswordView.setOnEditorActionListener(this);
        mPasswordRepeatView.setOnEditorActionListener(this);

        mEmailView.setText(UserEmailFetcher.getEmail(getActivity()));

        Button emailSignUpButton = (Button) rootView.findViewById(R.id.email_sign_in_button);
        emailSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mRegisterFormView = rootView.findViewById(R.id.login_form);
        mProgressView = rootView.findViewById(R.id.login_progress);

        return rootView;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
            case R.id.email:
                mPasswordView.requestFocus();
                return true;

            case R.id.password:
                mPasswordRepeatView.requestFocus();
                return true;

            case R.id.action_done:
                attemptRegister();
                return true;
        }

        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        int height = getDialog().getWindow().getAttributes().height;

        WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
        lp.dimAmount = 0.9f;

        getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getDialog().getWindow().setLayout(width, height);
        getDialog().getWindow().setAttributes(lp);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPasswordRepeatView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passwordRepeat = mPasswordRepeatView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!InputValidationChecker.getInstance(getActivity()).isPasswordShort(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!InputValidationChecker.getInstance(getActivity()).isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.password_digit_combination));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(passwordRepeat) || !password.equals(passwordRepeat)) {
            mPasswordRepeatView.setError(getString(R.string.error_incorrect_password));
            focusView = mPasswordRepeatView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!InputValidationChecker.getInstance(getActivity()).isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserRegisterTask(email, password);
            mAuthTask.execute((Void) null);
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

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, Integer> {
        private final String mEmail;
        private final String mPassword;

        UserRegisterTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            UserData userData = new UserData();
            userData.email = mEmail;
            userData.password = mPassword;

            RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
            PostRequest postRequest = new PostRequest(getActivity());
            String body = CleanBasketApplication.getInstance().getGson().toJson(userData);

            try {
                JSONObject jsonObject = new JSONObject(body);
                postRequest.setParams(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
                return Constants.ERROR;
            }

            postRequest.setUrl(AddressManager.REGISTER);
            postRequest.setListener(requestFuture, requestFuture);
            RequestQueue.getInstance(getActivity()).addToRequestQueue(postRequest.doRequest());

            JSONObject response = null;

            try {
                response = requestFuture.get();
            } catch (InterruptedException e) {
                Log.i(TAG, e.toString());
            } catch (ExecutionException e) {
                Log.i(TAG, e.toString());
            }

            JsonData jsonData = null;

            try {
                jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);
            } catch (JsonSyntaxException e) {
                return Constants.ERROR;
            } catch (NullPointerException e) {
                return Constants.ERROR;
            }

            return jsonData.constant;
        }

        @Override
        protected void onPostExecute(Integer constant) {
            mAuthTask = null;
            showProgress(false);

            if (constant < 0) {
                CleanBasketApplication.getInstance().showToast(getString(R.string.network_error));

                return;
            }

            switch (constant) {
                case Constants.ACCOUNT_DUPLICATION:
                    mEmailView.setError(getString(R.string.account_duplication));
                    mEmailView.requestFocus();
                    break;

                case Constants.ERROR:
                    CleanBasketApplication.getInstance().showToast(getString(R.string.toast_error));
                    break;

                case Constants.SUCCESS:
                    CleanBasketApplication.getInstance().showToast(getString(R.string.sign_up_success));
                    mOnRegisterListener.onRegister(mEmail);
                    dismiss();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}