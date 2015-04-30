package com.washappkorea.corp.cleanbasket.ui;


import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonSyntaxException;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.model.Alarm;
import com.washappkorea.corp.cleanbasket.io.model.JsonData;
import com.washappkorea.corp.cleanbasket.io.model.Order;
import com.washappkorea.corp.cleanbasket.io.request.GetRequest;
import com.washappkorea.corp.cleanbasket.io.request.PostRequest;
import com.washappkorea.corp.cleanbasket.util.AddressManager;
import com.washappkorea.corp.cleanbasket.util.BackPressCloseHandler;
import com.washappkorea.corp.cleanbasket.util.Constants;
import com.washappkorea.corp.cleanbasket.util.DateTimeFactory;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends BaseActivity implements Response.Listener<JSONObject>, Response.ErrorListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String NEW_INFO_FRAGMENT = "NEW_INFO";
    public static final String NEW_ORDER_FRAGMENT = "NEW_ORDER";
    public static final String CHANGE_TO_INFO_FRAGMENT = "CHANGE_TO_INFO";
    public static final String CHANGE_TO_ORDER_FRAGMENT = "CHANGE_TO_ORDER";
    public static final String REMOVE_NEW_ORDER_FRAGMENT = "REMOVE_NEW_ORDER";

    public static final int PICK_UP_ALRAM_HOUR = -1;
    public static final int DROP_OFF_ALRAM_HOUR = -3;

    public static final int PICK_UP_ALARM = 0;
    public static final int DROP_OFF_ALARM = 1;

    private ViewPager mViewPager;
    private MainTabsAdapter mTabsAdapter;

    private Context mContext;

    // Related to GCM
    public static final String GCM = "gcm";
    public static final String UID = "uid";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String USER_ID = "user_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

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

        getUidFromServer();
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

    public ViewPager getViewPager() {
        return mViewPager;
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

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private SharedPreferences getGCMPreferences() {
        return getSharedPreferences(GCM, Context.MODE_PRIVATE);
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
        storeRegistrationId(this, regId);
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

    public void cancelAlarm(Order order) {
        Integer pickUpRequestCode = Integer.parseInt(String.valueOf("2" + String.valueOf(order.oid)));
        Integer dropOffRequestCode = Integer.parseInt(String.valueOf("3" + String.valueOf(order.oid)));

        Intent intent = new Intent("com.google.android.c2dm.intent.RECEIVE");
        PendingIntent pPickUpIntent = PendingIntent.getBroadcast(this, pickUpRequestCode, intent, 0);
        PendingIntent pDropOffIntent = PendingIntent.getBroadcast(this, dropOffRequestCode, intent, 0);

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pPickUpIntent);
        alarmManager.cancel(pDropOffIntent);
    }

    public void setAlarm() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ArrayList<Alarm> arrayList = (ArrayList<Alarm>) getDBHelper().getAlarmDao().queryForAll();

                for (Alarm alarm : arrayList) {
                    if (alarm.date > System.currentTimeMillis()) {
                        Date date = new Date(alarm.date);

                        Intent intent = new Intent("com.google.android.c2dm.intent.RECEIVE");

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);

                        intent.putExtra("oid", String.valueOf(alarm.oid));

                        int requestCode = 0;

                        if (calendar.get(Calendar.MINUTE) == 0) {
                            intent.putExtra("message", String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) +
                                    getString(R.string.hour_text));
                        }
                        else
                            intent.putExtra("message", String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) +
                                    getString(R.string.hour_text) +
                                    " " +
                                    String.valueOf(calendar.get(Calendar.MINUTE)) +
                                    getString(R.string.minute_text));

                        if (alarm.type == PICK_UP_ALARM) {
                            intent.putExtra("type", "2");
                            requestCode = Integer.parseInt("2" + String.valueOf(alarm.oid));
                            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + PICK_UP_ALARM);
                        }
                        else if (alarm.type == DROP_OFF_ALARM) {
                            intent.putExtra("type", "3");
                            requestCode = Integer.parseInt("3" + String.valueOf(alarm.oid));
                            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + DROP_OFF_ALRAM_HOUR);
                        }


                        PendingIntent pIntent = PendingIntent.getBroadcast(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            if (alarm.type == PICK_UP_ALARM)
                                alarmManager.setExact(AlarmManager.RTC, alarm.date, pIntent);
                            else if (alarm.type == DROP_OFF_ALARM)
                                alarmManager.setExact(AlarmManager.RTC, alarm.date, pIntent);
                        }
                        else {
                            if (alarm.type == PICK_UP_ALARM)
                                alarmManager.set(AlarmManager.RTC, alarm.date, pIntent);
                            else if (alarm.type == DROP_OFF_ALARM)
                                alarmManager.set(AlarmManager.RTC, alarm.date, pIntent);
                        }
                    }
                }

                return null;
            }
        }.execute(null, null, null);
    }

    public void insertAlarm(Order order) {
        Alarm pickUpAlarm = new Alarm();
        pickUpAlarm.oid = order.oid;
        pickUpAlarm.type = MainActivity.PICK_UP_ALARM;
        pickUpAlarm.date = DateTimeFactory.getInstance().getDate(order.pickup_date).getTime();

        Alarm dropOffAlarm = new Alarm();
        dropOffAlarm.oid = order.oid;
        dropOffAlarm.type = MainActivity.DROP_OFF_ALARM;
        dropOffAlarm.date = DateTimeFactory.getInstance().getDate(order.dropoff_date).getTime();

        getDBHelper().getAlarmDao().createOrUpdate(pickUpAlarm);
        getDBHelper().getAlarmDao().createOrUpdate(dropOffAlarm);
    }

    public void deleteAlarmFromDB(Order order) {
        Alarm pickUpAlarm = new Alarm();
        pickUpAlarm.oid = order.oid;
        pickUpAlarm.type = MainActivity.PICK_UP_ALARM;
        pickUpAlarm.date = DateTimeFactory.getInstance().getDate(order.pickup_date).getTime();

        Alarm dropOffAlarm = new Alarm();
        dropOffAlarm.oid = order.oid;
        dropOffAlarm.type = MainActivity.DROP_OFF_ALARM;
        dropOffAlarm.date = DateTimeFactory.getInstance().getDate(order.dropoff_date).getTime();

        ArrayList<Alarm> alarms = (ArrayList<Alarm>) getDBHelper().getAlarmDao().queryForEq(Alarm.OrderID, order.oid);
        for (Alarm alarm : alarms) {
            getDBHelper().getAlarmDao().delete(alarm);
        }
    }

    public void getUidFromServer() {
        GetRequest getRequest = new GetRequest(this);
        getRequest.setUrl(AddressManager.GET_MEMBER_INFO);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonData jsonData;

                try {
                    jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response, JsonData.class);
                } catch (JsonSyntaxException e) {
                    return;
                }

                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        storeUid(getBaseContext(), Integer.parseInt(jsonData.data));
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

    private void storeUid(Context context, Integer uid) {
        final SharedPreferences prefs = getUidPreferences();
        Log.i(TAG, "Saving UID " + uid);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(USER_ID, uid);
        editor.commit();
    }


    public int getUid(Context context) {
        final SharedPreferences prefs = getUidPreferences();
        int uid = prefs.getInt(USER_ID, 0);

        return uid;
    }

    private SharedPreferences getUidPreferences() {
        return getSharedPreferences(UID, Context.MODE_PRIVATE);
    }
}
