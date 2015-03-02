package com.washappkorea.corp.cleanbasket.ui;


import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.washappkorea.corp.cleanbasket.R;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ViewPager mViewPager;
    private MainTabsAdapter mTabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.pager);

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        bar.setDisplayShowHomeEnabled(false);
        bar.setDisplayShowTitleEnabled(false);

        mTabsAdapter = new MainTabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.ic_order), OrderFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.ic_orderlist), OrderListFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.ic_info), InfoFragment.class, null);
        mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.ic_alarm), AlarmFragment.class, null);

        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
    }
}
