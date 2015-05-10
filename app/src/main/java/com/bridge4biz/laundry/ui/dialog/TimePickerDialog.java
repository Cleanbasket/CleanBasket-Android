package com.bridge4biz.laundry.ui.dialog;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;

import java.util.ArrayList;
import java.util.List;

public class TimePickerDialog extends DialogFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private OnTimeSetListener mCallback;

    private int mInitialHourOfDay;
    private int mInitialMinute;
    private int mSelectedHourOfDay;
    private int mSelectedMinute;
    private int mMode;

    private String mHeader;

    private GridView mTimePickerGridView;
    private TimeUnitAdapter timeUnitAdapter;
    private TextView mTextViewTimeHeader;
    private TextView mTextViewTimeWindow;
    private Button mButtonAccept;
    private Button mButtonCancel;


    /**
     * The callback interface used to indicate the user is done filling in the time (they clicked on the 'Set' button).
     */
    public interface OnTimeSetListener {
        void onTimeSet(TimePickerDialog dialog, int hourOfDay, int minute, int mode);
    }

    public static interface OnDialogDismissListener {
        public abstract void onDialogDismiss(DialogInterface dialoginterface);
    }

    public static TimePickerDialog newInstance(OnTimeSetListener callback,
                                                     int hourOfDay, int minute, String header, int mode) {
        TimePickerDialog tpd = new TimePickerDialog();
        tpd.initialize(callback, hourOfDay, minute, header, mode);

        return tpd;
    }

    public void initialize(OnTimeSetListener callback, int hourOfDay, int minute, String header, int mode) {
        this.mCallback = callback;

        this.mInitialHourOfDay = hourOfDay;
        this.mInitialMinute = minute;

        this.mSelectedHourOfDay = -1;
        this.mSelectedMinute = -1;

        this.mHeader = header;
        this.mMode = mode;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getShowsDialog()) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        View rootView = inflater.inflate(R.layout.dialog_time_picker, container, false);

        mTextViewTimeHeader = (TextView) rootView.findViewById(R.id.textview_time_header);
        mTimePickerGridView = (GridView) rootView.findViewById(R.id.gridview_time_picker);
        mTextViewTimeWindow = (TextView) rootView.findViewById(R.id.textview_time_window);
        mButtonAccept = (Button) rootView.findViewById(R.id.button_accept_time_picker);
        mButtonCancel = (Button) rootView.findViewById(R.id.button_cancel_time_picker);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        timeUnitAdapter = new TimeUnitAdapter(getActivity(), R.layout.custom_time_view, makeAvailableTimeSet());
        mTimePickerGridView.setAdapter(timeUnitAdapter);
        mTimePickerGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        mTimePickerGridView.setOnItemClickListener(this);

        mTextViewTimeHeader.setText(mHeader);
        mTextViewTimeWindow.setText(getString(R.string.time_picker_inform));

        mButtonAccept.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_accept_time_picker:
                if (mSelectedHourOfDay < 0 || mSelectedMinute < 0) {
                    CleanBasketApplication.getInstance().showToast(getString(R.string.incorrect_time));
                    return;
                }

                if (mCallback != null) {
                    mCallback.onTimeSet(
                            TimePickerDialog.this,
                            getSelectedHourOfDay(),
                            getSelectedMinute(),
                            mMode);
                }

                dismiss();
                break;

            case R.id.button_cancel_time_picker:
                dismiss();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(timeUnitAdapter == null)
            return;

        int hour = timeUnitAdapter.getItem(position).hour;
        int minute = timeUnitAdapter.getItem(position).minute;

        mSelectedHourOfDay = hour;
        mSelectedMinute = minute;

        if(minute == 0)
            mTextViewTimeWindow.setText(hour +
                    getString(R.string.time_separate) +
                    getString(R.string.time_exact) + " " +
                    getString(R.string.time_tilde) + " " +
                    (hour + 1) +
                    getString(R.string.time_separate) +
                    getString(R.string.time_exact));
        else
            mTextViewTimeWindow.setText(hour +
                    getString(R.string.time_separate) +
                    getString(R.string.time_half) + " " +
                    getString(R.string.time_tilde) + " " +
                    (hour + 1) +
                    getString(R.string.time_separate) +
                    getString(R.string.time_half));
    }

    private ArrayList<TimeUnit> makeAvailableTimeSet() {
        ArrayList<TimeUnit> timeSet = new ArrayList<TimeUnit>();

        int firstHour = getFirstHour();
        int firstMinute = getFirstMinute();

        // 처음 선택 가능 시간이 정각이 아니라면 dummy unit 삽입
        if (firstHour < 24 && firstMinute == 30) {
            timeSet.add(new TimeUnit(-1, -1));
            timeSet.add(new TimeUnit(firstHour, firstMinute));
            firstHour++;
            firstMinute = 0;
        }

        for (int h = firstHour; h < 24; h++) {
            timeSet.add(new TimeUnit(firstHour, firstMinute));
            timeSet.add(new TimeUnit(firstHour, firstMinute + 30));
            firstHour++;
        }

        return timeSet;
    }

    private int getFirstHour() {
        int firstHour = mInitialHourOfDay;

        // 현재 시간이 30분보다 높으면 시간을 한 시간 올림
        if (mInitialMinute > 30)
            firstHour++;

        // 8시 이전은 무조건 10:00
        if (mInitialHourOfDay < 8)
            firstHour = 10;

        return firstHour;
    }

    private int getFirstMinute() {
        int firstMinute = mInitialMinute;

        // 현재 시간이 30분보다 높으면 0으로 만듦
        if (mInitialMinute > 30)
            firstMinute = 0;
        else if (mInitialMinute < 30 && mInitialMinute > 0)
            firstMinute = 30;

        // 8시 이전은 무조건 10:00
        if (mInitialHourOfDay < 8)
            firstMinute = 0;

        return firstMinute;
    }

    public int getSelectedHourOfDay() {
        return mSelectedHourOfDay;
    }

    public int getSelectedMinute() {
        return mSelectedMinute;
    }

    protected class TimeUnit {
        int hour;
        int minute;
        float discountRate;

        public TimeUnit(int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
        }

        public TimeUnit(int hour, int minute, float discountRate) {
            this.hour = hour;
            this.minute = minute;
            this.discountRate = discountRate;
        }
    }

    @Override
    public void onStart() {
        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        int height = getDialog().getWindow().getAttributes().height;
        getDialog().getWindow().setLayout(width, height);

        final Drawable d = new ColorDrawable(Color.BLACK);
        d.setAlpha(150);

        getDialog().getWindow().setBackgroundDrawable(d);

        super.onStart();
    }

    protected class TimeUnitAdapter extends ArrayAdapter<TimeUnit> {
        private LayoutInflater mLayoutInflater;

        public TimeUnitAdapter(Context context, int resource, List<TimeUnit> objects) {
            super(context, resource, objects);

            this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TimeUnitHolder holder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.custom_time_view, parent, false);
                holder = new TimeUnitHolder();
                holder.imageViewTimeUnit = (ImageView) convertView.findViewById(R.id.imageview_discount_time);
                holder.textViewTime = (TextView) convertView.findViewById(R.id.textview_time);
                holder.textViewDiscountRate = (TextView) convertView.findViewById(R.id.textview_discount_time);
                convertView.setTag(holder);
            } else
                holder = (TimeUnitHolder) convertView.getTag();

            holder.imageViewTimeUnit.setVisibility(View.INVISIBLE);

            // 아이템이 1개 이상 선택되었을 경우
            if (getItem(position).hour < 0)
                return convertView;

//            if (getItem(position).discountRate > 0) {
//                holder.imageViewTimeUnit.setVisibility(View.VISIBLE);
//                holder.textViewDiscountRate.setText(getItem(position).discountRate + "% 할인");
//            }
//            else

            if (getItem(position).minute == 0)
                holder.textViewTime.setText(getItem(position).hour +
                        getString(R.string.time_separate) +
                        getString(R.string.time_exact));
            else
                holder.textViewTime.setText(getItem(position).hour +
                        getString(R.string.time_separate) +
                        getString(R.string.time_half));

            return convertView;
        }

        protected class TimeUnitHolder {
            public ImageView imageViewTimeUnit;
            public TextView textViewTime;
            public TextView textViewDiscountRate;
        }
    }
}
