package com.bridge4biz.laundry.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.db.DBHelper;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.AppInfo;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.request.GetRequest;
import com.bridge4biz.laundry.ui.dialog.PasswordDialog;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;
import com.bridge4biz.laundry.util.SessionManager;
import com.j256.ormlite.android.apptools.OpenHelperManager;

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
        customTitle.setText(getString(R.string.setting_title));
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
                openAlert();
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
                        CleanBasketApplication.getInstance().storeRegistrationId(getBaseContext(), null);
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

    private static String getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
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

    private void openAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(getString(R.string.logout));
        alertDialogBuilder.setMessage(getString(R.string.logout_confirm));
        alertDialogBuilder.setPositiveButton(R.string.label_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                logout();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
