package com.bridge4biz.laundry.ui;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.Address;
import com.bridge4biz.laundry.io.model.Coupon;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.model.Order;
import com.bridge4biz.laundry.io.model.OrderItem;
import com.bridge4biz.laundry.io.model.map.GeocodeResponse;
import com.bridge4biz.laundry.io.request.PostRequest;
import com.bridge4biz.laundry.search.AddressSearcher;
import com.bridge4biz.laundry.search.OnFinishAddrSearchListener;
import com.bridge4biz.laundry.ui.dialog.ConfirmDialog;
import com.bridge4biz.laundry.ui.dialog.CouponDialog;
import com.bridge4biz.laundry.ui.dialog.DatePickerDialog;
import com.bridge4biz.laundry.ui.dialog.ItemListDialog;
import com.bridge4biz.laundry.ui.dialog.MileageDialog;
import com.bridge4biz.laundry.ui.dialog.TimePickerDialog;
import com.bridge4biz.laundry.ui.widget.CalculationInfo;
import com.bridge4biz.laundry.ui.widget.CalculationInfoAdapter;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.AlarmManager;
import com.bridge4biz.laundry.util.Constants;
import com.bridge4biz.laundry.util.DateTimeFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class OrderInfoFragment extends Fragment implements TimePickerDialog.OnTimeSetListener, MapReverseGeoCoder.ReverseGeoCodingResultListener, View.OnClickListener, DatePickerDialog.OnDateSetListener, MileageDialog.OnDialogDismissListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, Response.Listener<JSONObject>, Response.ErrorListener, EditText.OnEditorActionListener, AdapterView.OnItemClickListener, MileageDialog.OnMileageSetListener, CouponDialog.OnCouponSetListener, OnFinishAddrSearchListener {
    public static final String TAG = OrderInfoFragment.class.getSimpleName();
    public static final String TAG_MODIFY = OrderInfoFragment.class.getSimpleName() + "_MODIFY";
    public static final String TIME_PICKER_TAG = "TIME_PICKER";
    public static final String CALENDAR_PICKER_TAG = "CALENDAR_PICKER";
    public static final String ITEM_LIST_DIALOG_TAG_INFO = "ITEM_LIST_DIALOG_INFO";
    public static final String MILEAGE_DIALOG_TAG = "MILEAGE_DIALOG";
    public static final String COUPON_DIALOG_TAG = "COUPON_USE_DIALOG";

    public static final int FASTEST_HOUR = 10;
    public static final int FASTEST_MINUTE = 0;

    public static final int MIN_PICK_UP_TIME = 2;
    public static final int DEFAULT_OTHER_PICK_UP = 2;
    public static final int MIN_DROP_OFF_DAY = 2;
    public static final int DEFAULT_DROP_OFF_DAY = 3;

    public static final int WEEK = 7;
    public static final int PICK_UP_DATETIME = 0;
    public static final int PICK_UP_DATE = 1;
    public static final int PICK_UP_TIME = 2;
    public static final int DROP_OFF_DATE = 3;
    public static final int DROP_OFF_TIME = 4;

    public static final int GET_ADDRESS = 0;
    public static final int ADDRESS_RESULT = 1;

    public static final int PAYMENT_CARD = 0;
    public static final int PAYMENT_CASH = 1;

    public static final int FREE_PICK_UP_PRICE = 20000;
    public static final int MINIMUM_ORDER = 10000;

    private LinearLayout mHeader;
    private View mProgressView;

    private RelativeLayout mLayoutSelector;
    private TextView mTextViewSelectedPickUpDate;
    private TextView mTextViewSelectedPickUpTime;
    private TextView mTextViewDropOffTitle;
    private TextView mTextViewSelectedDropOffDate;
    private TextView mTextViewSelectedDropOffTime;
    private EditText mEditTextAddress;
    private EditText mEditTextDetailAddress;
    private EditText mEditTextContact;
    private EditText mEditTextMemo;
    private ImageView mImageViewCurrentLocation;
    private RadioButton mButtonCard;
    private RadioButton mButtonCash;
    private Button mButtonToday;
    private Button mButtonTomorrow;
    private Button mButtonEtc;
    private ListView mCalculationInfoListView;

    private Button mButtonOrder;

    private CalculationInfoAdapter mCalculationInfoAdapter;

    private Boolean mAddressFlag;
    private Boolean mOrderFlag = false;

    private Order mOrder;
    private Integer mPaymentMethod = 0;
    private Coupon mCoupon;

    private int mTotal;

    private Date mSelectedPickUpDate = null;
    private Date mSelectedDropOffDate = null;

    protected Location mLastLocation;
    protected GoogleApiClient mGoogleApiClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order_info, container, false);

        mHeader = (LinearLayout) inflater.inflate(R.layout.custom_order_info, null);

        mLayoutSelector = (RelativeLayout) mHeader.findViewById(R.id.layout_select_date);
        mTextViewSelectedPickUpDate = (TextView) mHeader.findViewById(R.id.textview_selected_pickup_date);
        mTextViewSelectedPickUpTime = (TextView) mHeader.findViewById(R.id.textview_selected_pickup_time);
        mTextViewDropOffTitle = (TextView) mHeader.findViewById(R.id.textview_drop_off_title);
        mTextViewSelectedDropOffDate = (TextView) mHeader.findViewById(R.id.textview_selected_dropoff_date);
        mTextViewSelectedDropOffTime = (TextView) mHeader.findViewById(R.id.textview_selected_dropoff_time);
        mEditTextAddress = (EditText) mHeader.findViewById(R.id.edittext_address);
        mEditTextDetailAddress = (EditText) mHeader.findViewById(R.id.edittext_detail_address);
        mEditTextContact = (EditText) mHeader.findViewById(R.id.edittext_contact);
        mEditTextMemo = (EditText) mHeader.findViewById(R.id.edittext_memo);
        mImageViewCurrentLocation = (ImageView) mHeader.findViewById(R.id.imageview_current_location);
        mButtonCard = (RadioButton) mHeader.findViewById(R.id.radiobutton_payment_card);
        mButtonCash = (RadioButton) mHeader.findViewById(R.id.radiobutton_payment_cash);
        mButtonToday = (Button) mHeader.findViewById(R.id.imageview_datetime_today);
        mButtonTomorrow = (Button) mHeader.findViewById(R.id.imageview_datetime_tomorrow);
        mButtonEtc = (Button) mHeader.findViewById(R.id.imageview_datetime_etc);

        mEditTextAddress.setOnEditorActionListener(this);
        mEditTextDetailAddress.setOnEditorActionListener(this);
        mEditTextContact.setOnEditorActionListener(this);
        mEditTextMemo.setOnEditorActionListener(this);

        mCalculationInfoListView = (ListView) rootView.findViewById(R.id.listview_calculation);
        mProgressView = rootView.findViewById(R.id.loading_progress);

        mButtonOrder = (Button) rootView.findViewById(R.id.button_order_finish);

        mTextViewSelectedPickUpDate.setVisibility(View.INVISIBLE);
        mTextViewSelectedPickUpTime.setVisibility(View.INVISIBLE);

        mTextViewSelectedDropOffDate.setVisibility(View.GONE);
        mTextViewSelectedDropOffTime.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
            case R.id.address:
                mEditTextDetailAddress.requestFocus();
                return true;

            case R.id.address_detail:
                mEditTextContact.requestFocus();
                return true;

            case R.id.contact:
                popUpPickUpOtherDateTime();
                return true;

            case R.id.memo_hint:
                return true;
        }

        return false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mEditTextContact.setText(getPhoneNumber());

        mImageViewCurrentLocation.setOnClickListener(this);
        mButtonToday.setOnClickListener(this);
        mButtonTomorrow.setOnClickListener(this);
        mButtonEtc.setOnClickListener(this);
        mTextViewSelectedPickUpDate.setOnClickListener(this);
        mTextViewSelectedPickUpTime.setOnClickListener(this);
        mTextViewSelectedDropOffDate.setOnClickListener(this);
        mTextViewSelectedDropOffTime.setOnClickListener(this);
        mTextViewSelectedDropOffDate.setClickable(false);
        mTextViewSelectedDropOffTime.setClickable(false);

        mAddressFlag = false;

        buildGoogleApiClient();

        ArrayList<CalculationInfo> calculationInfos = new ArrayList<CalculationInfo>();
        calculationInfos.add(new CalculationInfo("mileage", getString(R.string.mileage), 0, CalculationInfo.MILEAGE));
        calculationInfos.add(new CalculationInfo("coupon", getString(R.string.coupon), 0, CalculationInfo.COUPON));

        mCalculationInfoAdapter = new CalculationInfoAdapter(getActivity(), R.layout.item_calculation_info, calculationInfos);
        mCalculationInfoListView.addHeaderView(mHeader);
        mCalculationInfoListView.setAdapter(mCalculationInfoAdapter);
        mCalculationInfoListView.setOnItemClickListener(this);

        mButtonCard.setOnClickListener(this);
        mButtonCash.setOnClickListener(this);
        mButtonOrder.setOnClickListener(this);

        getAddressFromDB();
    }

    private void getAddressFromDB() {
        Address address = null;

        try {
            address = ((MainActivity) getActivity()).getDBHelper().getAddressDao().queryBuilder().orderBy(Address.ID, false).limit(1L).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (address != null) {
            mAddressFlag = true;
            mEditTextAddress.setText(address.address);
            mEditTextDetailAddress.setText(address.address_detail);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_datetime_today:
                // 오늘을 픽업 날짜로 선택합니다
                popUpPickUpTodayTime(PICK_UP_DATETIME);
                break;

            case R.id.imageview_datetime_tomorrow:
                // 내일을 픽업 날짜로 선택합니다
                Calendar tomorrow = Calendar.getInstance();
                tomorrow.add(Calendar.DAY_OF_WEEK, 1);
                mSelectedPickUpDate = tomorrow.getTime();

                popTimePickerView(FASTEST_HOUR, FASTEST_MINUTE, PICK_UP_DATETIME);
                break;

            case R.id.imageview_datetime_etc:
                // 다른 날을 픽업 날짜로 선택합니다
                popUpPickUpOtherDateTime();
                break;

            case R.id.imageview_current_location:
                // 주소 설정
                Intent intent = new Intent();
                intent.setAction("com.bridge4biz.laundry.ui.MapActivity");

                if (mLastLocation != null) {
                    intent.putExtra("latitude", mLastLocation.getLatitude());
                    intent.putExtra("longitude", mLastLocation.getLongitude());
                }

                startActivityForResult(intent, GET_ADDRESS);
                break;
            
            case R.id.textview_selected_pickup_date:
                popUpPickUpOtherDate();
                break;

            case R.id.textview_selected_pickup_time:
                // 오늘을 선택하면 시간 선택에 제한을 둡니다
                if (isToday(mSelectedPickUpDate))
                    popUpPickUpTodayTime(PICK_UP_TIME);
                else
                    popTimePickerView(FASTEST_HOUR, FASTEST_MINUTE, PICK_UP_DATETIME);
                break;

            case R.id.textview_selected_dropoff_date:
                popUpDropOffOtherDay();
                break;

            case R.id.textview_selected_dropoff_time:
                // 가장 빠른 배달 날짜를 선택하면 시간 선택에 제한을 둡니다
                if (isFastestDay(mSelectedDropOffDate))
                    popUpDropOffFastestTime();
                else
                    popTimePickerView(FASTEST_HOUR, FASTEST_MINUTE, DROP_OFF_TIME);
                break;

//            case R.id.button_gross_total:
//                ArrayList<OrderItem> mOrderItems = getOrderFragment().getOrderItemAdapter().getSelectedItems();
//                popItemListDialog(mOrderItems);
//               break;

            case R.id.button_order_finish:
                if (checkFormFilled()) {
                    popOrderConfirmDialog();
                    insertToDataBase();
                }
                break;

            case R.id.radiobutton_payment_card:
                mButtonCash.setChecked(false);
                mPaymentMethod = 0;
                break;

            case R.id.radiobutton_payment_cash:
                mButtonCard.setChecked(false);
                mPaymentMethod = 1;
                break;
        }
    }

    private void insertToDataBase() {
        ((MainActivity) getActivity()).getDBHelper().getAddressDao().createOrUpdate(
                new Address(mEditTextAddress.getText().toString(), mEditTextDetailAddress.getText().toString()));
    }

    private boolean checkFormFilled() {
        mEditTextAddress.setError(null);
        mEditTextDetailAddress.setError(null);
        mEditTextContact.setError(null);

        if (mEditTextAddress.getText().toString().equals("")) {
            mEditTextAddress.setError(getString(R.string.address_empty));
            return false;
        }

        if (mEditTextDetailAddress.getText().toString().equals("")) {
            mEditTextDetailAddress.setError(getString(R.string.address_detail_empty));
            return false;
        }

        if (mEditTextContact.getText().toString().equals("")) {
            mEditTextContact.setError(getString(R.string.phone_empty));
            return false;
        }

        if (mSelectedPickUpDate == null || mSelectedDropOffDate == null) {
            CleanBasketApplication.getInstance().showToast(getString(R.string.datetime_empty));
            return false;
        }

        return true;
    }

    private void makeOrderInfo() {
        mOrder = new Order();
        mOrder.coupon = new ArrayList<Coupon>();

        mOrder.phone = mEditTextContact.getText().toString();
        mOrder.address = mEditTextAddress.getText().toString();
        mOrder.addr_building = mEditTextDetailAddress.getText().toString();
        mOrder.memo = mEditTextMemo.getText().toString();
        mOrder.price = mCalculationInfoAdapter.getPriceByType(CalculationInfo.TOTAL);
        mOrder.dropoff_price = mCalculationInfoAdapter.getPriceByType(CalculationInfo.COST);
        mOrder.pickup_date = DateTimeFactory.getInstance().getStringDateTime(mSelectedPickUpDate);
        mOrder.dropoff_date = DateTimeFactory.getInstance().getStringDateTime(mSelectedDropOffDate);
        mOrder.mileage = mCalculationInfoAdapter.getPriceByType(CalculationInfo.MILEAGE);
        mOrder.sale = mCalculationInfoAdapter.getPriceByType(CalculationInfo.SALE);
        mOrder.payment_method = mPaymentMethod;

        if (mCoupon != null) {
            mOrder.coupon.add(mCoupon);
        }

        mOrder.item = getOrderFragment().getOrderItemAdapter().getSelectedItems();
    }

    private void popOrderConfirmDialog() {
        ConfirmDialog confirmDialog = ConfirmDialog.newInstance(new ConfirmDialog.OnConfirmListener() {
            @Override
            public void onConfirm(ConfirmDialog dialog) {
                showProgress(true);
                makeOrderInfo();
                transferOrder();
            }
        }, getOrderFragment().getOrderItemAdapter().getItemNumber(), mCalculationInfoAdapter.getPriceByType(CalculationInfo.TOTAL));

        confirmDialog.show(getActivity().getSupportFragmentManager(), TAG);
    }

    private void transferOrder() {
        if (mOrderFlag)
            return;

        mOrderFlag = true;

        PostRequest postRequest = new PostRequest(getActivity());
        String body = CleanBasketApplication.getInstance().getGson().toJson(mOrder);

        try {
            JSONObject jsonObject = new JSONObject(body);
            postRequest.setParams(jsonObject);
        } catch (JSONException e) {
            return;
        }

        postRequest.setUrl(AddressManager.ADD_ORDER);
        postRequest.setListener(this, this);
        RequestQueue.getInstance(getActivity()).addToRequestQueue(postRequest.doRequest().setRetryPolicy(
                new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(10),
                Config.DEFAULT_MAX_RETRIES,
                Config.DEFAULT_BACKOFF_MULT)));
    }

    @Override
    public void onResponse(JSONObject response) {
        showProgress(false);

        JsonData jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);

        switch (jsonData.constant) {
            case Constants.AREA_UNAVAILABLE:
                CleanBasketApplication.getInstance().showToast(getString(R.string.area_unavailable_error));
                break;

            case Constants.DATE_UNAVAILABLE:
                CleanBasketApplication.getInstance().showToast(getString(R.string.date_unavailable_error));
                break;

            case Constants.ERROR:
                CleanBasketApplication.getInstance().showToast(getString(R.string.general_error));
                break;

            case Constants.SESSION_EXPIRED:
                CleanBasketApplication.getInstance().showToast(getString(R.string.session_invalid));
                break;

            case Constants.SUCCESS:
                CleanBasketApplication.getInstance().showToast(getString(R.string.order_success));

                Order order = new Order(
                        Integer.parseInt(jsonData.data),
                        DateTimeFactory.getInstance().getStringDateTime(mSelectedPickUpDate),
                        DateTimeFactory.getInstance().getStringDateTime(mSelectedDropOffDate)
                );

                AlarmManager.getInstance(getActivity()).insertAlarm(order);
                AlarmManager.getInstance(getActivity()).setAlarm();
                ((MainActivity) getActivity()).getViewPager().setCurrentItem(1);
                getActivity().getSupportFragmentManager().popBackStack();
//                getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }

        mOrderFlag = false;
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        showProgress(false);

        mOrderFlag = false;

        CleanBasketApplication.getInstance().showToast(getString(R.string.general_error));
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
            case PICK_UP_DATETIME:
            case PICK_UP_TIME:
                header = DateTimeFactory.getInstance().getStringDate(getActivity(), mSelectedPickUpDate);
                break;

            case DROP_OFF_TIME:
                header = DateTimeFactory.getInstance().getStringDate(getActivity(), mSelectedDropOffDate);
                break;
        }

        TimePickerDialog radialTimePickerDialog = TimePickerDialog.newInstance(getActivity(), this, hour, minute, header, mode);

        radialTimePickerDialog.show(
                getActivity().getSupportFragmentManager(),
                TIME_PICKER_TAG);
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
                CALENDAR_PICKER_TAG);
    }

    private Calendar getCalendar() {
        return Calendar.getInstance();
    }

    /**
     * 시간 설정 콜백 함수
     * @param timePickerDialog
     * @param hour 선택된 시간
     * @param minute 선택된 분
     * @param mode pick, drop 구분
     */
    @Override
    public void onTimeSet(TimePickerDialog timePickerDialog, int hour, int minute, int mode) {
        Calendar pickUp = Calendar.getInstance();

        mTextViewSelectedDropOffDate.setVisibility(View.VISIBLE);
        mTextViewSelectedDropOffTime.setVisibility(View.VISIBLE);
        mTextViewDropOffTitle.setVisibility(View.GONE);

        switch (mode) {
            case PICK_UP_DATETIME:
                pickUp = Calendar.getInstance();
                pickUp.setTime(mSelectedPickUpDate);
                pickUp.set(Calendar.HOUR_OF_DAY, hour);
                pickUp.set(Calendar.MINUTE, minute);
                pickUpDateSelected(pickUp.getTime());
                pickUpTimeSelected(pickUp.getTime());

                mSelectedPickUpDate = pickUp.getTime();

                /* 수거 날짜에 따라 배달 날짜를 조정합니다 */
                Calendar dropOff = pickUp;
                dropOff.add(Calendar.DAY_OF_WEEK, DEFAULT_DROP_OFF_DAY);

                mSelectedDropOffDate = dropOff.getTime();

                dropOffDateSelected(mSelectedDropOffDate);
                dropOffTimeSelected(mSelectedDropOffDate);

                /* 배달 시간을 선택할 수 있도록 합니다 */
                mTextViewSelectedDropOffDate.setClickable(true);
                mTextViewSelectedDropOffTime.setClickable(true);

                CleanBasketApplication.getInstance().showToast(getString(R.string.pickup_date_change));
                break;

            case PICK_UP_TIME:
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

            case DROP_OFF_TIME:
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

    private void showMileageDialog() {
        MileageDialog md = MileageDialog.newInstance(this);
        md.show(getActivity().getSupportFragmentManager(), MILEAGE_DIALOG_TAG);
    }

    private void showCouponDialog() {
        CouponDialog cd = CouponDialog.newInstance(this);
        cd.show(getActivity().getSupportFragmentManager(), COUPON_DIALOG_TAG);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 1:
                ArrayList<OrderItem> mOrderItems = getOrderFragment().getOrderItemAdapter().getSelectedItems();
                popItemListDialog(mOrderItems);
                break;

            case 2:
                if (mCalculationInfoAdapter.getCalculationInfoByType(CalculationInfo.COST) == null)
                    showMileageDialog();
                break;

            case 3:
                if (mCalculationInfoAdapter.getCalculationInfoByType(CalculationInfo.COST) == null)
                    showCouponDialog();
                else
                    showMileageDialog();
                break;

            case 4:
                if (mCalculationInfoAdapter.getCalculationInfoByType(CalculationInfo.COST) != null)
                    showCouponDialog();
                break;
        }

        Log.i(TAG, id + "");
    }

    @Override
    public void onCouponSet(CouponDialog dialog, Coupon coupon) {
        CalculationInfo calculationInfo = mCalculationInfoAdapter.getCalculationInfoByType(CalculationInfo.COUPON);

        if (coupon == null) {
            calculationInfo.name = getString(R.string.coupon);
            calculationInfo.price = 0;
        }
        else if (MINIMUM_ORDER > getOrderFragment().getOrderItemAdapter().getItemTotal()) {
            calculationInfo.name = getString(R.string.coupon);
            calculationInfo.price = 0;

            CleanBasketApplication.getInstance().showToast(getString(R.string.coupon_not_available));
        }
        else {
            calculationInfo.name = coupon.name;
            calculationInfo.price = coupon.value;
        }

        mCoupon = coupon;

        mCalculationInfoAdapter.getCalculationInfoByType(CalculationInfo.TOTAL).price = mTotal + mCalculationInfoAdapter.getTotal();

        mCalculationInfoAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMileageSet(MileageDialog dialog, int mileage) {
        CalculationInfo calculationInfo = mCalculationInfoAdapter.getCalculationInfoByType(CalculationInfo.MILEAGE);

        calculationInfo.price = mileage;

        mCalculationInfoAdapter.getCalculationInfoByType(CalculationInfo.TOTAL).price = mTotal + mCalculationInfoAdapter.getTotal();

        mCalculationInfoAdapter.notifyDataSetChanged();
    }

    /**
     * 날짜 설정 콜백 함수
     * @param dialog
     * @param date 선택된 날짜
     * @param mode pick, drop 구분
     */
    @Override
    public void onDateSet(DatePickerDialog dialog, Date date, int mode) {
        switch (mode) {
            case PICK_UP_DATETIME:
                // 초기 선택
                mSelectedPickUpDate = date;
                if (isToday(date))
                    popUpPickUpTodayTime(PICK_UP_DATETIME);
                else
                    popTimePickerView(FASTEST_HOUR, FASTEST_MINUTE, PICK_UP_DATETIME);
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

            case DROP_OFF_DATE:
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
        mLayoutSelector.setVisibility(View.GONE);
        mTextViewSelectedPickUpDate.setVisibility(View.VISIBLE);
        mTextViewSelectedPickUpDate.setText(
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

        mTextViewSelectedPickUpTime.setVisibility(View.VISIBLE);
        mTextViewSelectedPickUpTime.setText(
                DateTimeFactory.getInstance().getStringTime(getActivity(), date) + " " +
                        getString(R.string.time_tilde) + " " +
                        DateTimeFactory.getInstance().getStringTime(getActivity(), c.getTime()));
    }

    /**
     * 배달 날짜가 선택되면 이를 적용합니다
     * @param date 최종 선택된 배달 날짜
     */
    private void dropOffDateSelected(Date date) {
        mTextViewSelectedDropOffDate.setText(
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

        mTextViewSelectedDropOffTime.setText(
                DateTimeFactory.getInstance().getStringTime(getActivity(), date) + " " +
                        getString(R.string.time_tilde) + " " +
                        DateTimeFactory.getInstance().getStringTime(getActivity(), c.getTime()));
    }

    /**
     * 해당 날짜가 오늘인지 확인합니다
     * @param date
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
     * @param date
     * @return boolean
     */
    private boolean isFastestDay(Date date) {
        Calendar selectedDay = getCalendar();
        selectedDay.setTime(date);

        int monthOfYear = selectedDay.get(Calendar.MONTH);
        int dayOfMonth = selectedDay.get(Calendar.DAY_OF_WEEK);

        Calendar pickUpDay = getCalendar();
        pickUpDay.setTime(mSelectedPickUpDate);
        pickUpDay.add(Calendar.DAY_OF_WEEK, MIN_DROP_OFF_DAY);

        if (monthOfYear == pickUpDay.get(Calendar.MONTH) &&
                dayOfMonth == pickUpDay.get(Calendar.DAY_OF_WEEK))
            return true;

        return false;
    }

    /**
     * 오늘을 수거 날짜로 골랐을 때 시간 선택
     */
    private void popUpPickUpTodayTime(int mode) {
        mSelectedPickUpDate = getCalendar().getTime();

        popTimePickerView(
                getCalendar().get(Calendar.HOUR_OF_DAY) + MIN_PICK_UP_TIME,
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
                DROP_OFF_TIME);
    }

    /**
     * 수거 날짜와 시간을 선택합니다
     */
    private void popUpPickUpOtherDateTime() {
        Calendar max = getCalendar();
        max.add(Calendar.DAY_OF_WEEK, WEEK);

        Calendar other = Calendar.getInstance();
        other.add(Calendar.DAY_OF_WEEK, DEFAULT_OTHER_PICK_UP);

        popDatePickerView(
                getCalendar().getTime(),
                max.getTime(),
                other.getTime(),
                PICK_UP_DATETIME);
    }

    /**
     * 수거 날짜만 선택합니다
     */
    private void popUpPickUpOtherDate() {
        Calendar max = getCalendar();
        max.add(Calendar.DAY_OF_WEEK, WEEK);

        Calendar other = Calendar.getInstance();
        other.add(Calendar.DAY_OF_WEEK, DEFAULT_OTHER_PICK_UP);

        popDatePickerView(
                getCalendar().getTime(),
                max.getTime(),
                other.getTime(),
                PICK_UP_DATETIME);
    }

    /**
     * 배달 날짜만 선택합니다
     */
    private void popUpDropOffOtherDay() {
        Calendar max = getCalendar();
        max.setTime(mSelectedPickUpDate);
        max.add(Calendar.DAY_OF_WEEK, WEEK);

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
                DROP_OFF_DATE);
    }

    private void popItemListDialog(ArrayList<OrderItem> orderItems) {
        ItemListDialog itemListDialog =
                ItemListDialog.newInstance(orderItems, null);

        itemListDialog.show(
                getActivity().getSupportFragmentManager(),
                ITEM_LIST_DIALOG_TAG_INFO);
    }

    /* 스마트폰 번호를 가져옵니다 */
    private String getPhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager) getActivity().getSystemService(getActivity().TELEPHONY_SERVICE);
        String mPhoneNumber;
        mPhoneNumber = tMgr.getLine1Number();

        if (mPhoneNumber != null) {
            mPhoneNumber = mPhoneNumber.replace("+82", "0");

            if(mPhoneNumber.length() != 11) {
                return "";
            }
        }

        return mPhoneNumber;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case ADDRESS_RESULT:
                String resultAddress = data.getExtras().getString("address");
                mEditTextAddress.setText(resultAddress);
                mEditTextAddress.setError(null);

                if (mGoogleApiClient.isConnected())
                    mGoogleApiClient.disconnect();

                mAddressFlag = true;

                mEditTextDetailAddress.requestFocus();
                showSoftKeyboard(mEditTextDetailAddress);
                break;
        }
    }

    private void showSoftKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideSoftKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();

        setCalculationInfo();
        mCalculationInfoAdapter.mAuthUser = ((MainActivity) getActivity()).mAuthUser;
        mCalculationInfoAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null && !mAddressFlag) {
            AddressSearcher addressSearcher = new AddressSearcher();
            addressSearcher.searchAddr(getActivity(), mLastLocation.getLatitude(), mLastLocation.getLongitude(), this);
        }
    }

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        mEditTextAddress.setText(s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {

    }

    @Override
    public void onSuccess(GeocodeResponse geocodeResponse) {
        String address = "";

        if (geocodeResponse.getStatus().equals(MapActivity.OVER_QUERY_LIMIT)) {
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MapReverseGeoCoder reverseGeoCoder = new MapReverseGeoCoder(Config.DAUM_MAP_API, mapPoint, this, getActivity());
            reverseGeoCoder.startFindingAddress();
        }

        if (geocodeResponse.getResults().size() == 0)
            return;
        else
            address = geocodeResponse.getResults().get(0).getFormatted_address();

        String[] fullAddress = address.split(" ");
        mEditTextAddress.setText(fullAddress[1] + " " + fullAddress[2] + " " + fullAddress[3]);
    }

    @Override
    public void onFail() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    private OrderFragment getOrderFragment() {
        return (OrderFragment) getActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":0");
    }

    public void setCalculationInfo() {
        mTotal = getOrderFragment().getOrderItemAdapter().getItemTotal();
        mCalculationInfoAdapter.mTotal = mTotal;
        int totalItemNumber = getOrderFragment().getOrderItemAdapter().getItemNumber();

//        mButtonGrossTotal.setOnClickListener(this);
//        mButtonGrossTotal.setText(
//                totalItemNumber +
//                        getString(R.string.item_unit) +
//                        " / " +
//                        mTotal +
//                        getString(R.string.monetary_unit));

        /* 선택 아이템 총계 계산 */
        CalculationInfo calculationPreTotalInfo = mCalculationInfoAdapter.getCalculationInfoByType(CalculationInfo.PRE_TOTAL);

        if(calculationPreTotalInfo == null)
            mCalculationInfoAdapter.add(new CalculationInfo(null, getString(R.string.label_item) + " " + totalItemNumber + getString(R.string.item_unit), mTotal, CalculationInfo.PRE_TOTAL));
        else {
            calculationPreTotalInfo.name = getString(R.string.label_item) + " " + totalItemNumber + getString(R.string.item_unit);
            calculationPreTotalInfo.price = mTotal;
        }

        /* 수거배달비 계산 */
        CalculationInfo calculationCostInfo = mCalculationInfoAdapter.getCalculationInfoByType(CalculationInfo.COST);

        if (calculationCostInfo != null && mTotal >= FREE_PICK_UP_PRICE)
            mCalculationInfoAdapter.remove(calculationCostInfo);
        else if (calculationCostInfo == null && mTotal < FREE_PICK_UP_PRICE)
            mCalculationInfoAdapter.add(new CalculationInfo("pick_up_cost", getString(R.string.pick_up_cost), 2000, CalculationInfo.COST));

        /* 총계 계산 */
        CalculationInfo calculationTotalInfo = mCalculationInfoAdapter.getCalculationInfoByType(CalculationInfo.TOTAL);

        if (calculationTotalInfo == null)
            mCalculationInfoAdapter.add(new CalculationInfo(null, getString(R.string.label_total), mTotal + mCalculationInfoAdapter.getTotal(), CalculationInfo.TOTAL));
        else
            mCalculationInfoAdapter.getCalculationInfoByType(CalculationInfo.TOTAL).price = mTotal + mCalculationInfoAdapter.getTotal();

        /* Type 순으로 정렬 */
        mCalculationInfoAdapter.sort(new Comparator<CalculationInfo>() {
            @Override
            public int compare(CalculationInfo lhs, CalculationInfo rhs) {
                return lhs.type - rhs.type;
            }
        });
    }

    @Override
    public void onDialogDismiss(DialogInterface dialoginterface) {

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

            mCalculationInfoListView.setVisibility(show ? View.GONE : View.VISIBLE);
            mCalculationInfoListView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCalculationInfoListView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mProgressView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
