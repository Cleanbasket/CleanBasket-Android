package com.bridge4biz.laundry.util;

import android.content.Context;

import com.bridge4biz.laundry.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressChecker {
	Context mContext;

	private static AddressChecker mAddressChecker;

	private AddressChecker(Context context) {
		mContext = context;
	}

	public static synchronized AddressChecker getInstance(Context context) {
		if (mAddressChecker == null) {
			mAddressChecker = new AddressChecker(context);
		}

		return mAddressChecker;
	}

    public boolean isAddressValid(String address) {
        Pattern pattern = Pattern.compile(getString(R.string.regex_address_dong) + "|" + getString(R.string.regex_address_ro));
        Matcher match = pattern.matcher(address);

        return match.find();
    }

    public String getValidAddress(String address) {
        Pattern pattern = Pattern.compile(getString(R.string.regex_address_dong) + "|" + getString(R.string.regex_address_ro));
        Matcher match = pattern.matcher(address);

        if (match.find())
            return match.group();
        else
            return "";
    }

    private String getString(int resource) {
        return mContext.getResources().getString(resource);
    }
}
