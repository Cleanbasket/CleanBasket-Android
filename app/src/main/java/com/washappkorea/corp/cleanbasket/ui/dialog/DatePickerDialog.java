package com.washappkorea.corp.cleanbasket.ui.dialog;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.timessquare.CalendarPickerView;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.ui.OrderInfoFragment;
import com.washappkorea.corp.cleanbasket.util.HapticFeedbackController;

import java.util.Date;
import java.util.Locale;

public class DatePickerDialog extends DialogFragment implements View.OnClickListener {
    private OnDateSetListener mCallback;

    private int mode;
    private Date min;
    private Date max;
    private Date withSelectedDate;

    private CalendarPickerView calendar;
    private TextView mTextViewDateHeader;
    private Button mButtonAccept;
    private Button mButtonCancel;

    private HapticFeedbackController mHapticFeedbackController;

    /**
     * The callback interface used to indicate the user is done filling in the time (they clicked on the 'Set' button).
     */
    public interface OnDateSetListener {
        void onDateSet(DatePickerDialog dialog, Date selectedDay, int mode);
    }

    public static interface OnDialogDismissListener {
        public abstract void onDialogDismiss(DialogInterface dialoginterface);
    }

    public static DatePickerDialog newInstance(OnDateSetListener callback, Date min, Date max, Date withSelectedDate, int mode) {
        DatePickerDialog tpd = new DatePickerDialog();
        tpd.initialize(callback, min, max, withSelectedDate, mode);

        return tpd;
    }

    public void initialize(OnDateSetListener callback, Date min, Date max, Date withSelectedDate, int mode) {
        this.mCallback = callback;

        this.mode = mode;

        this.min = min;
        this.max = max;
        this.withSelectedDate = withSelectedDate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getShowsDialog()) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        View rootView = inflater.inflate(R.layout.dialog_date_picker, container, false);

        calendar = (CalendarPickerView) rootView.findViewById(R.id.calendar_view);

        mTextViewDateHeader = (TextView) rootView.findViewById(R.id.textview_time_header);
        mButtonAccept = (Button) rootView.findViewById(R.id.button_accept_date_picker);
        mButtonCancel = (Button) rootView.findViewById(R.id.button_cancel_date_picker);

        mHapticFeedbackController = new HapticFeedbackController(getActivity());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mHapticFeedbackController.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mHapticFeedbackController.stop();
    }

    public void tryVibrate() {
        mHapticFeedbackController.tryVibrate();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        calendar.init(min, max, Locale.KOREA)
                .inMode(CalendarPickerView.SelectionMode.SINGLE)
                .withSelectedDate(withSelectedDate);

        switch (mode) {
            case OrderInfoFragment.PICK_UP_DATETIME:
                mTextViewDateHeader.setText(getString(R.string.date_pickup));
                break;

            case OrderInfoFragment.DROP_OFF_DATE:
                mTextViewDateHeader.setText(getString(R.string.date_dropoff));
                break;
        }

        mButtonAccept.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_accept_date_picker:
                tryVibrate();

                mCallback.onDateSet(this, calendar.getSelectedDate(), mode);

                dismiss();
                break;

            case R.id.button_cancel_date_picker:
                dismiss();
                break;
        }
    }
}