package com.bridge4biz.laundry.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;

import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.db.DBHelper;
import com.bridge4biz.laundry.io.model.Alarm;
import com.bridge4biz.laundry.io.model.Order;
import com.bridge4biz.laundry.ui.MainActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AlarmManager {
    private Context mContext;
    private DBHelper mDBHelper;

    private static AlarmManager mAlarmManager;

    public static final int PICK_UP_ALRAM_HOUR = -1;
    public static final int DROP_OFF_ALRAM_HOUR = -3;

    private AlarmManager(Context context) {
        mContext = context;
        mDBHelper = CleanBasketApplication.mInstance.getDBHelper();
    }

    public static synchronized AlarmManager getInstance(Context context) {
        if (mAlarmManager == null) {
            mAlarmManager = new AlarmManager(context);
        }

        return mAlarmManager;
    }

    public void cancelAlarm(Order order) {
        Integer pickUpRequestCode = Integer.parseInt(String.valueOf("2" + String.valueOf(order.oid)));
        Integer dropOffRequestCode = Integer.parseInt(String.valueOf("3" + String.valueOf(order.oid)));

        Intent intent = new Intent("com.google.android.c2dm.intent.RECEIVE");
        PendingIntent pPickUpIntent = PendingIntent.getBroadcast(mContext, pickUpRequestCode, intent, 0);
        PendingIntent pDropOffIntent = PendingIntent.getBroadcast(mContext, dropOffRequestCode, intent, 0);

        android.app.AlarmManager alarmManager = (android.app.AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pPickUpIntent);
        alarmManager.cancel(pDropOffIntent);
    }

    public void setAlarm() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ArrayList<Alarm> arrayList = (ArrayList<Alarm>) mDBHelper.getAlarmDao().queryForAll();

                for (Alarm alarm : arrayList) {
                    if (alarm.date > System.currentTimeMillis()) {
                        Date date = new Date(alarm.date);

                        Intent intent = new Intent("com.google.android.c2dm.intent.RECEIVE");

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);

                        intent.putExtra("oid", String.valueOf(alarm.oid));

                        int requestCode = 0;

                        if (calendar.get(Calendar.MINUTE) == 0) {
                            intent.putExtra("message", String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) +
                                    mContext.getString(R.string.hour_text));
                        }
                        else
                            intent.putExtra("message", String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) +
                                    mContext.getString(R.string.hour_text) +
                                    " " +
                                    String.valueOf(calendar.get(Calendar.MINUTE)) +
                                    mContext.getString(R.string.minute_text));

                        if (alarm.type == MainActivity.PICK_UP_ALARM) {
                            intent.putExtra("uid", String.valueOf(CleanBasketApplication.getInstance().getUid()));
                            intent.putExtra("type", "2");
                            requestCode = Integer.parseInt("2" + String.valueOf(alarm.oid));
                            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + PICK_UP_ALRAM_HOUR);
                        }
                        else if (alarm.type == MainActivity.DROP_OFF_ALARM) {
                            intent.putExtra("uid", String.valueOf(CleanBasketApplication.getInstance().getUid()));
                            intent.putExtra("type", "3");
                            requestCode = Integer.parseInt("3" + String.valueOf(alarm.oid));
                            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + DROP_OFF_ALRAM_HOUR);
                        }

                        PendingIntent pIntent = PendingIntent.getBroadcast(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        android.app.AlarmManager alarmManager = (android.app.AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            if (alarm.type == MainActivity.PICK_UP_ALARM)
                                alarmManager.setExact(android.app.AlarmManager.RTC, calendar.getTime().getTime(), pIntent);
                            else if (alarm.type == MainActivity.DROP_OFF_ALARM)
                                alarmManager.setExact(android.app.AlarmManager.RTC, calendar.getTime().getTime(), pIntent);
                        }
                        else {
                            if (alarm.type == MainActivity.PICK_UP_ALARM)
                                alarmManager.set(android.app.AlarmManager.RTC, calendar.getTime().getTime(), pIntent);
                            else if (alarm.type == MainActivity.DROP_OFF_ALARM)
                                alarmManager.set(android.app.AlarmManager.RTC, calendar.getTime().getTime(), pIntent);
                        }
                    }
                }

                return null;
            }
        }.execute(null, null, null);
    }

    public void insertAlarm(Order order) {
        Alarm pickUpAlarm = new Alarm();
        pickUpAlarm.oid = order.oid;
        pickUpAlarm.type = MainActivity.PICK_UP_ALARM;
        pickUpAlarm.date = DateTimeFactory.getInstance().getDate(order.pickup_date).getTime();

        Alarm dropOffAlarm = new Alarm();
        dropOffAlarm.oid = order.oid;
        dropOffAlarm.type = MainActivity.DROP_OFF_ALARM;
        dropOffAlarm.date = DateTimeFactory.getInstance().getDate(order.dropoff_date).getTime();

        mDBHelper.getAlarmDao().createOrUpdate(pickUpAlarm);
        mDBHelper.getAlarmDao().createOrUpdate(dropOffAlarm);
    }

    public void deleteAlarmFromDB(Order order) {
        Alarm pickUpAlarm = new Alarm();
        pickUpAlarm.oid = order.oid;
        pickUpAlarm.type = MainActivity.PICK_UP_ALARM;
        pickUpAlarm.date = DateTimeFactory.getInstance().getDate(order.pickup_date).getTime();

        Alarm dropOffAlarm = new Alarm();
        dropOffAlarm.oid = order.oid;
        dropOffAlarm.type = MainActivity.DROP_OFF_ALARM;
        dropOffAlarm.date = DateTimeFactory.getInstance().getDate(order.dropoff_date).getTime();

        ArrayList<Alarm> alarms = (ArrayList<Alarm>) mDBHelper.getAlarmDao().queryForEq(Alarm.OrderID, order.oid);
        for (Alarm alarm : alarms) {
            mDBHelper.getAlarmDao().delete(alarm);
        }
    }
}
