package com.washappkorea.corp.cleanbasket.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.washappkorea.corp.cleanbasket.R;

public class LoginFragment extends Fragment {
    public static final String ARG_OBJECT = LoginFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        Bundle args = getArguments();

        switch (args.getInt(ARG_OBJECT)) {
            case 1:
                break;
            case 2:
                break;
        }

        return rootView;
    }
}
