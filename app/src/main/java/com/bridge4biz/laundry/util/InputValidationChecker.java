package com.bridge4biz.laundry.util;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidationChecker {
	Context mContext;

	private static InputValidationChecker mInputValidationChecker;

	private InputValidationChecker(Context context) {
		mContext = context;
	}

	public static synchronized InputValidationChecker getInstance(Context context) {
		if (mInputValidationChecker == null) {
			mInputValidationChecker = new InputValidationChecker(context);
		}

		return mInputValidationChecker;
	}

    public boolean isEmailValid(String email) {
        Pattern pattern = Pattern.compile("\\w+[@]\\w+\\.\\w+");
        Matcher match = pattern.matcher(email);

        return match.find();
    }

    public boolean isPasswordValid(String password) {
        Pattern patternAlpha = Pattern.compile("^[\\p{Alpha}[^\\p{Digit}]]*$");
        Matcher matcherAlpha = patternAlpha.matcher(password);

        Pattern patternDigit = Pattern.compile("^[\\p{Digit}[^\\p{Alpha}]]*$");
        Matcher matcherDigit = patternDigit.matcher(password);

        if (!(!matcherAlpha.matches() && !matcherDigit.matches())) {
            return false;
        }

        return true;
    }

    public boolean isPasswordShort(String password) {
        if (password.length() < 6 || password.length() > 24) {
            return false;
        }

        return true;
    }
}
