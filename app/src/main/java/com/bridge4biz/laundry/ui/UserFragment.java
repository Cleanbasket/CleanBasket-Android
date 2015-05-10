package com.bridge4biz.laundry.ui;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.AuthUser;
import com.bridge4biz.laundry.io.model.Coupon;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.request.GetRequest;
import com.bridge4biz.laundry.io.request.PostRequest;
import com.bridge4biz.laundry.ui.dialog.ClassDialog;
import com.bridge4biz.laundry.ui.dialog.CouponDialog;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;
import com.bridge4biz.laundry.util.UserEmailFetcher;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserFragment extends Fragment implements ListView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = UserFragment.class.getSimpleName();

    public static final String COUPON_VIEW_DIALOG_TAG = "COUPON_VIEW_DIALOG";
    public static final String ANDROID_AGENT = "ANDROID";
    public static final Integer SETTING = 0;

    public static final int BRONZE = 0;
    public static final int SILVER = 1;
    public static final int GOLD = 2;
    public static final int LOVE = 3;

    private LayoutInflater mInflater;

    private View mRegisterView;
    private View mUserInfoView;

    // UserInfoView
    private ImageView mImageViewUserClass;
    private TextView mUserName;
    private TextView mUserClass;
    private TextView mUserClassInfo;
    private TextView mUserClassMileage;
    private Button mButtonClassInfo;

    // RegisterView
    private EditText mEditTextEmail;
    private EditText mEditTextPhone;
    private EditText mEditTextAuthorization;
    private TextView mCheckedTextViewAll;
    private TextView mCheckedTextViewService;
    private TextView mCheckedTextViewProtection;
    private CheckBox mCheckBoxAll;
    private CheckBox mCheckBoxService;
    private CheckBox mCheckBoxProtection;
    private Button mButtonRequestCode;
    private Button mButtonRegister;

    private View mProgressView;
    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);

        mInflater = inflater;

        mProgressView = rootView.findViewById(R.id.loading_progress);
        mListView = (ListView) rootView.findViewById(R.id.listview_menu);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
        menuItems.add(new MenuItem(getString(R.string.coupon), "coupon_info"));
        menuItems.add(new MenuItem(getString(R.string.notification), "notification"));
        menuItems.add(new MenuItem(getString(R.string.setting), "setting"));
        menuItems.add(new MenuItem(getString(R.string.service_info), "service_info"));

        MenuAdapter menuAdapter = new MenuAdapter(getActivity(), R.layout.item_menu, menuItems);
        mListView.setAdapter(menuAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        getUserInfo();
    }

    private void getUserInfo() {
        showProgress(true);
        GetRequest getRequest = new GetRequest(getActivity());
        getRequest.setUrl(AddressManager.GET_AUTH_MEMBER_INFO);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonData jsonData = null;

                try {
                    jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response, JsonData.class);
                } catch (JsonSyntaxException e) {
                    return;
                }

                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        showProgress(false);

                        if (jsonData.data.equals("null"))
                            showRegisterHeader();
                        else {
                            AuthUser authUser = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, AuthUser.class);
                            showUserInfoHeader(authUser);
                        }
                        break;

                    default:
                        showProgress(false);
                        showRegisterHeader();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
            }
        });
        RequestQueue.getInstance(getActivity()).addToRequestQueue(getRequest.doRequest());
    }

    private void showUserInfoHeader(AuthUser authUser) {
        // UserInfoView
        if (mUserInfoView == null) {
            mUserInfoView = mInflater.inflate(R.layout.custom_user_info, null);
            mImageViewUserClass = (ImageView) mUserInfoView.findViewById(R.id.imageview_user_class);
            mUserName = (TextView) mUserInfoView.findViewById(R.id.textview_user_name);
            mUserClass = (TextView) mUserInfoView.findViewById(R.id.textview_user_class);
            mUserClassInfo = (TextView) mUserInfoView.findViewById(R.id.textview_user_class_info);
            mUserClassMileage = (TextView) mUserInfoView.findViewById(R.id.textview_user_class_mileage);
            mButtonClassInfo = (Button) mUserInfoView.findViewById(R.id.button_view_class_info);

            mImageViewUserClass.setImageResource(getDrawableByClass(authUser.user_class));
            mUserName.setText(authUser.email);
            mUserClass.setText(getClassName(authUser.user_class));
            mUserClassInfo.setText(getClassDetail(authUser.user_class));
            mButtonClassInfo.setOnClickListener(this);

            if (mRegisterView != null)
                mListView.removeHeaderView(mRegisterView);
            mListView.addHeaderView(mUserInfoView);
        }

        if (mUserClassMileage != null)
            mUserClassMileage.setText(getString(R.string.mileage_available) + " "  + authUser.mileage);
    }

    private int getDrawableByClass(Integer user_class) {
        switch (user_class) {
            case BRONZE:
                return R.drawable.ic_class_clean;
            case SILVER:
                return R.drawable.ic_class_silver;
            case GOLD:
                return R.drawable.ic_class_gold;
            case LOVE:
                return R.drawable.ic_class_love;
        }

        return R.drawable.ic_class_clean;
    }

    private String getClassName(Integer user_class) {
        switch (user_class) {
            case BRONZE:
                return getString(R.string.bronze_basket);
            case SILVER:
                return getString(R.string.silver_basket);
            case GOLD:
                return getString(R.string.gold_basket);
            case LOVE:
                return getString(R.string.love_basket);
        }

        return getString(R.string.bronze_basket);
    }

    private String getClassDetail(Integer user_class) {
        switch (user_class) {
            case BRONZE:
                return getString(R.string.bronze_info);
            case SILVER:
                return getString(R.string.silver_info);
            case GOLD:
                return getString(R.string.bronze_info);
            case LOVE:
                return getString(R.string.love_info);
        }

        return getString(R.string.bronze_info);
    }

    private void showRegisterHeader() {
        // RegisterView
        if (mRegisterView == null) {
            mRegisterView = mInflater.inflate(R.layout.custom_register, null);
            mEditTextEmail = (EditText) mRegisterView.findViewById(R.id.edittext_email);
            mEditTextPhone = (EditText) mRegisterView.findViewById(R.id.edittext_phone);
            mEditTextAuthorization = (EditText) mRegisterView.findViewById(R.id.edittext_authorization);
            mCheckedTextViewAll = (TextView) mRegisterView.findViewById(R.id.checkbox_agree_all);
            mCheckedTextViewService = (TextView) mRegisterView.findViewById(R.id.checkedtextview_service);
            mCheckedTextViewProtection = (TextView) mRegisterView.findViewById(R.id.checkedtextview_protection);
            mButtonRequestCode = (Button) mRegisterView.findViewById(R.id.button_authorization);
            mButtonRegister = (Button) mRegisterView.findViewById(R.id.register_button);
            mCheckBoxAll = (CheckBox) mRegisterView.findViewById(R.id.checkbox_agree_all);
            mCheckBoxService = (CheckBox) mRegisterView.findViewById(R.id.checkbox_service);
            mCheckBoxProtection = (CheckBox) mRegisterView.findViewById(R.id.checkbox_protection);
            mEditTextEmail.setText(UserEmailFetcher.getEmail(getActivity()));
            mEditTextPhone.setText(getPhoneNumber());

            mButtonRequestCode.setOnClickListener(this);
            mButtonRegister.setOnClickListener(this);
            mCheckedTextViewService.setOnClickListener(this);
            mCheckedTextViewProtection.setOnClickListener(this);
            mCheckBoxAll.setOnClickListener(this);

            mListView.addHeaderView(mRegisterView);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();

        switch (v.getId()) {
            case R.id.button_view_class_info:
                ClassDialog classDialog = ClassDialog.newInstance();

                classDialog.show(
                        getActivity().getSupportFragmentManager(),
                        "CLASS");
                break;

            case R.id.button_authorization:
                getActivity().registerReceiver(smsBroadCastReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
                requestAuthorizationCode();
                break;

            case R.id.checkedtextview_service:
                intent.setAction("com.bridge4biz.laundry.ui.WebViewActivity");
                intent.putExtra("type", WebViewActivity.TERM_OF_USE);
                startActivity(intent);
                break;

            case R.id.checkedtextview_protection:
                intent.setAction("com.bridge4biz.laundry.ui.WebViewActivity");
                intent.putExtra("type", WebViewActivity.PRIVACY);
                startActivity(intent);
                break;

            case R.id.register_button:
                checkAvailable();
                break;

            case R.id.checkbox_agree_all:
                mCheckBoxService.setChecked(mCheckBoxAll.isChecked());
                mCheckBoxProtection.setChecked(mCheckBoxAll.isChecked());
                break;
        }
    }

    private void checkAvailable() {
        // Reset errors.
        mEditTextEmail.setError(null);
        mEditTextPhone.setError(null);
        mEditTextAuthorization.setError(null);

        // Store values at the time of the login attempt.
        String email = mEditTextEmail.getText().toString();
        String phone = mEditTextPhone.getText().toString();
        String code = mEditTextAuthorization.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(phone)) {
            mEditTextPhone.setError(getString(R.string.error_field_required));
            focusView = mEditTextPhone;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(code)) {
            mEditTextAuthorization.setError(getString(R.string.error_field_required));
            focusView = mEditTextAuthorization;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEditTextEmail.setError(getString(R.string.error_field_required));
            focusView = mEditTextEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEditTextEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEditTextEmail;
            cancel = true;
        }

        if (!mCheckBoxService.isChecked() || !mCheckBoxProtection.isChecked()) {
            CleanBasketApplication.getInstance().showToast(getString(R.string.need_agree));
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            if (focusView != null)
                focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            startRegister();
        }
    }

    private void startRegister() {
        AuthUser authUser = new AuthUser();
        authUser.email = mEditTextEmail.getText().toString();
        authUser.phone = mEditTextPhone.getText().toString();
        authUser.code = mEditTextAuthorization.getText().toString();
        authUser.agent = ANDROID_AGENT;

        String body = CleanBasketApplication.getInstance().getGson().toJson(authUser);

        PostRequest postRequest = new PostRequest(getActivity());
        postRequest.setUrl(AddressManager.AUTH_REGISTER);

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(body);
            postRequest.setParams(jsonObject);
        } catch (JSONException e) {
            CleanBasketApplication.getInstance().showToast(getString(R.string.general_error));

            return;
        }

        postRequest.setListener(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JsonData jsonData = null;

                try {
                    jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);
                } catch (JsonSyntaxException e) {
                    return;
                }

                switch (jsonData.constant) {
                    case Constants.AUTH_CODE_INVALID:
                    case Constants.AUTH_CODE_TIME:
                        CleanBasketApplication.getInstance().showToast(getString(R.string.authorization_code_wrong));
                        showProgress(false);
                        break;

                    case Constants.ACCOUNT_DUPLICATION:
                        CleanBasketApplication.getInstance().showToast(getString(R.string.email_duplication));
                        showProgress(false);
                        break;

                    case Constants.DUPLICATION:
                        CleanBasketApplication.getInstance().showToast(getString(R.string.phone_duplication));
                        showProgress(false);
                        break;

                    case Constants.SUCCESS:
                        CleanBasketApplication.getInstance().showToast(getString(R.string.sign_up_success));
                        showProgress(false);
                        getUserInfo();
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mButtonRequestCode.setEnabled(true);

                try {
                    getActivity().unregisterReceiver(smsBroadCastReceiver);
                } catch (IllegalArgumentException e) {

                }

                CleanBasketApplication.getInstance().showToast(getString(R.string.general_error));
                showProgress(false);
            }
        });
        RequestQueue.getInstance(getActivity()).addToRequestQueue(postRequest.doRequest());
    }

    private boolean isEmailValid(String email) {
        Pattern pattern = Pattern.compile("\\w+[@]\\w+\\.\\w+");
        Matcher match = pattern.matcher(email);

        return match.find();
    }

    private void requestAuthorizationCode() {
        if (TextUtils.isEmpty(mEditTextPhone.getText().toString()))
            return;

        if (mEditTextPhone.getText().length() != 11)
            return;

        if (!mButtonRequestCode.isEnabled())
            return;

        mButtonRequestCode.setEnabled(false);
        mButtonRequestCode.setBackgroundResource(R.drawable.textview_back_grey);
        mButtonRequestCode.setTextColor(getResources().getColor(R.color.text_black));

        PostRequest postRequest = new PostRequest(getActivity());
        postRequest.setUrl(AddressManager.AUTH_CODE);
        postRequest.setParams("phone", mEditTextPhone.getText().toString());
        postRequest.setListener(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JsonData jsonData = null;

                try {
                    jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);
                } catch (JsonSyntaxException e) {
                    return;
                }

                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        CleanBasketApplication.getInstance().showToast(getString(R.string.authorization_code_sent));
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mButtonRequestCode.setEnabled(true);
                mButtonRequestCode.setBackgroundResource(R.drawable.button_green);
                mButtonRequestCode.setTextColor(getResources().getColor(R.color.text_white));

                CleanBasketApplication.getInstance().showToast(getString(R.string.general_error));
            }
        });
        RequestQueue.getInstance(getActivity()).addToRequestQueue(postRequest.doRequest());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();

        switch (position) {
            case 1:
                CouponDialog cd = CouponDialog.newInstance(new CouponDialog.OnCouponSetListener() {
                    @Override
                    public void onCouponSet(CouponDialog dialog, Coupon coupon) {

                    }
                });

                cd.show(getActivity().getSupportFragmentManager(), COUPON_VIEW_DIALOG_TAG);
                break;

            case 2:
                intent.setAction("com.bridge4biz.laundry.ui.NoticeActivity");
                startActivity(intent);
                break;

            case 3:
                intent.setAction("com.bridge4biz.laundry.ui.SettingActivity");
                startActivityForResult(intent, SETTING);
                break;

            case 4:
                intent.setAction("com.bridge4biz.laundry.ui.WebViewActivity");
                intent.putExtra("type", WebViewActivity.SERVICE_INFO);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case SettingActivity.LOG_OUT:
                getActivity().finish();

                Intent intent = new Intent();
                intent.setAction("com.bridge4biz.laundry.ui.LoginActivity");
                startActivity(intent);
                break;
        }
    }

    protected class MenuItem {
        String name;
        String img;

        public MenuItem(String name, String img) {
            this.name = name;
            this.img = img;
        }
    }

    protected class MenuAdapter extends ArrayAdapter<MenuItem> {
        private LayoutInflater mLayoutInflater;

        MenuAdapter(Context context, int resource, List<MenuItem> objects) {
            super(context, resource, objects);

            this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MenuViewHolder holder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_menu, parent, false);
                holder = new MenuViewHolder();
                holder.imageViewMenu = (ImageView) convertView.findViewById(R.id.imageview_item_menu);
                holder.textViewMenu = (TextView) convertView.findViewById(R.id.textview_item_menu);
                convertView.setTag(holder);
            } else
                holder = (MenuViewHolder) convertView.getTag();

            holder.imageViewMenu.setBackgroundResource(getDrawableByString(getItem(position).img));
            holder.textViewMenu.setText(getItem(position).name);

            return convertView;
        }

        private int getDrawableByString(String img) {
            return getContext().getResources().getIdentifier("ic_information_" + img, "drawable", getContext().getPackageName());
        }

        protected class MenuViewHolder {
            public ImageView imageViewMenu;
            public TextView textViewMenu;
        }
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

//            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
//            mListView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mListView.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });

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

    private BroadcastReceiver smsBroadCastReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                        StringBuilder sms = new StringBuilder();
                        Bundle bundle = intent.getExtras();

                        if (bundle != null) {
                            Object[] pdusObj = (Object[]) bundle.get("pdus");

                            if (pdusObj == null) return;

                            SmsMessage[] messages = new SmsMessage[pdusObj.length];
                            for (int i = 0; i < pdusObj.length; i++) {
                                messages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                            }

                            for (SmsMessage smsMessage : messages) {
                                sms.append(smsMessage.getMessageBody());
                            }

                            mEditTextAuthorization.setText(getCode(sms.toString()));

                            mButtonRequestCode.setEnabled(true);
                            mButtonRequestCode.setBackgroundResource(R.drawable.button_green);
                            mButtonRequestCode.setTextColor(getResources().getColor(R.color.text_white));
                        }
                    }
                }
            };

    private String getCode(String sms) {
        Pattern pattern = Pattern.compile("\\d{4}");
        Matcher match = pattern.matcher(sms);

        String code = "";

        if (match.find())
            code = match.group();

        return code;
    }
}