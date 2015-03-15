package com.washappkorea.corp.cleanbasket.ui;


import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonSyntaxException;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.model.JsonData;
import com.washappkorea.corp.cleanbasket.io.request.GetRequest;
import com.washappkorea.corp.cleanbasket.util.AddressManager;
import com.washappkorea.corp.cleanbasket.util.Constants;

public class SplashActivity extends BaseActivity implements Response.Listener<String>, Response.ErrorListener {
    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setDisplayShowHomeEnabled(false);
        bar.setDisplayShowTitleEnabled(false);
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
        intent.setAction("com.washappkorea.corp.cleanbasket.ui.LoginActivity");
        startActivity(intent);

        super.finish();
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent();
        intent.setAction("com.washappkorea.corp.cleanbasket.ui.MainActivity");
        startActivity(intent);

        super.finish();
    }
}
