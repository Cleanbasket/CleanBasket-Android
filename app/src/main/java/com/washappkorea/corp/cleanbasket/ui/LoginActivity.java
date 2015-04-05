package com.washappkorea.corp.cleanbasket.ui;


import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.CirclePageIndicator;
import com.washappkorea.corp.cleanbasket.R;

public class LoginActivity extends BaseActivity {
    private ViewPager mViewPager;
    private LoginPagerAdapter mLoginPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mViewPager = (ViewPager) findViewById(R.id.pager);

        final ActionBar bar = getActionBar();

        mLoginPagerAdapter = new LoginPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mLoginPagerAdapter);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        mViewPager.setCurrentItem(position);
                    }
                });

        CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.page_indicator);
        circlePageIndicator.setViewPager(mViewPager);
    }
}
