package com.washappkorea.corp.cleanbasket.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.washappkorea.corp.cleanbasket.R;

public class NotificationFragment extends Fragment {
    ListView mNotificationListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);

        mNotificationListView = (ListView) rootView.findViewById(R.id.listview_notification);

        return rootView;
    }
}