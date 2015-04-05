package com.washappkorea.corp.cleanbasket.ui;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.Config;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.db.DBHelper;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.model.AppInfo;
import com.washappkorea.corp.cleanbasket.io.model.JsonData;
import com.washappkorea.corp.cleanbasket.io.request.GetRequest;
import com.washappkorea.corp.cleanbasket.ui.dialog.PasswordDialog;
import com.washappkorea.corp.cleanbasket.util.AddressManager;
import com.washappkorea.corp.cleanbasket.util.Constants;
import com.washappkorea.corp.cleanbasket.util.SessionManager;

import java.sql.SQLException;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SettingActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final String TAG = SettingActivity.class.getSimpleName();
    public static final int LOG_OUT = 1;

    public static final String NOTIFICATION = "notification";

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getListView().setBackgroundColor(Color.WHITE);

        getActionBar().setTitle(R.string.setting_title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory header = new PreferenceCategory(this);
        header.setTitle(R.string.notification);
        getPreferenceScreen().addPreference(header);
        addPreferencesFromResource(R.xml.pref_notification);

        // Add 'data and sync' preferences, and a corresponding header.
        header = new PreferenceCategory(this);
        header.setTitle(R.string.pref_header_version);
        getPreferenceScreen().addPreference(header);
        addPreferencesFromResource(R.xml.pref_version);

        findPreference("current_version").setSummary(getString(R.string.current_version) + " " + getAppVersion(this));

        try {
            AppInfo appInfo = getDBHelper().getAppInfoDao().queryBuilder().queryForFirst();
            findPreference("latest_version").setSummary(getString(R.string.latest_version) + " " + appInfo.android_app_ver);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        bindPreferenceSummaryToValue(findPreference("change_password"));
        bindPreferenceSummaryToValue(findPreference("logout"));
        bindPreferenceSummaryToValue(findPreference("current_version"));
        bindPreferenceSummaryToValue(findPreference("latest_version"));
        bindPreferenceSummaryToValue(findPreference("update"));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference instanceof Preference) {
            if (preference.getKey().equals("change_password")) {
                PasswordDialog pd = PasswordDialog.newInstance();

                pd.show(getFragmentManager(), "pd");
            }
            else if (preference.getKey().equals("logout")) {
                logout();
            }
            else if (preference.getKey().equals("update")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.PLAY_STORE_URL));
                startActivity(intent);
            }
        }

        return true;
    }

    private void logout() {
        GetRequest getRequest = new GetRequest(this);
        getRequest.setUrl(AddressManager.LOGOUT);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonData jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response, JsonData.class);
                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        SessionManager.get(getBaseContext()).clearSessionId();
                        redirectToLoginActivity();
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });
        RequestQueue.getInstance(this).addToRequestQueue(getRequest.doRequest());
    }

    private void redirectToLoginActivity() {
        setResult(LOG_OUT);
        finish();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference instanceof SwitchPreference) {
            SwitchPreference switchPreference = (SwitchPreference) preference;

            boolean isSwitch = true;

            if(switchPreference.getKey().equals("notification_switch_event"))
                isSwitch = switchPreference.isChecked();
            else if(switchPreference.getKey().equals("notification_switch_order"))
                isSwitch = switchPreference.isChecked();

            storeNotification(switchPreference.getKey(), isSwitch);
        }

        return true;
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);
        preference.setOnPreferenceClickListener(this);

        this.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private void storeNotification(String key, Boolean value) {
        final SharedPreferences prefs = getNotificationPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private SharedPreferences getNotificationPreferences() {
        return getSharedPreferences(NOTIFICATION, Context.MODE_PRIVATE);
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

    public DBHelper getDBHelper() {
        if(dbHelper == null) {
            dbHelper = OpenHelperManager.getHelper(this, DBHelper.class);
        }

        return dbHelper;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
