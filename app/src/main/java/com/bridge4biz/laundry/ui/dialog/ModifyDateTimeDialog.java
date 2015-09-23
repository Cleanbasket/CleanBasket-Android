package com.bridge4biz.laundry.ui.dialog;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.model.Order;
import com.bridge4biz.laundry.io.request.GetRequest;
import com.bridge4biz.laundry.io.request.PostRequest;
import com.bridge4biz.laundry.ui.DialogActivity;
import com.bridge4biz.laundry.ui.MainActivity;
import com.bridge4biz.laundry.ui.OrderInfoFragment;
import com.bridge4biz.laundry.ui.OrderStatusFragment;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.AlarmManager;
import com.bridge4biz.laundry.util.Constants;
import com.bridge4biz.laundry.util.DateTimeFactory;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ModifyDateTimeDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener, View.OnClickListener, DatePickerDialog.OnDateSetListener, Response.Listener<JSONObject>, Response.ErrorListener {
    private static final String TAG = ModifyDateTimeDialog.class.getSimpleName();

    private int mOid;
    private Order mOrder;

    private Date mSelectedPickUpDate = null;
    private Date mSelectedDropOffDate = null;

    // UI references.
    private TextView mTextViewPickUpDate;
    private TextView mTextViewPickUpTime;
    private TextView mTextViewDropOffDate;
    private TextView mTextViewDropOffTime;
    private TextView mTextViewFinish;
    private TextView mTextViewCancel;
    private View mModifyDateTimeFormView;
    private View mPickUpFormView;
    private View mProgressView;

    private OnDialogDismissListener mOnDialogDismissListener;

    public static interface OnDialogDismissListener {
        public abstract void onDialogDismiss();
    }

    public static ModifyDateTimeDialog newInstance(OnDialogDismissListener onDialogDismissListener, int oid) {
        ModifyDateTimeDialog pd = new ModifyDateTimeDialog();
        pd.initialize(onDialogDismissListener, oid);

        return pd;
    }

    public void initialize(OnDialogDismissListener onDialogDismissListener, int oid) {
        this.mOnDialogDismissListener = onDialogDismissListener;
        this.mOid = oid;

        Log.i(TAG, oid + " initialize");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getShowsDialog()) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

//        getDialog().setTitle(getString(R.string.label_order_modify));

        View rootView = inflater.inflate(R.layout.dialog_modify_date, container, false);

        mTextViewPickUpDate = (TextView) rootView.findViewById(R.id.textview_pick_up_date);
        mTextViewPickUpTime = (TextView) rootView.findViewById(R.id.textview_pick_up_time);
        mTextViewDropOffDate = (TextView) rootView.findViewById(R.id.textview_drop_off_date);
        mTextViewDropOffTime = (TextView) rootView.findViewById(R.id.textview_drop_off_time);
        mTextViewFinish = (TextView) rootView.findViewById(R.id.finish_order);

        Button buttonModify = (Button) rootView.findViewById(R.id.modify_datetime_button);
        Button buttonCancel = (Button) rootView.findViewById(R.id.cancel_modify_datetime_button);
        buttonModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptChange();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mTextViewFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mPickUpFormView = rootView.findViewById(R.id.layout_pickup_container);
        mModifyDateTimeFormView = rootView.findViewById(R.id.modify_datetime_form);
        mProgressView = rootView.findViewById(R.id.login_progress);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        int height = getDialog().getWindow().getAttributes().height;
        getDialog().getWindow().setLayout(width, height);

        final Drawable d = new ColorDrawable(Color.WHITE);

        getDialog().getWindow().setBackgroundDrawable(d);
    }

    @Override
    public void onResume() {
        super.onResume();

        getOrder();
    }

    private void getOrder() {
        showProgress(true);

        GetRequest getRequest = new GetRequest(getActivity());
        getRequest.setUrl(AddressManager.GET_ORDER);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonData jsonData;

                try {
                    jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response, JsonData.class);
                } catch (JsonSyntaxException e) {
                    return;
                }

                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        showProgress(false);

                        ArrayList<Order> orders = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, new TypeToken<ArrayList<Order>>(){}.getType());

                        if (getOrderInfo(orders) != null) {
                            mModifyDateTimeFormView.setVisibility(View.VISIBLE);
                            mTextViewFinish.setVisibility(View.GONE);
                            insertOrderInfo(getOrderInfo(orders));
                        }
                        else {
                            showProgress(true);
                            mTextViewFinish.setVisibility(View.VISIBLE);
                        }

                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CleanBasketApplication.getInstance().showToast(getString(R.string.toast_error));
                showProgress(false);
            }
        });
        RequestQueue.getInstance(getActivity()).addToRequestQueue(getRequest.doRequest());
    }

    private void insertOrderInfo(Order orderInfo) {
        if (orderInfo.state > OrderStatusFragment.PICK_UP_MAN_SELECTED)
            mPickUpFormView.setVisibility(View.GONE);

        mOrder = orderInfo;

        Log.i(TAG, orderInfo.pickup_date + " / " + orderInfo.dropoff_date);

        Date pickUpDate = DateTimeFactory.getInstance().getDate(orderInfo.pickup_date);
        Date dropOffDate = DateTimeFactory.getInstance().getDate(orderInfo.dropoff_date);

        mSelectedPickUpDate = pickUpDate;
        mSelectedDropOffDate = dropOffDate;

        mTextViewPickUpDate.setText(DateTimeFactory.getInstance().getStringDate(getActivity(), pickUpDate));
        pickUpTimeSelected(pickUpDate);
        mTextViewDropOffDate.setText(DateTimeFactory.getInstance().getStringDate(getActivity(), dropOffDate));
        dropOffTimeSelected(dropOffDate);

        mTextViewPickUpDate.setOnClickListener(this);
        mTextViewPickUpTime.setOnClickListener(this);
        mTextViewDropOffDate.setOnClickListener(this);
        mTextViewDropOffTime.setOnClickListener(this);
    }

    private Order getOrderInfo(ArrayList<Order> orders) {
        Order order = null;

        for (Order o : orders) {
            if (o.oid == mOid)
                order = o;

            Log.i(TAG, o.oid + " / " + mOid);
        }

        return order;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptChange() {
        showProgress(true);
        makeOrderData();
        transferOrder();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textview_pick_up_date:
                popUpPickUpOtherDate();
                break;

            case R.id.textview_pick_up_time:
                // 오늘을 선택하면 시간 선택에 제한을 둡니다
                if (isToday(mSelectedPickUpDate))
                    popUpPickUpTodayTime(OrderInfoFragment.PICK_UP_TIME);
                else
                    popTimePickerView(OrderInfoFragment.FASTEST_HOUR, OrderInfoFragment.FASTEST_MINUTE, OrderInfoFragment.PICK_UP_TIME);
                break;

            case R.id.textview_drop_off_date:
                popUpDropOffOtherDay();
                break;

            case R.id.textview_drop_off_time:
                // 가장 빠른 배달 날짜를 선택하면 시간 선택에 제한을 둡니다
                if (isFastestDay(mSelectedDropOffDate))
                    popUpDropOffFastestTime();
                else
                    popTimePickerView(OrderInfoFragment.FASTEST_HOUR, OrderInfoFragment.FASTEST_MINUTE, OrderInfoFragment.DROP_OFF_TIME);
                break;
        }
    }

    /**
     * 시간 선택 화면을 띄웁니다
     * @param hour 현재 시간
     * @param minute 현재 분
     * @param mode pick, drop 구분
     */
    private void popTimePickerView(int hour, int minute, int mode) {
        String header = getString(R.string.time_picker_inform);

        switch (mode) {
            case OrderInfoFragment.PICK_UP_DATETIME:
                header = DateTimeFactory.getInstance().getStringDate(getActivity(), mSelectedPickUpDate);
                break;

            case OrderInfoFragment.DROP_OFF_TIME:
                header = DateTimeFactory.getInstance().getStringDate(getActivity(), mSelectedPickUpDate);
                break;
        }

        TimePickerDialog radialTimePickerDialog = TimePickerDialog.newInstance(getActivity(), this, hour, minute, header, mode);

        radialTimePickerDialog.show(
                getActivity().getSupportFragmentManager(),
                OrderInfoFragment.TIME_PICKER_TAG);
    }

    /**
     * 날짜 선택 화면을 띄웁니다
     * @param min 선택 가능 최소 시간
     * @param max 선택 가능 최대 시간
     * @param withSelectedDate 초기 선택
     * @param mode pick, drop 구분
     */
    private void popDatePickerView(Date min, Date max, Date withSelectedDate, int mode) {
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, min, max, withSelectedDate, mode);

        datePickerDialog.show(
                getActivity().getSupportFragmentManager(),
                OrderInfoFragment.CALENDAR_PICKER_TAG);
    }

    private Calendar getCalendar() {
        return Calendar.getInstance();
    }

    /**
     * 시간 설정 콜백 함수
     * @param timePickerDialog 해당 다이얼로그
     * @param hour 선택된 시간
     * @param minute 선택된 분
     * @param mode pick, drop 구분
     */
    @Override
    public void onTimeSet(TimePickerDialog timePickerDialog, int hour, int minute, int mode) {
        Calendar pickUp = Calendar.getInstance();

        switch (mode) {
            case OrderInfoFragment.PICK_UP_DATETIME:
                pickUp = Calendar.getInstance();
                pickUp.setTime(mSelectedPickUpDate);
                pickUp.set(Calendar.HOUR_OF_DAY, hour);
                pickUp.set(Calendar.MINUTE, minute);
                pickUpDateSelected(pickUp.getTime());
                pickUpTimeSelected(pickUp.getTime());

                mSelectedPickUpDate = pickUp.getTime();

                /* 수거 날짜에 따라 배달 날짜를 조정합니다 */
                Calendar dropOff = pickUp;
                dropOff.add(Calendar.DAY_OF_WEEK, OrderInfoFragment.DEFAULT_DROP_OFF_DAY);

                mSelectedDropOffDate = dropOff.getTime();

                dropOffDateSelected(mSelectedDropOffDate);
                dropOffTimeSelected(mSelectedDropOffDate);

                CleanBasketApplication.getInstance().showToast(getString(R.string.pickup_date_change));
                break;

            case OrderInfoFragment.PICK_UP_TIME:
                pickUp.setTime(mSelectedPickUpDate);
                pickUp.set(Calendar.HOUR_OF_DAY, hour);
                pickUp.set(Calendar.MINUTE, minute);
                pickUpDateSelected(pickUp.getTime());
                pickUpTimeSelected(pickUp.getTime());

                if (isFastestDay(mSelectedDropOffDate)) {
                    dropOffTimeSelected(pickUp.getTime());
                }

                mSelectedPickUpDate = pickUp.getTime();
                break;

            case OrderInfoFragment.DROP_OFF_TIME:
                Calendar dropOffDateTime = Calendar.getInstance();
                dropOffDateTime.setTime(mSelectedDropOffDate);
                dropOffDateTime.set(Calendar.HOUR_OF_DAY, hour);
                dropOffDateTime.set(Calendar.MINUTE, minute);
                dropOffDateSelected(dropOffDateTime.getTime());
                dropOffTimeSelected(dropOffDateTime.getTime());

                mSelectedDropOffDate = dropOffDateTime.getTime();
                break;
        }
    }

    /**
     * 날짜 설정 콜백 함수
     * @param dialog 해당 다이얼로그
     * @param date 선택된 날짜
     * @param mode pick, drop 구분
     */
    @Override
    public void onDateSet(DatePickerDialog dialog, Date date, int mode) {
        switch (mode) {
            case OrderInfoFragment.PICK_UP_DATETIME:
                // 초기 선택
                mSelectedPickUpDate = date;
                if (isToday(date))
                    popUpPickUpTodayTime(OrderInfoFragment.PICK_UP_DATETIME);
                else
                    popTimePickerView(OrderInfoFragment.FASTEST_HOUR, OrderInfoFragment.FASTEST_MINUTE, OrderInfoFragment.PICK_UP_DATETIME);
                break;

//            case PICK_UP_DATE:
//                // 초기 선택
//                if (isToday(date)) {
//                    mSelectedPickUpDate = date;
//                    popUpPickUpTodayTime();
//                }
//                else {
//                    Calendar prevSelected = getCalendar();
//                    prevSelected.setTime(mSelectedPickUpDate);
//                    Calendar newSelected = getCalendar();
//                    newSelected.setTime(date);
//
//                    newSelected.set(Calendar.HOUR_OF_DAY, prevSelected.get(Calendar.HOUR_OF_DAY));
//                    newSelected.set(Calendar.MINUTE, prevSelected.get(Calendar.MINUTE));
//
//                    pickUpDateSelected(newSelected.getTime());
//                }
//                break;

            case OrderInfoFragment.DROP_OFF_DATE:
                // 배달 날짜만 선택
                mSelectedDropOffDate = date;
                if (isFastestDay(date))
                    popUpDropOffFastestTime();
                else
                    dropOffDateSelected(mSelectedDropOffDate);
                break;
        }
    }

    /**
     * 수거 날짜가 설정되면 이를 화면에 적용합니다
     * @param date 최종 선택된 수거 날짜
     */
    private void pickUpDateSelected(Date date) {
        mTextViewPickUpDate.setVisibility(View.VISIBLE);
        mTextViewPickUpDate.setText(
//                DateTimeFactory.getInstance().getPrettyTime(date) +
//                DateTimeFactory.getInstance().getNewLine() +
                DateTimeFactory.getInstance().getStringDate(getActivity(), date));
    }

    /**
     * 수거 시간이 설정되면 이를 화면에 적용합니다
     * @param date 최종 선택된 수거 시간
     */
    private void pickUpTimeSelected(Date date) {
        Calendar c = getCalendar();
        c.setTime(date);
        c.add(Calendar.HOUR_OF_DAY, 1);

        mTextViewPickUpTime.setVisibility(View.VISIBLE);
        mTextViewPickUpTime.setText(
                DateTimeFactory.getInstance().getStringTime(getActivity(), date) + " " +
                        getString(R.string.time_tilde) + " " +
                        DateTimeFactory.getInstance().getStringTime(getActivity(), c.getTime()));
    }

    /**
     * 배달 날짜가 선택되면 이를 적용합니다
     * @param date 최종 선택된 배달 날짜
     */
    private void dropOffDateSelected(Date date) {
        mTextViewDropOffDate.setText(
//                DateTimeFactory.getInstance().getPrettyTime(date) +
//                DateTimeFactory.getInstance().getNewLine() +
                DateTimeFactory.getInstance().getStringDate(getActivity(), date));
    }

    /**
     * 배달 시간이 선택되면 이를 적용합니다
     * @param date 최종 선택된 배달 시간
     */
    private void dropOffTimeSelected(Date date) {
        Calendar c = getCalendar();
        c.setTime(date);
        c.add(Calendar.HOUR_OF_DAY, 1);

        mTextViewDropOffTime.setText(
                DateTimeFactory.getInstance().getStringTime(getActivity(), date) + " " +
                        getString(R.string.time_tilde) + " " +
                        DateTimeFactory.getInstance().getStringTime(getActivity(), c.getTime()));
    }

    /**
     * 해당 날짜가 오늘인지 확인합니다
     * @param date 해당 날짜
     * @return boolean
     */
    private boolean isToday(Date date) {
        Calendar selectedDay = getCalendar();
        selectedDay.setTime(date);

        int monthOfYear = selectedDay.get(Calendar.MONTH);
        int dayOfMonth = selectedDay.get(Calendar.DAY_OF_WEEK);

        if (monthOfYear == getCalendar().get(Calendar.MONTH) &&
                dayOfMonth == getCalendar().get(Calendar.DAY_OF_WEEK))
            return true;

        return false;
    }

    /**
     * 해당 날짜가 배달 가능한 가장 빠른 날인지 확인합니다
     * @param date 해당 날짜
     * @return boolean
     */
    private boolean isFastestDay(Date date) {
        Calendar selectedDay = getCalendar();
        selectedDay.setTime(date);

        int monthOfYear = selectedDay.get(Calendar.MONTH);
        int dayOfMonth = selectedDay.get(Calendar.DAY_OF_WEEK);

        Calendar pickUpDay = getCalendar();
        pickUpDay.setTime(mSelectedPickUpDate);
        pickUpDay.add(Calendar.DAY_OF_WEEK, OrderInfoFragment.MIN_DROP_OFF_DAY);

        return monthOfYear == pickUpDay.get(Calendar.MONTH) &&
                dayOfMonth == pickUpDay.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 오늘을 수거 날짜로 골랐을 때 시간 선택
     */
    private void popUpPickUpTodayTime(int mode) {
        mSelectedPickUpDate = getCalendar().getTime();

        popTimePickerView(
                getCalendar().get(Calendar.HOUR_OF_DAY) + OrderInfoFragment.MIN_PICK_UP_TIME,
                getCalendar().get(Calendar.MINUTE),
                mode);
    }

    /**
     * 가장 빠른 배달 날짜를 골랐을 때 시간 선택
     */
    private void popUpDropOffFastestTime() {
        Calendar selectedPickUp = getCalendar();
        selectedPickUp.setTime(mSelectedPickUpDate);

        popTimePickerView(
                selectedPickUp.get(Calendar.HOUR_OF_DAY),
                selectedPickUp.get(Calendar.MINUTE),
                OrderInfoFragment.DROP_OFF_TIME);
    }

    /**
     * 수거 날짜만 선택합니다
     */
    private void popUpPickUpOtherDate() {
        Calendar max = getCalendar();
        max.add(Calendar.DAY_OF_WEEK, OrderInfoFragment.WEEK);

        Calendar other = Calendar.getInstance();
        other.add(Calendar.DAY_OF_WEEK, OrderInfoFragment.DEFAULT_OTHER_PICK_UP);

        popDatePickerView(
                getCalendar().getTime(),
                max.getTime(),
                other.getTime(),
                OrderInfoFragment.PICK_UP_DATETIME);
    }

    /**
     * 배달 날짜만 선택합니다
     */
    private void popUpDropOffOtherDay() {
        Calendar max = getCalendar();
        max.setTime(mSelectedPickUpDate);
        max.add(Calendar.DAY_OF_WEEK, OrderInfoFragment.WEEK);

        Calendar withSelectedDate = Calendar.getInstance();
        withSelectedDate.setTime(mSelectedPickUpDate);
        withSelectedDate.add(Calendar.DAY_OF_WEEK, 3);

        Calendar min = Calendar.getInstance();
        min.setTime(mSelectedPickUpDate);
        min.add(Calendar.DAY_OF_WEEK, 2);

        popDatePickerView(
                min.getTime(),
                max.getTime(),
                withSelectedDate.getTime(),
                OrderInfoFragment.DROP_OFF_DATE);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mModifyDateTimeFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mModifyDateTimeFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mModifyDateTimeFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mModifyDateTimeFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void makeOrderData() {
        mOrder.pickup_date = DateTimeFactory.getInstance().getStringDateTime(mSelectedPickUpDate);
        mOrder.dropoff_date = DateTimeFactory.getInstance().getStringDateTime(mSelectedDropOffDate);
    }

    private void transferOrder() {
        PostRequest postRequest = new PostRequest(getActivity());
        String body = CleanBasketApplication.getInstance().getGson().toJson(mOrder);

        try {
            JSONObject jsonObject = new JSONObject(body);
            postRequest.setParams(jsonObject);
        } catch (JSONException e) {
            return;
        }

        postRequest.setUrl(AddressManager.DATE_UPDATE_ORDER);
        postRequest.setListener(this, this);
        RequestQueue.getInstance(getActivity()).addToRequestQueue(postRequest.doRequest());
    }

    @Override
    public void onResponse(JSONObject response) {
        showProgress(false);

        JsonData jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);

        switch (jsonData.constant) {
            case Constants.ERROR:

                CleanBasketApplication.getInstance().showToast(getString(R.string.general_error));

                this.mOnDialogDismissListener.onDialogDismiss();
                break;

            case Constants.TOO_EARLY_TIME:
                CleanBasketApplication.getInstance().showToast(getString(R.string.too_early_time));

                this.mOnDialogDismissListener.onDialogDismiss();
                break;

            case Constants.TOO_LATE_TIME:
                CleanBasketApplication.getInstance().showToast(getString(R.string.too_late_time));

                this.mOnDialogDismissListener.onDialogDismiss();
                break;

            case Constants.SUCCESS:
                CleanBasketApplication.getInstance().showToast(getString(R.string.order_modify_success));

                Order order = new Order(
                        mOid,
                        DateTimeFactory.getInstance().getStringDateTime(mSelectedPickUpDate),
                        DateTimeFactory.getInstance().getStringDateTime(mSelectedDropOffDate)
                );

                if (getTag().equals(OrderStatusFragment.MODIFY_DATETIME_DIALOG_TAG)) {
                    AlarmManager.getInstance(getActivity()).cancelAlarm(order);
                    AlarmManager.getInstance(getActivity()).insertAlarm(order);
                    AlarmManager.getInstance(getActivity()).setAlarm();
                    ((MainActivity) getActivity()).getViewPager().setCurrentItem(1);
                }
                else if (getTag().equals(DialogActivity.MODIFY_TIME_DIALOG)) {
                    AlarmManager.getInstance(getActivity()).cancelAlarm(order);
                    AlarmManager.getInstance(getActivity()).insertAlarm(order);
                    AlarmManager.getInstance(getActivity()).setAlarm();
                }

                if (this.mOnDialogDismissListener != null)
                    this.mOnDialogDismissListener.onDialogDismiss();

                dismiss();
        }
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        showProgress(false);

        Toast.makeText(getActivity(), R.string.general_error,Toast.LENGTH_SHORT).show();
    }
}