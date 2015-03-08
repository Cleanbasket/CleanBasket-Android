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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.google.gson.JsonSyntaxException;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.listener.NetworkErrorListener;
import com.washappkorea.corp.cleanbasket.io.model.Coupon;
import com.washappkorea.corp.cleanbasket.io.model.JsonData;
import com.washappkorea.corp.cleanbasket.io.request.GetRequest;
import com.washappkorea.corp.cleanbasket.ui.dialog.CouponDialog;
import com.washappkorea.corp.cleanbasket.util.AddressManager;
import com.washappkorea.corp.cleanbasket.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment implements ListView.OnItemClickListener {
    private static final String TAG = UserFragment.class.getSimpleName();

    public static final String COUPON_VIEW_DIALOG_TAG = "COUPON_VIEW_DIALOG";

    private View mProgressView;
    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);

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

//        getUserInfo();
    }

    private void getUserInfo() {
        showProgress(true);
        GetRequest getRequest = new GetRequest(getActivity());
        getRequest.setUrl(AddressManager.GET_MEMBER_INFO);
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
                        break;
                }
            }
        }, new NetworkErrorListener(getActivity()));
        RequestQueue.getInstance(getActivity()).addToRequestQueue(getRequest.doRequest());
    }

    private void insertUserInfo(ArrayList<MenuItem> menuItems) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();

        switch (position) {
            case 0:
                CouponDialog cd = CouponDialog.newInstance(new CouponDialog.OnCouponSetListener() {
                    @Override
                    public void onCouponSet(CouponDialog dialog, Coupon coupon) {

                    }
                });

                cd.show(getActivity().getSupportFragmentManager(), COUPON_VIEW_DIALOG_TAG);
                break;

            case 1:
                intent.setAction("com.washappkorea.corp.cleanbasket.ui.NoticeActivity");
                startActivity(intent);
                break;

            case 3:
                intent.setAction("com.washappkorea.corp.cleanbasket.ui.WebViewActivity");
                intent.putExtra("type", WebViewActivity.SERVICE_INFO);
                startActivity(intent);
                break;
        }
    }

    private void startActivity(String intentInfo) {

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