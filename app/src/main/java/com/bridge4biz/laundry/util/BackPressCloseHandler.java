package com.bridge4biz.laundry.util;


import android.app.Activity;

import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;

public class BackPressCloseHandler {
    private long backKeyPressedTime = 0;

    private Activity activity;

    public BackPressCloseHandler(Activity context) {
        this.activity = context;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            activity.finish();
        }
    }

    public void showGuide() {
        CleanBasketApplication.getInstance().showToast(activity.getString(R.string.app_exit));
    }
}
