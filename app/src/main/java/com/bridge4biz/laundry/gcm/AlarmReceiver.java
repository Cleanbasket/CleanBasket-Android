package com.bridge4biz.laundry.gcm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            com.bridge4biz.laundry.util.AlarmManager.getInstance(mContext).setAlarm();
        }
    }
}
