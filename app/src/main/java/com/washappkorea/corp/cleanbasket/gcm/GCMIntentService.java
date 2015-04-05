package com.washappkorea.corp.cleanbasket.gcm;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.squareup.picasso.Picasso;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.model.Notification;
import com.washappkorea.corp.cleanbasket.ui.DialogActivity;
import com.washappkorea.corp.cleanbasket.ui.NoticeActivity;
import com.washappkorea.corp.cleanbasket.ui.SettingActivity;
import com.washappkorea.corp.cleanbasket.ui.dialog.CouponDialog;
import com.washappkorea.corp.cleanbasket.util.DateTimeFactory;

import java.io.IOException;

public class GCMIntentService extends IntentService {
    private static final String TAG = GCMIntentService.class.getSimpleName();

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public GCMIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences pref = getSharedPreferences(SettingActivity.NOTIFICATION, MODE_PRIVATE);

        Bundle extras = intent.getExtras();

        Notification notification = new Notification();
        if (extras.containsKey("oid"))
            notification.oid = Integer.parseInt(extras.getString("oid"));
        if (extras.containsKey("type"))
            notification.type = Integer.parseInt(extras.getString("type"));
        notification.title = extras.getString("title");
        notification.message = extras.getString("message");
        notification.image = extras.getString("image");
        if (extras.containsKey("value"))
            notification.value = Integer.parseInt((String) extras.getString("value"));
        notification.date = DateTimeFactory.getInstance().getNow();
        notification.check = false;

        // 알람 목록에 보이기 위해 메시지를 저장합니다
        switch (notification.type) {
            case Notification.MESSAGE_ALARM:
                notification.title = getString(R.string.pd_message);
                break;

            case Notification.PICKUP_ALARM:
                notification.title = getString(R.string.today) +
                        " " +
                        notification.message +
                        getString(R.string.pick_up_notification);
                break;

            case Notification.DROPOFF_ALARM:
                notification.title = getString(R.string.today) +
                        " " +
                        notification.message +
                        getString(R.string.drop_off_notification);
                break;

            case Notification.COUPON_ALARM:
                notification.title = notification.message + getString(R.string.issue_coupon);
                break;

            case Notification.FEEDBACK_ALARM:
                notification.title = getString(R.string.request_feedback);
                break;
        }

        Log.i(TAG, notification.oid + " 저장 / " + notification.title);

        if (!insertDB(notification)) return;

        switch (notification.type) {
            case Notification.EVENT_ALARM:
                if(!pref.getBoolean("notification_switch_event", true))
                    return;

                if (!TextUtils.isEmpty(notification.image)) {
                    try {
                        Bitmap bitmap = Picasso.with(this).load(notification.image).get();
                        sendEventNotificationWithImage(notification, bitmap);
                    } catch (IOException e) {
                        sendEventNotification(notification);
                    } catch (IllegalStateException e) {
                        sendEventNotification(notification);
                    }
                }
                else
                    sendEventNotification(notification);
                break;

            case Notification.MESSAGE_ALARM:
                sendMessageNotification(getString(R.string.pd_message), notification.message);
                break;

            case Notification.PICKUP_ALARM:
                if(!pref.getBoolean("notification_switch_order", true))
                    return;

                sendNotificationForOrder(
                        notification,
                        getString(R.string.today) +
                                " " +
                                notification.message +
                                getString(R.string.pick_up_notification));
                break;

            case Notification.DROPOFF_ALARM:
                if(!pref.getBoolean("notification_switch_order", true))
                    return;

                sendNotificationForOrder(
                        notification,
                        getString(R.string.today) +
                                " " +
                                notification.message +
                                getString(R.string.drop_off_notification));
                break;

            case Notification.COUPON_ALARM:
                sendCouponNotification(notification.message + getString(R.string.issue_coupon));
                break;

            case Notification.FEEDBACK_ALARM:
                sendFeedbackNotification(notification);
                break;
        }

        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private boolean insertDB(Notification notification) {
        Dao.CreateOrUpdateStatus createOrUpdateStatus = CleanBasketApplication.getInstance().getDBHelper().getNotificationDao().createOrUpdate(notification);

        if (createOrUpdateStatus.isCreated() || createOrUpdateStatus.isUpdated())
            return true;

        return false;
    }

    /**
     * 이벤트 알림
     */
    protected void sendEventNotification(Notification notification) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent();
        intent.setAction("com.washappkorea.corp.cleanbasket.ui.NoticeActivity");
        intent.getIntExtra("value", notification.value);

        PendingIntent contentIntent = PendingIntent.getActivity(this, notification.type,
                intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(notification.title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(notification.message))
                        .setContentText(notification.message);

        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * 이벤트 그림 알림
     */
    protected void sendEventNotificationWithImage(Notification notification, Bitmap image) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, notification.type,
                new Intent(this, NoticeActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(notification.title)
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .setBigContentTitle(notification.title).setSummaryText(notification.message).bigPicture(image))
                        .setContentText(notification.message);

        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * 쪽지 전달 시 노티피케이션
     * @param title 쪽지 제목으로 앱 내에 String으로 저장되어 있음
     * @param msg 메시지
     */
    private void sendMessageNotification(String title, String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent();
        intent.setAction("com.washappkorea.corp.cleanbasket.ui.DialogActivity");
        intent.putExtra("tag", DialogActivity.MESSAGE_DIALOG);
        intent.putExtra("message", msg);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 1, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * 수거 배달 노티피케이션, 알람으로 작동
     */
    private void sendNotificationForOrder(Notification notification, String message) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent();
        intent.setAction("com.washappkorea.corp.cleanbasket.ui.DialogActivity");
        intent.putExtra("tag", DialogActivity.MODIFY_TIME_DIALOG);
        intent.putExtra("oid", notification.oid);

        Log.i(TAG, "intent : " + notification.oid);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 2,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(message)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setContentText(message)
                .addAction(R.drawable.ic_sale, getString(R.string.order_okay), null)
                .addAction(R.drawable.ic_order_pickuptime, getString(R.string.order_change), contentIntent);

        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * 쿠폰 발행 시 전송되는 노티피케이션
     * @param msg 액수
     */
    private void sendCouponNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 4,
                new Intent(this, CouponDialog.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(msg)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * 쿠폰 발행 시 전송되는 노티피케이션
     * @param notification 액수
     */
    private void sendFeedbackNotification(Notification notification) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent();
        intent.setAction("com.washappkorea.corp.cleanbasket.ui.FeedbackActivity");
        intent.putExtra("oid", notification.oid);

        PendingIntent contentIntent = PendingIntent.getActivity(this, notification.type,
                intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.thanks_message))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(getString(R.string.request_feedback)))
                        .setContentText(getString(R.string.request_feedback));

        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}