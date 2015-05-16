package com.bridge4biz.laundry.util;


import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.bridge4biz.laundry.R;

public class BackPressCloseHandler {
    private long backKeyPressedTime = 0;

    private Activity activity;
    private Toast toast;

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
            toast.cancel();
            activity.finish();
        }
    }

    public void showGuide() {
        toast = Toast.makeText(activity, activity.getString(R.string.app_exit), Toast.LENGTH_LONG);
        View view = toast.getView();
        view.setBackgroundResource(R.color.badge_color);
        toast.show();
    }
}
