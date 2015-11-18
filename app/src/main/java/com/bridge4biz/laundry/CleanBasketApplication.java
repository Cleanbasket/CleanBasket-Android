package com.bridge4biz.laundry;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bridge4biz.laundry.db.DBHelper;
import com.google.gson.Gson;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.urqa.clientinterface.URQAController;

import java.text.DecimalFormat;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class CleanBasketApplication extends Application {
    private static final String TAG = CleanBasketApplication.class.getSimpleName();

    public static CleanBasketApplication mInstance;

    // Related to GCM
    public static final String GCM = "gcm";
    public static final String PAYMENT_CARD_NAME = "cardName";
    public static final String PAYMENT_AUTH_DATE = "authDate";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String UID = "uid";
    public static final String USER_ID = "user_id";

    private DBHelper dbHelper;

    public Gson mGson;
    public static DecimalFormat mFormatKRW = new DecimalFormat("###,###,###");

    @Override
    public void onCreate() {
        mInstance = this;

        URQAController.InitializeAndStartSession(getApplicationContext(), Config.URQA_API_Key);

        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/NanumBarunGothic.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    public static synchronized CleanBasketApplication getInstance() {
        return mInstance;
    }

    public Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }

        return mGson;
    }

    public SharedPreferences getGCMPreferences() {
        return getSharedPreferences(GCM, Context.MODE_PRIVATE);
    }

    public void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /* 이름으로 문자열을 가져옵니다 */
    public String getStringByString(String name) {
        int resourceId = this.getResources().getIdentifier(name, "string", this.getPackageName());

        String result = "";

        try {
            result = this.getString(resourceId);
        } catch (Exception e) {
            return null;
        }

        return result;
    }

    /* 이름으로 아이콘을 가져옵니다 */
    public int getDrawableByString(String name) {
        return this.getResources().getIdentifier("ic_item_" + name, "drawable", this.getPackageName());
    }

    /* 이름으로 아이콘을 가져옵니다 */
    public int getDrawableByCategoryString(String name) {
        int resource;

        resource = this.getResources().getIdentifier("ic_category_" + name, "drawable", this.getPackageName());

        if (resource == 0)
            resource =  this.getResources().getIdentifier("ic_category_etc", "drawable", this.getPackageName());

        return resource;
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        View view = toast.getView();
        view.setBackgroundResource(R.color.badge_color);
        toast.show();
    }

    public DBHelper getDBHelper() {
        if(dbHelper == null) {
            dbHelper = OpenHelperManager.getHelper(this, DBHelper.class);
        }

        return dbHelper;
    }

    public int getPx(int dimensionDp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dimensionDp * density + 0.5f);
    }

    public int getUid() {
        final SharedPreferences prefs = getUidPreferences();
        int uid = prefs.getInt(USER_ID, 0);

        return uid;
    }

    private SharedPreferences getUidPreferences() {
        return getSharedPreferences(UID, Context.MODE_PRIVATE);
    }
}
