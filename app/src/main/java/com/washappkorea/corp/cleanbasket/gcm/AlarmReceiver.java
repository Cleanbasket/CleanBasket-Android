package com.washappkorea.corp.cleanbasket.gcm;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;

import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.model.Alarm;
import com.washappkorea.corp.cleanbasket.ui.MainActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AlarmReceiver extends BroadcastReceiver {
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            setAlarm();
        }
    }

    public void setAlarm() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ArrayList<Alarm> arrayList = (ArrayList<Alarm>) CleanBasketApplication.getInstance().getDBHelper().getAlarmDao().queryForAll();

                for (Alarm alarm : arrayList) {

                    if (alarm.date > System.currentTimeMillis()) {
                        Date date = new Date(alarm.date);

                        Intent intent = new Intent("com.google.android.c2dm.intent.RECEIVE");

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);

                        if (calendar.get(Calendar.MINUTE) == 0)
                            intent.putExtra("message", String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) +
                                    mContext.getString(R.string.hour_text));
                        else
                            intent.putExtra("message", String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) +
                                    mContext.getString(R.string.hour_text) +
                                    " " +
                                    String.valueOf(calendar.get(Calendar.MINUTE)) +
                                    mContext.getString(R.string.minute_text));

                        int requestCode = 0;

                        if (alarm.type == MainActivity.PICK_UP_ALARM) {
                            intent.putExtra("type", "2");
                            requestCode = Integer.parseInt("2" + String.valueOf(alarm.oid));
                            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + MainActivity.PICK_UP_ALARM);
                        }
                        else if (alarm.type == MainActivity.DROP_OFF_ALARM) {
                            intent.putExtra("type", "3");
                            requestCode = Integer.parseInt("3" + String.valueOf(alarm.oid));
                            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + MainActivity.DROP_OFF_ALRAM_HOUR);
                        }

                        PendingIntent pIntent = PendingIntent.getBroadcast(mContext, requestCode, intent, 0);

                        // todo
                        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            if (alarm.type == MainActivity.PICK_UP_ALARM)
                                alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis() + 5000, pIntent);
                            else if (alarm.type == MainActivity.DROP_OFF_ALARM)
                                alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis() + 8000, pIntent);
                        }
                        else {
                            if (alarm.type == MainActivity.PICK_UP_ALARM)
                                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 5000, pIntent);
                            else if (alarm.type == MainActivity.DROP_OFF_ALARM)
                                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 8000, pIntent);
                        }
                    }
                }

                return null;
            }
        }.execute(null, null, null);
    }
}
