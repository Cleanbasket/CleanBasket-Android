package com.washappkorea.corp.cleanbasket;


import android.app.Application;
import android.widget.Toast;

import com.google.gson.Gson;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.washappkorea.corp.cleanbasket.db.DBHelper;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class CleanBasketApplication extends Application {
    private static final String TAG = CleanBasketApplication.class.getSimpleName();
    public static CleanBasketApplication mInstance;

    private DBHelper dbHelper;

    public Gson mGson;

    @Override
    public void onCreate() {
        mInstance = this;

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

    /* 이름으로 문자열을 가져옵니다 */
    public String getStringByString(String name) {
        int resourceId = this.getResources().getIdentifier(name, "string", this.getPackageName());

        String result = "";

        try {
            result = this.getString(resourceId);
        } catch (Exception e) {
            return this.getString(R.string.default_name);
        }

        return result;
    }

    /* 이름으로 아이콘을 가져옵니다 */
    public int getDrawableByString(String name) {
        return this.getResources().getIdentifier("ic_item_" + name, "drawable", this.getPackageName());
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public DBHelper getDBHelper() {
        if(dbHelper == null) {
            dbHelper = OpenHelperManager.getHelper(this, DBHelper.class);
        }

        return dbHelper;
    }
}
