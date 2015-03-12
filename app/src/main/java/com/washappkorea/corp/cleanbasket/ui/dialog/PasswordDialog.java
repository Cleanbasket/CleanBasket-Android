package com.washappkorea.corp.cleanbasket.ui.dialog;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.toolbox.RequestFuture;
import com.google.gson.JsonSyntaxException;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.model.JsonData;
import com.washappkorea.corp.cleanbasket.io.request.PostRequest;
import com.washappkorea.corp.cleanbasket.util.AddressManager;
import com.washappkorea.corp.cleanbasket.util.Constants;

import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class PasswordDialog extends android.app.DialogFragment {
    private static final String TAG = PasswordDialog.class.getSimpleName();

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mPasswordView;
    private EditText mPasswordRepeatView;
    private View mPasswordFormView;
    private View mProgressView;

    public static PasswordDialog newInstance() {
        PasswordDialog pd = new PasswordDialog();
        pd.initialize();

        return pd;
    }

    public void initialize() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if (getShowsDialog()) {
//            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        }

        getDialog().setTitle(getString(R.string.pref_change_password));

        View rootView = inflater.inflate(R.layout.dialog_password, container, false);

        mPasswordView = (EditText) rootView.findViewById(R.id.edittext_password);
        mPasswordRepeatView = (EditText) rootView.findViewById(R.id.edittext_password_repeat);

        Button mPasswordChangeButton = (Button) rootView.findViewById(R.id.password_change_button);
        mPasswordChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptChange();
            }
        });

        mPasswordFormView = rootView.findViewById(R.id.password_change);
        mProgressView = rootView.findViewById(R.id.login_progress);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        int height = getDialog().getWindow().getAttributes().height;
        getDialog().getWindow().setLayout(width, height);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptChange() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPasswordView.setError(null);
        mPasswordRepeatView.setError(null);

        // Store values at the time of the login attempt.
        String password = mPasswordView.getText().toString();
        String repeatPassword = mPasswordRepeatView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(repeatPassword) && !isPasswordValid(repeatPassword)) {
            mPasswordRepeatView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordRepeatView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (TextUtils.isEmpty(repeatPassword)) {
            mPasswordRepeatView.setError(getString(R.string.error_field_required));
            focusView = mPasswordRepeatView;
            cancel = true;
        } else if (!password.equals(repeatPassword)) {
            mPasswordRepeatView.setError(getString(R.string.error_incorrect_repeat));
            focusView = mPasswordRepeatView;
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
            mAuthTask = new UserLoginTask(password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
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

            mPasswordFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mPasswordFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPasswordFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mPasswordFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {
        private final String mPassword;

        UserLoginTask(String password) {
            mPassword = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
            PostRequest postRequest = new PostRequest(getActivity());
            postRequest.setParams("password", mPassword);
            postRequest.setUrl(AddressManager.SET_PASSWORD);
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
                case Constants.SESSION_EXPIRED:
                    CleanBasketApplication.getInstance().showToast(getString(R.string.session_invalid));
                    break;
                case Constants.ERROR:
                    Toast.makeText(getActivity(), R.string.error_password_change, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.SUCCESS:
                    Toast.makeText(getActivity(), R.string.success_password_change, Toast.LENGTH_SHORT).show();
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