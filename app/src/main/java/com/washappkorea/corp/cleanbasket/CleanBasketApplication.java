package com.washappkorea.corp.cleanbasket;


import android.app.Application;

import com.google.gson.Gson;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class CleanBasketApplication extends Application {
    public static CleanBasketApplication mInstance = null;

    public Gson mGson;

    @Override
    public void onCreate() {
        super.onCreate();

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        mInstance = this;
    }

    public Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }

        return mGson;
    }
}
