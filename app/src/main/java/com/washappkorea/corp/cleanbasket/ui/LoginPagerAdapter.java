package com.washappkorea.corp.cleanbasket.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
* Created by ganghan-yong on 2014. 5. 3..
*/

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class LoginPagerAdapter extends FragmentStatePagerAdapter {
    public LoginPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putInt(LoginFragment.ARG_OBJECT, i + 1);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
