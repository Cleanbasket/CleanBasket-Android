package com.bridge4biz.laundry.ui;


import android.os.Bundle;

import com.bridge4biz.laundry.R;

public class PhoneActivity extends BaseActivity {
    private static final String TAG = PhoneActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
    }
}
