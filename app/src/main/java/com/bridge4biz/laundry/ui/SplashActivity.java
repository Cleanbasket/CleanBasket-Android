package com.bridge4biz.laundry.ui;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.request.GetRequest;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;
import com.google.gson.JsonSyntaxException;

public class SplashActivity extends Activity implements Response.Listener<String>, Response.ErrorListener {
    private static final String TAG = SplashActivity.class.getSimpleName();

    public static final String UID = "uid";
    public static final String USER_ID = "user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();

        GetRequest getRequest = new GetRequest(this);
        getRequest.setUrl(AddressManager.LOGIN_CHECK);
        getRequest.setListener(this, this);
        RequestQueue.getInstance(this).addToRequestQueue(getRequest.doRequest());
    }

    @Override
    public void onResponse(String response) {
        Log.i(TAG, response);

        JsonData jsonData = null;

        try {
            jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response, JsonData.class);
        }
        catch (JsonSyntaxException e) {
            redirectToLoginActivity();

            return;
        }

        switch (jsonData.constant) {
            case Constants.SESSION_VALID:
                storeUid(getBaseContext(), Integer.parseInt(jsonData.data));
                redirectToMainActivity();
                break;

            case Constants.SESSION_EXPIRED:
                redirectToLoginActivity();
                break;
        }
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        redirectToLoginActivity();
    }

    private void redirectToLoginActivity() {
        Intent intent = new Intent();
        intent.setAction("com.bridge4biz.laundry.ui.LoginActivity");
        startActivity(intent);

        super.finish();
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent();
        intent.setAction("com.bridge4biz.laundry.ui.MainActivity");
        startActivity(intent);

        super.finish();
    }

    private void storeUid(Context context, Integer uid) {
        final SharedPreferences prefs = getUidPreferences();
        Log.i(TAG, "Saving UID " + uid);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(USER_ID, uid);
        editor.commit();
    }

    private SharedPreferences getUidPreferences() {
        return getSharedPreferences(UID, Context.MODE_PRIVATE);
    }
}
