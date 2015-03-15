package com.washappkorea.corp.cleanbasket.ui.dialog;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.google.gson.JsonSyntaxException;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.listener.NetworkErrorListener;
import com.washappkorea.corp.cleanbasket.io.model.JsonData;
import com.washappkorea.corp.cleanbasket.io.request.GetRequest;
import com.washappkorea.corp.cleanbasket.util.AddressManager;
import com.washappkorea.corp.cleanbasket.util.Constants;

public class FindPasswordDialog extends DialogFragment {
    private static final String TAG = FindPasswordDialog.class.getSimpleName();

    // UI references.
    private TextView mTextViewInfo;
    private EditText mEditTextEmail;

    private View mEmailFormView;
    private View mProgressView;

    public static FindPasswordDialog newInstance() {
        FindPasswordDialog md = new FindPasswordDialog();
        md.initialize();

        return md;
    }

    public void initialize() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getShowsDialog()) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        View rootView = inflater.inflate(R.layout.dialog_find_password, container, false);

        // Set up the login form.
        mTextViewInfo = (TextView) rootView.findViewById(R.id.find_password_inform);

        mEditTextEmail = (EditText) rootView.findViewById(R.id.edittext_find_email);
        mEditTextEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.use || id == EditorInfo.IME_ACTION_DONE) {
                    attemptUse();
                    return true;
                }

                return false;
            }
        });

        Button buttonSendEmail = (Button) rootView.findViewById(R.id.button_send_email);
        buttonSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptUse();
            }
        });

        mTextViewInfo.setText(getString(R.string.find_password_inform));

        mEmailFormView = rootView.findViewById(R.id.find_password_form);
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
    public void attemptUse() {
        // Reset errors.
        mEditTextEmail.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(mEditTextEmail.getText().toString())) {
            mEditTextEmail.setError(getString(R.string.mileage_short));
            focusView = mEditTextEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            sendEmail();
        }
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditTextEmail.getWindowToken(), 0);
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

            mEmailFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mEmailFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mEmailFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mEmailFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void sendEmail() {
        showProgress(true);
        GetRequest getRequest = new GetRequest(getActivity());
        getRequest.setUrl(AddressManager.FIND_PASSWORD);
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
                    case Constants.EMAIL_ERROR:
                        CleanBasketApplication.getInstance().showToast(getString(R.string.email_unknwon));
                        break;

                    case Constants.SUCCESS:
                        CleanBasketApplication.getInstance().showToast(getString(R.string.success_password_change));
                        hideSoftKeyBoard();
                        dismiss();
                        showProgress(false);
                        break;
                }
            }
        }, new NetworkErrorListener(getActivity()));
        RequestQueue.getInstance(getActivity()).addToRequestQueue(getRequest.doRequest());
    }
}