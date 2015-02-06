package com.washappkorea.corp.cleanbasket.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.washappkorea.corp.cleanbasket.R;

public class OrderListFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_orderlist, container, false);

        return rootView;
    }
}