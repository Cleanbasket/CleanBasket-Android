package com.washappkorea.corp.cleanbasket.ui;


import android.os.Bundle;

import com.washappkorea.corp.cleanbasket.R;

public class PhoneActivity extends BaseActivity {
    private static final String TAG = PhoneActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
    }
}
