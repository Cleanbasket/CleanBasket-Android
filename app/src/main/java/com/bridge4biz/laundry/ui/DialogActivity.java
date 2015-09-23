package com.bridge4biz.laundry.ui;


import android.app.NotificationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.request.GetRequest;
import com.bridge4biz.laundry.ui.dialog.MessageDialog;
import com.bridge4biz.laundry.ui.dialog.ModifyDateTimeDialog;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;

public class DialogActivity extends FragmentActivity implements MessageDialog.OnDialogDismissListener, ModifyDateTimeDialog.OnDialogDismissListener {
    private static final String TAG = DialogActivity.class.getSimpleName();
    public static final String MESSAGE_DIALOG = "MESSAGE_DIALOG";
    public static final String COUPON_DIALOG = "COUPON_DIALOG";
    public static final String MODIFY_TIME_DIALOG = "MODIFY_TIME_DIALOG";
    public static final String CONFIRM_TIME_DIALOG = "CONFIRM_TIME_DIALOG";
    public static final String MODIFY_TIME_BY_SERVER = "MODIFY_TIME_BY_SERVER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cancelNotification();

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("tag")) {
            String tag = getIntent().getExtras().getString("tag");
            if (tag.equals(MESSAGE_DIALOG)) {
                String message = getIntent().getExtras().getString("message");
                MessageDialog md = MessageDialog.newInstance(this, message);

                md.show(getSupportFragmentManager(), tag);
            }
            else if (tag.equals(COUPON_DIALOG)) {

            }
            else if (tag.equals(CONFIRM_TIME_DIALOG)) {
                if(getIntent().getExtras().containsKey("oid")) {
                    int oid = getIntent().getExtras().getInt("oid");

                    Log.i(TAG, oid + "");

                    sendConfirm(oid);
                }
            }
            else if (tag.equals(MODIFY_TIME_DIALOG)) {
                if(getIntent().getExtras().containsKey("oid")) {
                    int oid = getIntent().getExtras().getInt("oid");

                    Log.i(TAG, oid + "");

                    ModifyDateTimeDialog mdtd = ModifyDateTimeDialog.newInstance(this, oid);

                    mdtd.show(getSupportFragmentManager(), tag);
                }
            }
            else if (tag.equals(MODIFY_TIME_BY_SERVER)) {
                if(getIntent().getExtras().containsKey("oid")) {
                    int oid = getIntent().getExtras().getInt("oid");

                    Log.i(TAG, oid + "");

                    ModifyDateTimeDialog mdtd = ModifyDateTimeDialog.newInstance(this, oid);

                    mdtd.show(getSupportFragmentManager(), tag);
                }
            }
        }
    }

    public void cancelNotification() {
        String ns = this.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) this.getSystemService(ns);
        nMgr.cancelAll();
    }

    private void sendConfirm(int oid) {
        GetRequest getRequest = new GetRequest(this);

        getRequest.setUrl(AddressManager.CONFIRM_ORDER);
        getRequest.setParams("oid", oid);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonData jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);

                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        CleanBasketApplication.getInstance().showToast(getString(R.string.time_confirmed));
                        finish();
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                CleanBasketApplication.getInstance().showToast(getString(R.string.general_error));
                finish();
            }
        });
        RequestQueue.getInstance(this).addToRequestQueue(getRequest.doRequest());
    }

    @Override
    public void onDialogDismiss() {
        finish();
    }
}
