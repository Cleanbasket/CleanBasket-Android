package com.bridge4biz.laundry.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.model.Payment;
import com.bridge4biz.laundry.io.request.PostRequest;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class AddPaymentActivity extends Activity implements View.OnClickListener, Response.Listener<JSONObject>, Response.ErrorListener {
    private int SCAN_REQUEST_CODE = 100;

    private EditText editTextCard1;
    private EditText editTextCard2;
    private EditText editTextCard3;
    private EditText editTextCard4;
    private EditText editTextExpM;
    private EditText editTextExpY;
    private EditText editTextBirthDay;
    private EditText editTextPassword;
    private EditText editTextNickName;
    private CheckBox checkBoxAgree;
    private View mProgressView;

    private boolean progress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_payment);

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
        customTitle.setText(getString(R.string.offsite_payment_card));

        editTextCard1 = (EditText) findViewById(R.id.edittext_card_number1);
        editTextCard2 = (EditText) findViewById(R.id.edittext_card_number2);
        editTextCard3 = (EditText) findViewById(R.id.edittext_card_number3);
        editTextCard4 = (EditText) findViewById(R.id.edittext_card_number4);
        final LinearLayout buttonScan = (LinearLayout) findViewById(R.id.button_scan);
        editTextExpM = (EditText) findViewById(R.id.edittext_month);
        editTextExpY = (EditText) findViewById(R.id.edittext_year);
        editTextBirthDay = (EditText) findViewById(R.id.edittext_birthday);
        editTextPassword = (EditText) findViewById(R.id.edittext_password);
        editTextNickName = (EditText) findViewById(R.id.edittext_nickname);
        checkBoxAgree = (CheckBox) findViewById(R.id.checkbox_agree);
        Button button = (Button) findViewById(R.id.card_register_button);
        mProgressView = findViewById(R.id.loading_progress);

        button.setOnClickListener(this);

        editTextCard1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 3) {
                    editTextCard2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editTextCard2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 3) {
                    editTextCard3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editTextCard3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 3) {
                    editTextCard4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editTextCard4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 3) {
                    editTextExpM.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editTextExpM.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 1) {
                    editTextExpY.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editTextExpY.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 1) {
                    editTextBirthDay.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editTextBirthDay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 5) {
                    editTextPassword.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 1) {
                    editTextNickName.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        buttonScan.setOnClickListener(this);

        StringBuilder html = new StringBuilder();

        if (Locale.getDefault().getLanguage().equals("ko") || Locale.getDefault().equals("kr")) {
            html.append("NICEPAY ");
            html.append("<a href='com.bridge4biz.laundry.ui.WebViewAgreementActivity://nice_privacy'>");
            html.append(getString(R.string.service));
            html.append("</a> ");
            html.append(getString(R.string.agreement));
        }
        else {
            html.append(getString(R.string.agreement) + " ");
            html.append("<a href='com.bridge4biz.laundry.ui.WebViewAgreementActivity://nice_privacy'>");
            html.append(getString(R.string.service));
            html.append("</a>");
        }

        checkBoxAgree.setClickable(false);
        checkBoxAgree.setText(Html.fromHtml(html.toString()));
        checkBoxAgree.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_scan) {
            if (CardIOActivity.canReadCardWithCamera()) {
                Intent scanIntent = new Intent(this, CardIOActivity.class);

                // customize these values to suit your needs.
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false); // default: false
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false

                // hides the manual entry button
                // if set, developers should provide their own manual entry mechanism in the app
                scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false); // default: false

                // matches the theme of your application
                scanIntent.putExtra(CardIOActivity.EXTRA_KEEP_APPLICATION_THEME, false); // default: false

                // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
                startActivityForResult(scanIntent, SCAN_REQUEST_CODE);
            }
        }
        else if (v.getId() == R.id.card_register_button) {
            if (!checkValid() || !checkBoxAgree.isChecked()) {
                CleanBasketApplication.getInstance().showToast(getString(R.string.service_not_agree));
                return;
            }
            else
                registerCard();
        }
    }

    private void finishRegisterCard(Payment payment) {
        Intent data = new Intent();
        Bundle args = new Bundle();

        args.putString(CleanBasketApplication.PAYMENT_CARD_NAME, payment.getCardName());
        args.putString(CleanBasketApplication.PAYMENT_AUTH_DATE, payment.getAuthDate());

        data.putExtras(args);

        setResult(OrderInfoFragment.PAYMENT_RESULT, data);

        finish();
    }

    private void showProgress() {
        mProgressView.setVisibility(View.VISIBLE);
        progress = true;
    }

    private void finishProgress() {
        mProgressView.setVisibility(View.INVISIBLE);
        progress = false;
    }

    @Override
    public void onResponse(JSONObject response) {
        JsonData jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);

        Payment payment;

        switch (jsonData.constant) {
            case Constants.ERROR:
                payment = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, Payment.class);
                CleanBasketApplication.getInstance().showToast(payment.getResultMsg());
                finishProgress();
                break;

            case Constants.SUCCESS:
                CleanBasketApplication.getInstance().showToast(getString(R.string.success));
                payment = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, Payment.class);
                finishRegisterCard(payment);
                finishProgress();
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        CleanBasketApplication.getInstance().showToast(getString(R.string.general_error));

        finishProgress();
    }

    private void registerCard() {
        if (progress) return;

        showProgress();

        HashMap<String, String> map = new HashMap<String, String>();

        map.put("CardNo", editTextCard1.getText().toString() + editTextCard2.getText().toString() + editTextCard3.getText().toString() + editTextCard4.getText().toString());
        map.put("ExpMonth", editTextExpM.getText().toString());
        map.put("ExpYear", editTextExpY.getText().toString());
        map.put("IDNo", editTextBirthDay.getText().toString());
        map.put("CardPw", editTextPassword.getText().toString());

        PostRequest postRequest = new PostRequest(this);
        postRequest.setParams(new JSONObject(map));

        postRequest.setUrl(AddressManager.ADD_PAYMENT);
        postRequest.setListener(this, this);
        RequestQueue.getInstance(this).addToRequestQueue(postRequest.doRequest().setRetryPolicy(
                new DefaultRetryPolicy(
                        (int) TimeUnit.SECONDS.toMillis(60),
                        Config.DEFAULT_MAX_RETRIES,
                        Config.DEFAULT_BACKOFF_MULT)));

        CleanBasketApplication.getInstance().showToast(getString(R.string.card_register));
    }

    private boolean checkValid() {
        if (TextUtils.isEmpty(editTextCard1.getText()) || editTextCard1.getText().length() != 4) {
            editTextCard1.setError(getString(R.string.empty));
            return false;
        }

        if (TextUtils.isEmpty(editTextCard2.getText()) || editTextCard2.getText().length() != 4) {
            editTextCard2.setError(getString(R.string.empty));
            return false;
        }

        if (TextUtils.isEmpty(editTextCard3.getText()) || editTextCard3.getText().length() != 4) {
            editTextCard3.setError(getString(R.string.empty));
            return false;
        }

        if (TextUtils.isEmpty(editTextCard4.getText()) || editTextCard4.getText().length() != 4) {
            editTextCard4.setError(getString(R.string.empty));
            return false;
        }

        if (TextUtils.isEmpty(editTextExpM.getText()) || editTextExpM.getText().length() != 2) {
            editTextExpM.setError(getString(R.string.empty));
            return false;
        }

        if (TextUtils.isEmpty(editTextExpY.getText()) || editTextExpY.getText().length() != 2) {
            editTextExpY.setError(getString(R.string.empty));
            return false;
        }

        if (TextUtils.isEmpty(editTextBirthDay.getText()) || editTextBirthDay.getText().length() != 6) {
            editTextBirthDay.setError(getString(R.string.empty));
            return false;
        }

        if (TextUtils.isEmpty(editTextPassword.getText()) || editTextPassword.getText().length() != 2) {
            editTextPassword.setError(getString(R.string.empty));
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
            CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

            editTextCard1.setText(scanResult.cardNumber.substring(0, 4));
            editTextCard2.setText(scanResult.cardNumber.substring(4, 8));
            editTextCard3.setText(scanResult.cardNumber.substring(8, 12));
            editTextCard4.setText(scanResult.cardNumber.substring(12, 16));

            if (scanResult.isExpiryValid()) {
                if (scanResult.expiryMonth < 10)
                    editTextExpM.setText("0" + scanResult.expiryMonth);
                else
                    editTextExpM.setText("" + scanResult.expiryMonth);

                editTextExpY.setText((scanResult.expiryYear - 2000) + "");
            }
        }
    }
}
