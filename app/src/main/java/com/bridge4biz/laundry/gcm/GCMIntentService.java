package com.bridge4biz.laundry.gcm;


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

import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.model.Notification;
import com.bridge4biz.laundry.ui.DialogActivity;
import com.bridge4biz.laundry.ui.NoticeActivity;
import com.bridge4biz.laundry.ui.SettingActivity;
import com.bridge4biz.laundry.ui.dialog.CouponDialog;
import com.bridge4biz.laundry.util.DateTimeFactory;
import com.j256.ormlite.dao.Dao;
import com.squareup.picasso.Picasso;

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
        if (extras.containsKey("uid"))
            notification.uid = Integer.parseInt(extras.getString("uid"));
        if (extras.containsKey("type"))
            notification.type = Integer.parseInt(extras.getString("type"));
        if (extras.containsKey("title"))
            notification.title = extras.getString("title");
        if (extras.containsKey("message"))
            notification.message = extras.getString("message");
        if (extras.containsKey("image"))
            notification.image = extras.getString("image");
        if (extras.containsKey("value"))
            notification.value = Integer.parseInt(extras.getString("value"));
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
                notification.title = notification.value + " " + getString(R.string.issue_coupon);
                break;

            case Notification.FEEDBACK_ALARM:
                notification.title = getString(R.string.request_feedback);
                break;

            case Notification.MODIFY_ALARM:
                notification.title = getString(R.string.modify_message);
                break;

            case Notification.MILEAGE_ALARM:
                notification.title = notification.value + " " + getString(R.string.mileage_message);
                break;
        }

        Log.i(TAG, notification.oid + " 저장 / " + notification.title + " / " + notification.uid);

        if (!insertDB(notification)) return;

        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

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
                sendCouponNotification(notification.value + getString(R.string.issue_coupon));
                break;

            case Notification.FEEDBACK_ALARM:
                sendFeedbackNotification(notification);
                break;

            case Notification.MODIFY_ALARM:
                sendModifyNotification(notification);
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
        Intent intent = new Intent();
        intent.setAction("com.bridge4biz.laundry.ui.NoticeActivity");
        intent.getIntExtra("value", notification.value);

        PendingIntent contentIntent = PendingIntent.getActivity(this, Notification.EVENT_ALARM,
                intent, PendingIntent.FLAG_ONE_SHOT);

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
        PendingIntent contentIntent = PendingIntent.getActivity(this, Notification.EVENT_ALARM,
                new Intent(this, NoticeActivity.class), PendingIntent.FLAG_ONE_SHOT);

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
        Intent intent = new Intent();
        intent.setAction("com.bridge4biz.laundry.ui.DialogActivity");
        intent.putExtra("tag", DialogActivity.MESSAGE_DIALOG);
        intent.putExtra("message", msg);

        PendingIntent contentIntent = PendingIntent.getActivity(this, Notification.MESSAGE_ALARM, intent, PendingIntent.FLAG_ONE_SHOT);

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
        Intent intent = new Intent();
        intent.setAction("com.bridge4biz.laundry.ui.DialogActivity");
        intent.putExtra("tag", DialogActivity.MODIFY_TIME_DIALOG);
        intent.putExtra("oid", notification.oid);

        Intent confirmIntent = new Intent();
        confirmIntent.setAction("com.bridge4biz.laundry.ui.DialogActivity");
        confirmIntent.putExtra("tag", DialogActivity.CONFIRM_TIME_DIALOG);
        confirmIntent.putExtra("oid", notification.oid);

        Log.i(TAG, "intent : " + notification.oid);

        PendingIntent contentPIntent = PendingIntent.getActivity(this, notification.oid + Notification.PICKUP_ALARM,
                intent, PendingIntent.FLAG_ONE_SHOT);

        PendingIntent confirmPIntent = PendingIntent.getActivity(this, notification.oid + Notification.DROPOFF_ALARM,
                confirmIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(message)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setContentText(message)
                .addAction(R.drawable.ic_alarm_delivery, getString(R.string.order_okay), confirmPIntent)
                .addAction(R.drawable.ic_order_pickuptime, getString(R.string.order_change), contentPIntent);

        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentPIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * 쿠폰 발행 시 전송되는 노티피케이션
     * @param msg 액수
     */
    private void sendCouponNotification(String msg) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, Notification.COUPON_ALARM,
                new Intent(this, CouponDialog.class), PendingIntent.FLAG_ONE_SHOT);

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

    private void sendFeedbackNotification(Notification notification) {
        Intent intent = new Intent();
        intent.setAction("com.bridge4biz.laundry.ui.FeedbackActivity");
        intent.putExtra("oid", notification.oid);

        Log.i(TAG, notification.oid + "");

        PendingIntent contentIntent = PendingIntent.getActivity(this, Notification.FEEDBACK_ALARM,
                intent, PendingIntent.FLAG_ONE_SHOT);

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

    private void sendModifyNotification(Notification notification) {
        Intent intent = new Intent();
        intent.setAction("com.bridge4biz.laundry.ui.DialogActivity");
        intent.putExtra("tag", DialogActivity.MODIFY_TIME_DIALOG);
        intent.putExtra("oid", notification.oid);

        PendingIntent contentPIntent = PendingIntent.getActivity(this, notification.oid + Notification.MODIFY_ALARM,
                intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(notification.title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(notification.title))
                        .setContentText(notification.title);

        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentPIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}