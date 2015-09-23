package com.bridge4biz.laundry.ui;


import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.bridge4biz.laundry.R;
import com.kakao.auth.AuthType;
import com.kakao.auth.Session;
import com.kakao.auth.SessionCallback;
import com.kakao.util.exception.KakaoException;
import com.viewpagerindicator.CirclePageIndicator;

public class LoginActivity extends BaseActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private final SessionCallback mySessionCallback = new MySessionStatusCallback();

    public Session session;
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

        // 세션 콜백 추가
        Session.initialize(this, AuthType.KAKAO_TALK);
        session = Session.getCurrentSession();
        session.addCallback(mySessionCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data))
            return;
    }

    private class MySessionStatusCallback implements SessionCallback {
        @Override
        public void onSessionOpened() {
            Log.i(TAG, "onSessionOpened()");
            Intent intent = new Intent();
            intent.setAction("com.bridge4biz.laundry.ui.KakaoRegisterActivity");
            startActivity(intent);
            finish();
        }

        @Override
        public void onSessionClosed(final KakaoException exception) {
            Log.i(TAG, "onSessionClosed()");
        }

        @Override
        public void onSessionOpening() {
            Log.i(TAG, "onSessionOpening()");
        }
    }
}
