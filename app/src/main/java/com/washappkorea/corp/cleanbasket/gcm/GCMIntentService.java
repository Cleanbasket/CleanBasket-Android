package com.washappkorea.corp.cleanbasket.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.ui.SettingActivity;
import com.washappkorea.corp.cleanbasket.ui.SplashActivity;


public class GCMIntentService extends IntentService {
    private static final String TAG = GCMIntentService.class.getSimpleName();

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private String message;

    public GCMIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences pref = getSharedPreferences(SettingActivity.NOTIFICATION, MODE_PRIVATE);

        if(!pref.getBoolean("notification_switch_event", true))
            return;
        if(!pref.getBoolean("notification_switch_order", true))
            return;

        Bundle extras = intent.getExtras();

        Log.i(TAG, extras.toString());

        if (extras.containsKey("message")) {
            String mp_message = intent.getExtras().getString("message");

            sendNotification(mp_message);

            GCMBroadcastReceiver.completeWakefulIntent(intent);
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SplashActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}