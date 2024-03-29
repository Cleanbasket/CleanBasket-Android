package com.bridge4biz.laundry.ui;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.model.Coupon;
import com.bridge4biz.laundry.io.model.Notification;
import com.bridge4biz.laundry.ui.dialog.CouponDialog;
import com.bridge4biz.laundry.ui.dialog.MessageDialog;
import com.bridge4biz.laundry.ui.dialog.ModifyDateTimeDialog;
import com.bridge4biz.laundry.util.DateTimeFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = NotificationActivity.class.getSimpleName();
    
    private ListView mNotificationListView;
    private TextView mTextViewEmpty;
    public NotificationAdapter mNotificationAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mNotificationListView = (ListView) findViewById(R.id.listview_notification);
        mTextViewEmpty = (TextView) findViewById(R.id.textview_no_item);

        ArrayList<Notification> notifications = new ArrayList<Notification>();
        mNotificationAdapter = new NotificationAdapter(this, R.layout.item_notice, notifications, getLayoutInflater());
        mNotificationListView.setAdapter(mNotificationAdapter);
        mNotificationListView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getDataFromDB();
    }

    private void getDataFromDB() {
        int uid = CleanBasketApplication.getInstance().getUid();

        if (uid == 0) return;

        ArrayList<Notification> notifications;

        notifications = (ArrayList<Notification>) getDBHelper().getNotificationDao().queryForEq(Notification.UID, uid);

        Collections.sort(notifications, new Comparator<Notification>() {
            @Override
            public int compare(Notification lhs, Notification rhs) {
                return rhs.nid - lhs.nid;
            }
        });

        if (mNotificationAdapter != null) {
            mNotificationAdapter.clear();
            mNotificationAdapter.addAll(notifications);
            mTextViewEmpty.setVisibility(View.GONE);
        }

        if (notifications.size() == 0) {
            mTextViewEmpty.setVisibility(View.VISIBLE);
        }
    }

    protected class NotificationAdapter extends ArrayAdapter<Notification> {
        private LayoutInflater mLayoutInflater;

        NotificationAdapter(Context context, int resource, List<Notification> objects, LayoutInflater mLayoutInflater) {
            super(context, resource, objects);

            this.mLayoutInflater = mLayoutInflater;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NotificationViewHolder holder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_notification, parent, false);
                holder = new NotificationViewHolder();
                holder.layoutNotification = (RelativeLayout) convertView.findViewById(R.id.layout_notification);
                holder.imageViewNotification = (ImageView) convertView.findViewById(R.id.imageview_notification_icon);
                holder.textViewNotificationTitle = (TextView) convertView.findViewById(R.id.textview_notification_title);
                holder.textViewNotificationDate = (TextView) convertView.findViewById(R.id.textview_notification_date);
                convertView.setTag(holder);
            } else
                holder = (NotificationViewHolder) convertView.getTag();

            if (getItem(position).check)
                holder.layoutNotification.setBackgroundColor(getResources().getColor(R.color.notification_checked));
            else
                holder.layoutNotification.setBackgroundColor(getResources().getColor(R.color.notification_unchecked));
            holder.imageViewNotification.setImageResource(getDrawableByType(getItem(position).type));
            holder.textViewNotificationTitle.setText(getItem(position).title);
            holder.textViewNotificationDate.setText(DateTimeFactory.getInstance().getPrettyTime(getItem(position).date));

            return convertView;
        }

        protected class NotificationViewHolder {
            public RelativeLayout layoutNotification;
            public ImageView imageViewNotification;
            public TextView textViewNotificationTitle;
            public TextView textViewNotificationDate;
        }
    }

    private int getDrawableByType(int type) {
        switch (type) {
            case Notification.EVENT_ALARM:
                return R.drawable.ic_alarm_event;
            case Notification.MESSAGE_ALARM:
                return R.drawable.ic_alarm_message;
            case Notification.PICKUP_ALARM:
                return R.drawable.ic_alarm_pickup;
            case Notification.DROPOFF_ALARM:
                return R.drawable.ic_alarm_delivery;
            case Notification.COUPON_ALARM:
                return R.drawable.ic_alarm_message;
            default:
                return R.drawable.ic_alarm_message;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Notification notification = null;

        if (mNotificationAdapter != null)
            notification = mNotificationAdapter.getItem(position);

        Intent intent;

        switch (notification.type) {
            case Notification.EVENT_ALARM:
                intent = new Intent();
                intent.setAction("com.bridge4biz.laundry.ui.NoticeActivity");
                intent.putExtra("value", mNotificationAdapter.getItem(position).value);
                startActivity(intent);
                break;
            case Notification.MESSAGE_ALARM:
                MessageDialog md = MessageDialog.newInstance(null, mNotificationAdapter.getItem(position).message);

                md.show(this.getSupportFragmentManager(), notification.message);
                break;
            case Notification.PICKUP_ALARM:
                ModifyDateTimeDialog pickUpModifyDateTimeDialog = ModifyDateTimeDialog.newInstance(null, notification.oid);

                pickUpModifyDateTimeDialog.show(this.getSupportFragmentManager(), OrderStatusFragment.MODIFY_DIALOG_TAG);
                break;
            case Notification.DROPOFF_ALARM:
                ModifyDateTimeDialog dropOffModifyDateTimeDialog = ModifyDateTimeDialog.newInstance(null, notification.oid);

                dropOffModifyDateTimeDialog.show(this.getSupportFragmentManager(), OrderStatusFragment.MODIFY_DIALOG_TAG);
                break;
            case Notification.COUPON_ALARM:
                CouponDialog cd = CouponDialog.newInstance(new CouponDialog.OnCouponSetListener() {
                    @Override
                    public void onCouponSet(CouponDialog dialog, Coupon coupon) {

                    }
                });

                cd.show(this.getSupportFragmentManager(), UserFragment.COUPON_VIEW_DIALOG_TAG);
                break;
            case Notification.FEEDBACK_ALARM:
                intent = new Intent();
                intent.setAction("com.bridge4biz.laundry.ui.FeedbackActivity");
                intent.putExtra("oid", mNotificationAdapter.getItem(position).oid);
                startActivity(intent);
                break;
            case Notification.PAYMENT_ALARM:
            case Notification.PAYMENT_CANCEL_ALARM:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.NICE_PAYMENT_ADDRESS + mNotificationAdapter.getItem(position).message + Config.NICE_PAYMENT_ADDRESS_SUFFIX));
                startActivity(intent);
                break;
        }

        notification.check = true;
        mNotificationAdapter.notifyDataSetChanged();
        getDBHelper().getNotificationDao().createOrUpdate(notification);
    }
}
