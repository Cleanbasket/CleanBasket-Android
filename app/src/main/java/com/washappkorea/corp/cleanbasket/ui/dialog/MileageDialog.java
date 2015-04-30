package com.washappkorea.corp.cleanbasket.ui.dialog;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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

public class MileageDialog extends DialogFragment {
    private static final String TAG = MileageDialog.class.getSimpleName();

    private OnMileageSetListener mOnMileageSetListener;

    // UI references.
    private TextView mTextViewInfo;
    private EditText mEditTextMileage;

    private View mMileageFormView;
    private View mProgressView;

    private int mAvailableMileage = 0;

    public interface OnMileageSetListener {
        void onMileageSet(MileageDialog dialog, int mileage);
    }

    public static interface OnDialogDismissListener {
        public abstract void onDialogDismiss(DialogInterface dialoginterface);
    }

    public static MileageDialog newInstance(OnMileageSetListener onMileageSetListener) {
        MileageDialog md = new MileageDialog();
        md.initialize(onMileageSetListener);

        return md;
    }

    public void initialize(OnMileageSetListener onMileageSetListener) {
        this.mOnMileageSetListener = onMileageSetListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getShowsDialog()) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        View rootView = inflater.inflate(R.layout.dialog_mileage, container, false);

        // Set up the login form.
        mTextViewInfo = (TextView) rootView.findViewById(R.id.current_mileage);

        mEditTextMileage = (EditText) rootView.findViewById(R.id.use_mileage);
        mEditTextMileage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.use || id == EditorInfo.IME_ACTION_DONE) {
                    attemptUse();
                    return true;
                }

                return false;
            }
        });

        Button buttonUseMileage = (Button) rootView.findViewById(R.id.button_use_mileage);
        buttonUseMileage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptUse();
            }
        });

        mTextViewInfo.setText(getString(R.string.mileage_available) + " " + mAvailableMileage + getString(R.string.point_unit));

        mMileageFormView = rootView.findViewById(R.id.mileage_form);
        mProgressView = rootView.findViewById(R.id.login_progress);

        return rootView;
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

        getMileage();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptUse() {
        // Reset errors.
        mEditTextMileage.setError(null);

        // Store values at the time of the login attempt.
        int mileage = 0;
        String inputValue = mEditTextMileage.getText().toString();

        if (!inputValue.equals(""))
            mileage = Integer.parseInt(mEditTextMileage.getText().toString());

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (mileage > mAvailableMileage) {
            mEditTextMileage.setError(getString(R.string.mileage_short));
            focusView = mEditTextMileage;
            cancel = true;
        }

        if (mileage % 100 > 0) {
            mEditTextMileage.setError(getString(R.string.mileage_info));
            focusView = mEditTextMileage;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mOnMileageSetListener.onMileageSet(this, mileage);
            hideSoftKeyBoard();
            dismiss();
        }
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditTextMileage.getWindowToken(), 0);
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

            mMileageFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mMileageFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mMileageFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mMileageFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void getMileage() {
        showProgress(true);
        GetRequest getRequest = new GetRequest(getActivity());
        getRequest.setUrl(AddressManager.GET_MILEAGE);
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
                        int mileage = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, Integer.class);
                        updateMileage(mileage);
                        showProgress(false);
                        break;
                }
            }
        }, new NetworkErrorListener(getActivity()));
        RequestQueue.getInstance(getActivity()).addToRequestQueue(getRequest.doRequest());
    }

    private void updateMileage(int mileage) {
        mAvailableMileage = mileage;
        mTextViewInfo.setText(getString(R.string.mileage_available) + " " + mAvailableMileage);
    }
}