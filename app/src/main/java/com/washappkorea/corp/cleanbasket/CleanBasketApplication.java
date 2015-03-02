package com.washappkorea.corp.cleanbasket;


import android.app.Application;

import com.google.gson.Gson;

public class CleanBasketApplication extends Application {
    public static CleanBasketApplication mInstance;

    public Gson mGson;

    @Override
    public void onCreate() {
        mInstance = this;

        super.onCreate();
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
}
