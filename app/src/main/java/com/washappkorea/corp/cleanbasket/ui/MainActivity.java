package com.washappkorea.corp.cleanbasket.ui;


import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.model.JsonData;
import com.washappkorea.corp.cleanbasket.io.request.StringRequest;
import com.washappkorea.corp.cleanbasket.util.AddressManager;
import com.washappkorea.corp.cleanbasket.util.Constants;

import java.io.IOException;

public class MainActivity extends BaseActivity implements Response.Listener<String>, Response.ErrorListener{
    private static final String TAG = MainActivity.class.getSimpleName();

    private ViewPager mViewPager;
    private MainTabsAdapter mTabsAdapter;

    // Related to GCM
    public static final String GCM = "gcm";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private String SENDER_ID = "1008388733235";
    private GoogleCloudMessaging gcm;
    private String regId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.pager);

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setDisplayShowHomeEnabled(false);
        bar.setDisplayShowTitleEnabled(false);

        mTabsAdapter = new MainTabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.ic_order), OrderFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.ic_orderlist), OrderStateFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.ic_info), UserFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.ic_alarm), AlarmFragment.class, null);

        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }

        gcm = GoogleCloudMessaging.getInstance(this);
        regId = getRegistrationId(this);

        if (regId.isEmpty()) {
            registerInBackground();
        }
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");

        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");

            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);

        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");

            return "";
        }

        return registrationId;
    }

    private SharedPreferences getGCMPreferences() {
        return getSharedPreferences(GCM, Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void ... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getBaseContext());
                    }

                    regId = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID = " + regId;

                    sendRegistrationIdToBackend(regId);
                } catch (IOException ex) {
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                    msg = "Error : " + ex.getMessage();
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i("TAG", msg);
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend(final String regId) {
        StringRequest stringRequest = new StringRequest(this);
        stringRequest.setParams("regid", regId);
        stringRequest.setUrl(AddressManager.REGID);
        stringRequest.setListener(this, this);
        RequestQueue.getInstance(this).addToRequestQueue(stringRequest.doRequest());
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    @Override
    public void onResponse(String response) {
        JsonData jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response, JsonData.class);
        switch (jsonData.constant) {
            case Constants.ERROR:
                break;
            case Constants.SUCCESS:
                storeRegistrationId(getBaseContext(), regId);
                Log.i(TAG, "Reg Id 등록");
                break;
        }

        storeRegistrationId(getBaseContext(), regId);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.i(TAG, "Reg Id 등록 실패 : " + error.toString());
    }
}
