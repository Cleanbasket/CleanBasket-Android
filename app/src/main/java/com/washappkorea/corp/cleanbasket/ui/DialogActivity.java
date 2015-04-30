package com.washappkorea.corp.cleanbasket.ui;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.db.DBHelper;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.model.Alarm;
import com.washappkorea.corp.cleanbasket.io.model.JsonData;
import com.washappkorea.corp.cleanbasket.io.model.Order;
import com.washappkorea.corp.cleanbasket.io.request.GetRequest;
import com.washappkorea.corp.cleanbasket.ui.dialog.MessageDialog;
import com.washappkorea.corp.cleanbasket.ui.dialog.ModifyDateTimeDialog;
import com.washappkorea.corp.cleanbasket.util.AddressManager;
import com.washappkorea.corp.cleanbasket.util.Constants;
import com.washappkorea.corp.cleanbasket.util.DateTimeFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DialogActivity extends FragmentActivity implements MessageDialog.OnDialogDismissListener, ModifyDateTimeDialog.OnDialogDismissListener {
    private static final String TAG = DialogActivity.class.getSimpleName();
    public static final String MESSAGE_DIALOG = "MESSAGE_DIALOG";
    public static final String COUPON_DIALOG = "COUPON_DIALOG";
    public static final String MODIFY_TIME_DIALOG = "MODIFY_TIME_DIALOG";
    public static final String CONFIRM_TIME_DIALOG = "CONFIRM_TIME_DIALOG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("tag")) {
            String tag = getIntent().getExtras().getString("tag");
            if (tag.equals(MESSAGE_DIALOG)) {
                String message = getIntent().getExtras().getString("message");
                MessageDialog md = MessageDialog.newInstance(this, message);

                md.show(getSupportFragmentManager(), tag);
            }
            else if (tag.equals(COUPON_DIALOG)) {

            }
            else if (tag.equals(CONFIRM_TIME_DIALOG)) {
                if(getIntent().getExtras().containsKey("oid")) {
                    int oid = getIntent().getExtras().getInt("oid");

                    Log.i(TAG, oid + "");

                    sendConfirm(oid);
                }
            }
            else if (tag.equals(MODIFY_TIME_DIALOG)) {
                if(getIntent().getExtras().containsKey("oid")) {
                    int oid = getIntent().getExtras().getInt("oid");

                    Log.i(TAG, oid + "");

                    ModifyDateTimeDialog mdtd = ModifyDateTimeDialog.newInstance(this, oid);

                    mdtd.show(getSupportFragmentManager(), tag);
                }
            }
        }
    }

    private void sendConfirm(int oid) {
        GetRequest getRequest = new GetRequest(this);

        getRequest.setUrl(AddressManager.CONFIRM_ORDER);
        getRequest.setParams("oid", oid);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonData jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);

                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        CleanBasketApplication.getInstance().showToast(getString(R.string.time_confirmed));
                        finish();
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                CleanBasketApplication.getInstance().showToast(getString(R.string.general_error));
                finish();
            }
        });
        RequestQueue.getInstance(this).addToRequestQueue(getRequest.doRequest());
    }

    @Override
    public void onDialogDismiss() {
        finish();
    }


    public void cancelAlarm(Order order) {
        Integer pickUpRequestCode = Integer.parseInt(String.valueOf("2" + String.valueOf(order.oid)));
        Integer dropOffRequestCode = Integer.parseInt(String.valueOf("3" + String.valueOf(order.oid)));

        Intent intent = new Intent("com.google.android.c2dm.intent.RECEIVE");
        PendingIntent pPickUpIntent = PendingIntent.getBroadcast(this, pickUpRequestCode, intent, 0);
        PendingIntent pDropOffIntent = PendingIntent.getBroadcast(this, dropOffRequestCode, intent, 0);

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pPickUpIntent);
        alarmManager.cancel(pDropOffIntent);
    }

    public void setAlarm() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ArrayList<Alarm> arrayList = (ArrayList<Alarm>) getDBHelper().getAlarmDao().queryForAll();

                for (Alarm alarm : arrayList) {

                    if (alarm.date > System.currentTimeMillis()) {
                        Date date = new Date(alarm.date);

                        Intent intent = new Intent("com.google.android.c2dm.intent.RECEIVE");

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);

                        intent.putExtra("oid", String.valueOf(alarm.oid));

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

                        if (calendar.get(Calendar.MINUTE) == 0) {
                            intent.putExtra("message", String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) +
                                    getString(R.string.hour_text));
                        }
                        else
                            intent.putExtra("message", String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) +
                                    getString(R.string.hour_text) +
                                    " " +
                                    String.valueOf(calendar.get(Calendar.MINUTE)) +
                                    getString(R.string.minute_text));

                        PendingIntent pIntent = PendingIntent.getBroadcast(getBaseContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        // todo
                        AlarmManager alarmManager = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
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

    private DBHelper getDBHelper() {
        return CleanBasketApplication.getInstance().getDBHelper();
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

        getDBHelper().getAlarmDao().createOrUpdate(pickUpAlarm);
        getDBHelper().getAlarmDao().createOrUpdate(dropOffAlarm);
    }
}
