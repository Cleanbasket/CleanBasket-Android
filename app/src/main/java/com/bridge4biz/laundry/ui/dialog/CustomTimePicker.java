package com.bridge4biz.laundry.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CustomTimePicker extends android.app.TimePickerDialog {
    private final static int TIME_PICKER_INTERVAL = 30;

    private final static String TAG = CustomTimePicker.class.getSimpleName();

    private Context mContext;
    private TimePicker timePicker;
    private final OnTimeSetListener callback;

    private int minHour = 9;
    private int maxHour = 23;

    private int minMinute = 0;

    private int currentHour = 0;
    private int currentMinute = 0;

    public CustomTimePicker(Context context, OnTimeSetListener callBack, String header, int hourOfDay, int minute, boolean is24HourView) {
        super(context, callBack, hourOfDay, minute, is24HourView);

        setTitle(header);

        mContext = context;

        currentHour = hourOfDay;
        currentMinute = minute;

        minHour = hourOfDay;
        minMinute = minute;

        this.callback = callBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (callback != null && timePicker != null) {
            timePicker.clearFocus();

            int time = 0;

            if (timePicker.getCurrentMinute() % 2 == 0)
                time = 30;

            callback.onTimeSet(timePicker, timePicker.getCurrentHour(), time);
        }
    }

    @Override
    protected void onStop() {

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        try {
            Class<?> classForid = Class.forName("com.android.internal.R$id");
            Field timePickerField = classForid.getField("timePicker");
            this.timePicker = (TimePicker) findViewById(timePickerField.getInt(null));
            Field field = classForid.getField("minute");

            NumberPicker mMinuteSpinner = (NumberPicker) timePicker.findViewById(field.getInt(null));
            mMinuteSpinner.setMinValue(1);
            mMinuteSpinner.setMaxValue(4);

            List<String> displayedValues = new ArrayList<String>();
            for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
                displayedValues.add(String.format("%02d", i));
            }

            for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
                displayedValues.add(String.format("%02d", i));
            }

            mMinuteSpinner.setDisplayedValues(displayedValues.toArray(new String[0]));
            mMinuteSpinner.setWrapSelectorWheel(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        boolean validHour = true;
        boolean validMinute = true;

        int currentMinute = 0;
        if (minute % 2 == 0) currentMinute = 30;

        if (hourOfDay < minHour) {
            validHour = false;
        }

        if (hourOfDay > maxHour) {
            validHour = false;
        }

        if (hourOfDay == minHour && currentMinute < minMinute) {
            validMinute = false;
        }

        if (validHour) {
            currentHour = hourOfDay;
        }

        if (validMinute) {
            currentMinute = minute;
        }

        updateTime(currentHour, currentMinute);
    }
}