package com.washappkorea.corp.cleanbasket.ui;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonSyntaxException;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.model.AuthUser;
import com.washappkorea.corp.cleanbasket.io.model.Coupon;
import com.washappkorea.corp.cleanbasket.io.model.JsonData;
import com.washappkorea.corp.cleanbasket.io.request.GetRequest;
import com.washappkorea.corp.cleanbasket.ui.dialog.CouponDialog;
import com.washappkorea.corp.cleanbasket.util.AddressManager;
import com.washappkorea.corp.cleanbasket.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment implements ListView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = UserFragment.class.getSimpleName();

    public static final String COUPON_VIEW_DIALOG_TAG = "COUPON_VIEW_DIALOG";
    public static final Integer SETTING = 0;

    public static final int BRONZE = 0;
    public static final int SILVER = 1;
    public static final int GOLD = 2;
    public static final int LOVE = 3;

    private View mRegisterView;
    private View mUserInfoView;

    private ImageView mImageViewUserClass;
    private TextView mUserName;
    private TextView mUserClass;
    private TextView mUserClassInfo;
    private TextView mUserClassMileage;
    private Button mButtonClassInfo;

    private View mProgressView;
    private ListView mListView;

    private Button mButtonRegister;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);

        mRegisterView = inflater.inflate(R.layout.custom_user_register, null);

        mButtonRegister = (Button) mRegisterView.findViewById(R.id.button_register);

        mUserInfoView = inflater.inflate(R.layout.custom_user_info, null);

        mImageViewUserClass = (ImageView) mRegisterView.findViewById(R.id.imageview_user_class);
        mUserName = (TextView) mRegisterView.findViewById(R.id.textview_user_name);
        mUserClass = (TextView) mRegisterView.findViewById(R.id.textview_user_class);
        mUserClassInfo = (TextView) mRegisterView.findViewById(R.id.textview_user_class_info);
        mUserClassMileage = (TextView) mRegisterView.findViewById(R.id.textview_user_class_mileage);
        mButtonClassInfo = (Button) mRegisterView.findViewById(R.id.button_view_class_info);

        mProgressView = rootView.findViewById(R.id.loading_progress);
        mListView = (ListView) rootView.findViewById(R.id.listview_menu);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
        menuItems.add(new MenuItem(getString(R.string.coupon), "coupon"));
        menuItems.add(new MenuItem(getString(R.string.notification), "notification"));
        menuItems.add(new MenuItem(getString(R.string.setting), "setting"));
        menuItems.add(new MenuItem(getString(R.string.service_info), "service_info"));

        MenuAdapter menuAdapter = new MenuAdapter(getActivity(), R.layout.item_menu, menuItems);
        mListView.setAdapter(menuAdapter);
        mListView.setOnItemClickListener(this);

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

                        if (jsonData.data != null)
                            showRegisterHeader();
                        else {
                            AuthUser authUser = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, AuthUser.class);
                            showUserInfoHeader(authUser);
                        }
                        break;
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
        mImageViewUserClass.setImageResource(R.drawable.ic_sale);
        mUserName.setText(authUser.email);
        mUserClass.setText(getClassName(authUser.user_class));
        mUserClassInfo.setText(getClassDetail(authUser.user_class));
        mUserClassMileage.setText(getString(R.string.mileage_available) + authUser.mileage + getString(R.string.mileage_available));
        mButtonClassInfo.setOnClickListener(this);

        mListView.addHeaderView(mUserInfoView);
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

        return getString(R.string.bronze_basket);
    }

    private void showRegisterHeader() {
        mListView.addHeaderView(mRegisterView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_view_class_info:
                break;
        }
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
                intent.setAction("com.washappkorea.corp.cleanbasket.ui.NoticeActivity");
                startActivity(intent);
                break;

            case 3:
                intent.setAction("com.washappkorea.corp.cleanbasket.ui.SettingActivity");
                startActivityForResult(intent, SETTING);
                break;

            case 4:
                intent.setAction("com.washappkorea.corp.cleanbasket.ui.WebViewActivity");
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
                intent.setAction("com.washappkorea.corp.cleanbasket.ui.LoginActivity");
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

            holder.imageViewMenu.setBackgroundResource(CleanBasketApplication.getInstance().getDrawableByString(getItem(position).img));
            holder.textViewMenu.setText(getItem(position).name);

            return convertView;
        }

        protected class MenuViewHolder {
            public ImageView imageViewMenu;
            public TextView textViewMenu;
        }
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

            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
            mListView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mListView.setVisibility(show ? View.GONE : View.VISIBLE);
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