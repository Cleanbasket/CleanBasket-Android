package com.bridge4biz.laundry.ui;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.model.Notice;
import com.bridge4biz.laundry.io.request.GetRequest;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;
import com.bridge4biz.laundry.util.DateTimeFactory;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class NoticeActivity extends BaseActivity {
    private static final String TAG = NoticeActivity.class.getSimpleName();

    private ExpandableListView mExpandableListView;
    private NoticeAdapter mNoticeAdapter;

    private View mProgressView;

    private int previousChild = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notice);

        mExpandableListView = (ExpandableListView) findViewById(R.id.listview_notice);
        mProgressView = findViewById(R.id.loading_progress);

        mExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (previousChild == groupPosition) {

                } else if (previousChild >= 0) {
                    mExpandableListView.collapseGroup(previousChild);
                }

                previousChild = groupPosition;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        getLatestNotice();
    }

    private void openGroupByValue() {
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("value")) {
            int value = getIntent().getExtras().getInt("value");

            for (int i = 0; i < mNoticeAdapter.getGroupCount(); i++) {
                int noid = ((Notice) mExpandableListView.getAdapter().getItem(i)).noid;
                if (value == noid) mExpandableListView.expandGroup(i);
            }
        }
    }

    private void getLatestNotice() {
        showProgress(true);
        GetRequest getRequest = new GetRequest(this);
        getRequest.setUrl(AddressManager.GET_NOTICE);
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
                        ArrayList<Notice> notices = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, new TypeToken<ArrayList<Notice>>(){}.getType());
                        insertNotice(notices);
                        showProgress(false);
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);
            }
        });
        RequestQueue.getInstance(this).addToRequestQueue(getRequest.doRequest());
    }

    private void insertNotice(ArrayList<Notice> notices) {
        mNoticeAdapter = new NoticeAdapter(notices);
        mExpandableListView.setAdapter(mNoticeAdapter);

        if (notices.size() > 0)
            mExpandableListView.expandGroup(0);

        openGroupByValue();
    }

    protected class NoticeAdapter extends BaseExpandableListAdapter {
        private ArrayList<Notice> notices;
        private LayoutInflater mLayoutInflater;

        public NoticeAdapter(ArrayList<Notice> Notices) {
            this.notices = Notices;
            this.mLayoutInflater = getLayoutInflater();
        }

        @Override
        public int getGroupCount() {
            return notices.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Notice getGroup(int groupPosition) {
            return notices.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return notices.get(groupPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int position, boolean isExpanded, View convertView, ViewGroup parent) {
            NoticeHolder holder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_notice, parent, false);
                holder = new NoticeHolder();
                holder.textViewTitle = (TextView) convertView.findViewById(R.id.textview_notice_title);
                holder.textViewDateTime = (TextView) convertView.findViewById(R.id.textview_notice_datetime);
                convertView.setTag(holder);
            } else
                holder = (NoticeHolder) convertView.getTag();

            holder.textViewTitle.setText(getGroup(position).title);
            holder.textViewDateTime.setText(DateTimeFactory.getInstance().getPrettyTime(getGroup(position).rdate));

            return convertView;
        }

        @Override
        public View getChildView(int position, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            NoticeContentHolder holder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.custom_notice_child, parent, false);
                holder = new NoticeContentHolder();
                holder.textViewContent = (TextView) convertView.findViewById(R.id.textivew_notice_content);
                convertView.setTag(holder);
            } else
                holder = (NoticeContentHolder) convertView.getTag();

            holder.textViewContent.setText(getGroup(position).content);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        protected class NoticeHolder {
            public TextView textViewTitle;
            public TextView textViewDateTime;
        }

        protected class NoticeContentHolder {
            public TextView textViewContent;
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

            mExpandableListView.setVisibility(show ? View.GONE : View.VISIBLE);
            mExpandableListView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mExpandableListView.setVisibility(show ? View.GONE : View.VISIBLE);
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
