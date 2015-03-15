package com.washappkorea.corp.cleanbasket.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.model.Coupon;
import com.washappkorea.corp.cleanbasket.io.model.Notification;
import com.washappkorea.corp.cleanbasket.ui.dialog.CouponDialog;
import com.washappkorea.corp.cleanbasket.ui.dialog.MessageDialog;
import com.washappkorea.corp.cleanbasket.ui.dialog.ModifyDateTimeDialog;
import com.washappkorea.corp.cleanbasket.util.DateTimeFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationFragment extends Fragment implements ListView.OnItemClickListener {
    private ListView mNotificationListView;
    public NotificationAdapter mNotificationAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);

        mNotificationListView = (ListView) rootView.findViewById(R.id.listview_notification);
        ArrayList<Notification> notifications = new ArrayList<Notification>();

        try {
            notifications = (ArrayList<Notification>) ((MainActivity) getActivity()).getDBHelper().getNotificationDao().queryBuilder().query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mNotificationAdapter = new NotificationAdapter(getActivity(), R.layout.item_notice, notifications, inflater);
        mNotificationListView.setAdapter(mNotificationAdapter);
        mNotificationListView.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        getDataFromDB();
    }

    private void getDataFromDB() {
        ArrayList<Notification> notifications;

        notifications = (ArrayList<Notification>) ((MainActivity) getActivity()).getDBHelper().getNotificationDao().queryForAll();

        Collections.sort(notifications, new Comparator<Notification>() {
            @Override
            public int compare(Notification lhs, Notification rhs) {
                return rhs.nid - lhs.nid;
            }
        });

        if (mNotificationAdapter != null) {
            mNotificationAdapter.clear();
            mNotificationAdapter.addAll(notifications);
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
                return R.drawable.calendar_bg_selector;
            case Notification.MESSAGE_ALARM:
                return R.drawable.calendar_bg_selector;
            case Notification.PICKUP_ALARM:
                return R.drawable.calendar_bg_selector;
            case Notification.DROPOFF_ALARM:
                return R.drawable.calendar_bg_selector;
            case Notification.COUPON_ALARM:
                return R.drawable.calendar_bg_selector;
            default:
                return R.drawable.calendar_bg_selector;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Notification notification = null;

        if (mNotificationAdapter != null)
            notification = mNotificationAdapter.getItem(position);

        switch (notification.type) {
            case Notification.EVENT_ALARM:
                Intent intent = new Intent();
                intent.setAction("com.washappkorea.corp.cleanbasket.ui.NoticeActivity");
                intent.putExtra("value", mNotificationAdapter.getItem(position).value);
                startActivity(intent);
                break;
            case Notification.MESSAGE_ALARM:
                MessageDialog md = MessageDialog.newInstance(null, mNotificationAdapter.getItem(position).message);

                md.show(getActivity().getSupportFragmentManager(), notification.message);
                break;
            case Notification.PICKUP_ALARM:
                ModifyDateTimeDialog pickUpModifyDateTimeDialog = ModifyDateTimeDialog.newInstance(null, notification.oid);

                pickUpModifyDateTimeDialog.show(getActivity().getSupportFragmentManager(), OrderStatusFragment.MODIFY_DIALOG_TAG);
                break;
            case Notification.DROPOFF_ALARM:
                ModifyDateTimeDialog dropOffModifyDateTimeDialog = ModifyDateTimeDialog.newInstance(null, notification.oid);

                dropOffModifyDateTimeDialog.show(getActivity().getSupportFragmentManager(), OrderStatusFragment.MODIFY_DIALOG_TAG);
                break;
            case Notification.COUPON_ALARM:
                CouponDialog cd = CouponDialog.newInstance(new CouponDialog.OnCouponSetListener() {
                    @Override
                    public void onCouponSet(CouponDialog dialog, Coupon coupon) {

                    }
                });

                cd.show(getActivity().getSupportFragmentManager(), UserFragment.COUPON_VIEW_DIALOG_TAG);
                break;
        }

        notification.check = true;
        mNotificationAdapter.notifyDataSetChanged();
        ((MainActivity) getActivity()).getDBHelper().getNotificationDao().createOrUpdate(notification);
    }
}