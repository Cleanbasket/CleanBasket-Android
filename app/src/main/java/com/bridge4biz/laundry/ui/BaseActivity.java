package com.bridge4biz.laundry.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.bridge4biz.laundry.db.DBHelper;
import com.facebook.appevents.AppEventsLogger;
import com.google.analytics.tracking.android.EasyTracker;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class BaseActivity extends FragmentActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    private DBHelper dbHelper;

    public static Bundle intentToFragmentArguments(Intent intent) {
        Bundle arguments = new Bundle();

        if (intent == null) {
            return arguments;
        }

        final Bundle extras = intent.getExtras();
        if (extras != null) {
            arguments.putAll(intent.getExtras());
        }

        return arguments;
    }

    @Override
    protected void onResume() {
        AppEventsLogger.activateApp(this);

        super.onResume();
    }

    @Override
    protected void onPause() {
        AppEventsLogger.deactivateApp(this);

        super.onPause();
    }

    public DBHelper getDBHelper() {
        if(dbHelper == null) {
            dbHelper = OpenHelperManager.getHelper(this, DBHelper.class);
        }

        return dbHelper;
    }

    @Override
    protected void onStart() {
        super.onStart();

        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dbHelper != null) {
            OpenHelperManager.releaseHelper();
            dbHelper = null;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}