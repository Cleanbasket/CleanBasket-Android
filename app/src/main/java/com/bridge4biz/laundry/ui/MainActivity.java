package com.bridge4biz.laundry.ui;


import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.AuthUser;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.request.GetRequest;
import com.bridge4biz.laundry.io.request.PostRequest;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.BackPressCloseHandler;
import com.bridge4biz.laundry.util.Constants;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonSyntaxException;
import com.zopim.android.sdk.api.ZopimChat;
import com.zopim.android.sdk.model.VisitorInfo;

import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends BaseActivity implements Response.Listener<JSONObject>, Response.ErrorListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String NEW_INFO_FRAGMENT = "NEW_INFO";
    public static final String NEW_ORDER_FRAGMENT = "NEW_ORDER";
    public static final String CHANGE_TO_INFO_FRAGMENT = "CHANGE_TO_INFO";
    public static final String CHANGE_TO_ORDER_FRAGMENT = "CHANGE_TO_ORDER";
    public static final String REMOVE_NEW_ORDER_FRAGMENT = "REMOVE_NEW_ORDER";

    public static final int PICK_UP_ALARM = 0;
    public static final int DROP_OFF_ALARM = 1;

    private ViewPager mViewPager;
    private MainTabsAdapter mTabsAdapter;

    private Context mContext;
    public AuthUser mAuthUser;

    // Related to GCM
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";

    private BackPressCloseHandler backPressCloseHandler;

    private String SENDER_ID = "1008388733235";
    private GoogleCloudMessaging gcm;
    private String regId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(4);

        final ActionBar bar = getActionBar();

        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setDisplayShowHomeEnabled(false);
        bar.setDisplayShowTitleEnabled(false);

        mTabsAdapter = new MainTabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.menu_01), OrderFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.menu_02), OrderStatusFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.menu_03), UserFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.menu_04), NotificationFragment.class, null);

        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }

        gcm = GoogleCloudMessaging.getInstance(this);
        regId = getRegistrationId(this);

        if (regId.isEmpty())
            registerInBackground();

        backPressCloseHandler = new BackPressCloseHandler(this);

        mContext = this;

        getUserInfo();

        showMarketingPopUp();
    }

    private void showMarketingPopUp() {
        Intent intent = new Intent(this, MarketingPopupActivity.class);
        startActivity(intent);
    }

    private VisitorInfo initZopimChatFragment() {
        VisitorInfo visitorInfo = new VisitorInfo.Builder()
                .email(mAuthUser.email)
                .phoneNumber(mAuthUser.phone)
                .build();

        return visitorInfo;
    }

    private VisitorInfo initZopimChatFragmentWithoutAuthUser() {
        VisitorInfo visitorInfo = new VisitorInfo.Builder()
                .phoneNumber(getPhoneNumber())
                .build();

        return visitorInfo;
    }

    /* 스마트폰 번호를 가져옵니다 */
    public String getPhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(this.TELEPHONY_SERVICE);
        String mPhoneNumber;
        mPhoneNumber = tMgr.getLine1Number();

        if (mPhoneNumber != null) {
            mPhoneNumber = mPhoneNumber.replace("+82", "0");

            if(mPhoneNumber.length() != 11) {
                return "";
            }
        }

        return mPhoneNumber;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = CleanBasketApplication.getInstance().getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");

        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");

            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = CleanBasketApplication.getAppVersion(this);

        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");

            return "";
        }

        return registrationId;
    }

    public ViewPager getViewPager() {
        return mViewPager;
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

                Log.i(TAG, msg);

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i("TAG", msg);
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend(final String regId) {
        PostRequest postRequest = new PostRequest(this);
        postRequest.setParams("regid", regId);
        postRequest.setUrl(AddressManager.REGID);
        postRequest.setListener(this, this);
        RequestQueue.getInstance(this).addToRequestQueue(postRequest.doRequest());
    }

    @Override
    public void onResponse(JSONObject response) {
        JsonData jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);
        switch (jsonData.constant) {
            case Constants.ERROR:
                Log.i(TAG, "Reg Id Error");
                break;

            case Constants.SUCCESS:
                triggerStoreRegId();
                Log.i(TAG, "Reg Id 등록");
                break;
        }
    }

    private void triggerStoreRegId() {
        CleanBasketApplication.getInstance().storeRegistrationId(this, regId);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.i(TAG, "Reg Id 등록 실패 : " + error.toString());
    }

    @Override
    public void onBackPressed() {
        String transactionName = null;

        try {
            android.support.v4.app.FragmentManager.BackStackEntry bse = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1);
            transactionName = bse.getName();
        } catch (Exception e) {
            backPressCloseHandler.onBackPressed();

            return;
        }

        if (transactionName.equals(NEW_INFO_FRAGMENT) || transactionName.equals(CHANGE_TO_INFO_FRAGMENT)) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.hide(getSupportFragmentManager().findFragmentByTag(OrderInfoFragment.TAG));
            ft.show(getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.pager+":0"));
            ft.addToBackStack(CHANGE_TO_ORDER_FRAGMENT);
            ft.commit();
        }
        else if (transactionName.equals(NEW_ORDER_FRAGMENT)) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(getSupportFragmentManager().findFragmentByTag(OrderFragment.TAG));
            ft.addToBackStack(REMOVE_NEW_ORDER_FRAGMENT);
            ft.commit();
        }
        else {
            backPressCloseHandler.onBackPressed();
        }

        Log.i(TAG, "Back Pressed");
    }

    public void getUserInfo() {
        GetRequest getRequest = new GetRequest(this);
        getRequest.setUrl(AddressManager.GET_AUTH_MEMBER_INFO);
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
                        if (jsonData.data.equals("null")) {
                            mAuthUser = null;
                            ZopimChat.setVisitorInfo(initZopimChatFragmentWithoutAuthUser());
                        }
                        else {
                            mAuthUser = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, AuthUser.class);
                            ZopimChat.setVisitorInfo(initZopimChatFragment());
                        }

                        ZopimChat.init(Config.ZOPIM_API).build();
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestQueue.getInstance(this).addToRequestQueue(getRequest.doRequest());
    }
}
