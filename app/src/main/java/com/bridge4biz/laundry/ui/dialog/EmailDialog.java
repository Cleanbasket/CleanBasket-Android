package com.bridge4biz.laundry.ui.dialog;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.bridge4biz.laundry.io.request.StringRequest;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;
import com.bridge4biz.laundry.util.UserEmailFetcher;
import com.google.gson.JsonSyntaxException;

import java.util.concurrent.ExecutionException;

public class EmailDialog extends DialogFragment implements EditText.OnEditorActionListener {
    private static final String TAG = EmailDialog.class.getSimpleName();
    private static final String REGISTER_TAG = "REGISTER";
    private static final String FIND_PASSWORD_TAG = "FIND_PASSWORD";
    private static final String EMAIL = "EMAIL";

    private OnLoginSuccess mOnLoginSuccess;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mProgressView;

    public interface OnLoginSuccess {
        void onLoginSuccess();
    }

    public static EmailDialog newInstance(OnLoginSuccess onLoginSuccess) {
        EmailDialog ed = new EmailDialog();
        ed.initialize(onLoginSuccess);

        return ed;
    }

    public void initialize(OnLoginSuccess onLoginSuccess) {
        this.mOnLoginSuccess = onLoginSuccess;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if (getShowsDialog()) {
//            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        }

        getDialog().setTitle(Html.fromHtml("<font color='#FFFFFF'>" + getString(R.string.action_sign_in_email) + "</font>"));
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        int color = getResources().getColor(R.color.dialog_color);
        int titleDividerId = getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = getDialog().getWindow().getDecorView().findViewById(titleDividerId);
        titleDivider.setBackgroundColor(color);

        View rootView = inflater.inflate(R.layout.dialog_login, container, false);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) rootView.findViewById(R.id.email);
        mPasswordView = (EditText) rootView.findViewById(R.id.password);

        mEmailView.setOnEditorActionListener(this);
        mPasswordView.setOnEditorActionListener(this);

        Button emailSignInButton = (Button) rootView.findViewById(R.id.email_sign_in_button);
        emailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        TextView textViewSignUp = (TextView) rootView.findViewById(R.id.textview_sign_up);
        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popRegisterDialog();
            }
        });

        TextView textViewFindPassword = (TextView) rootView.findViewById(R.id.textview_find_password);
        textViewFindPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popFindPasswordDialog();
            }
        });

        mLoginFormView = rootView.findViewById(R.id.login_form);
        mProgressView = rootView.findViewById(R.id.login_progress);

        final SharedPreferences prefs = getEmailPreference();
        String email = prefs.getString(EMAIL, "");

        if (TextUtils.isEmpty(email))
            mEmailView.setText(UserEmailFetcher.getEmail(getActivity()));
        else
            mEmailView.setText(email);

        return rootView;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
            case R.id.email:
                mPasswordView.requestFocus();
                return true;

            case R.id.login:
                attemptLogin();
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

    private void popRegisterDialog() {
        RegisterDialog rd = RegisterDialog.newInstance(new RegisterDialog.OnRegisterListener() {
            @Override
            public void onRegister(String email) {
                mEmailView.setText(email);
            }
        });

        rd.show(getFragmentManager(), REGISTER_TAG);
    }

    private void popFindPasswordDialog() {
        FindPasswordDialog fpd = FindPasswordDialog.newInstance();
        fpd.show(getFragmentManager(), FIND_PASSWORD_TAG);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
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
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        showProgress(false);
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {
        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            RequestFuture<String> requestFuture = RequestFuture.newFuture();
            StringRequest stringRequest = new StringRequest(getActivity());
            stringRequest.setParams("email", mEmail);
            stringRequest.setParams("password", mPassword);
            stringRequest.setParams("remember", "true");
            stringRequest.setUrl(AddressManager.LOGIN);
            stringRequest.setListener(requestFuture, requestFuture);
            RequestQueue.getInstance(getActivity()).addToRequestQueue(stringRequest.doRequest());

            String response = null;

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
                case Constants.SESSION_EXPIRED:
                    CleanBasketApplication.getInstance().showToast(getString(R.string.session_invalid));
                    break;
                case Constants.EMAIL_ERROR:
                    mEmailView.setError(getString(R.string.error_invalid_email));
                    mEmailView.requestFocus();
                    break;
                case Constants.PASSWORD_ERROR:
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                    break;
                case Constants.ACCOUNT_DISABLED:
                    CleanBasketApplication.getInstance().showToast(getString(R.string.disable_error));
                    break;
                case Constants.SUCCESS:
                    storeEmail(mEmailView.getText().toString());
                    mOnLoginSuccess.onLoginSuccess();
                    CleanBasketApplication.getInstance().storeRegistrationId(getActivity(), null);
                    dismiss();

                    Intent intent = new Intent();
                    intent.setAction("com.bridge4biz.laundry.ui.MainActivity");
                    startActivity(intent);
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void storeEmail(String email) {
        final SharedPreferences prefs = getEmailPreference();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(EMAIL, email);
        editor.commit();
    }

    private SharedPreferences getEmailPreference() {
        return getActivity().getSharedPreferences(EMAIL, Context.MODE_PRIVATE);
    }
}