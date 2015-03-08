package com.washappkorea.corp.cleanbasket.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.Header;

import java.util.Map;

public class SessionManager {
    private static final String TAG = SessionManager.class.getSimpleName();

    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String SPRING_SECURITY = "SPRING_SECURITY_REMEMBER_ME_COOKIE";
    private static final String COOKIE_KEY = "Cookie";
    private static Context mContext;

    private static SessionManager mInstance;

    public static SessionManager get(Context context) {
        if(mInstance == null)
            mInstance = new SessionManager();

        mContext = context;

        return mInstance;
    }

    /**
     * Checks the response headers for session cookie and saves it
     * if it finds it.
     * @param headers Response Headers.
     */
    public final void checkSessionCookie(Header[] headers) {
        Log.i(TAG, "Start");

        for (int i = 0; i < headers.length; i++) {
            String key = headers[i].getName();
            String value = headers[i].getValue();

            if(key.equals(SET_COOKIE_KEY)) {
                String[] sessionId = value.split("=");
                String name = sessionId[0];
                String cookie = sessionId[1];

                storeSessionIdPreference(name, cookie);
            }
        }
    }

    /**
     * Adds session cookie to headers if exists.
     * @param headers
     */
    public final void addSessionCookie(Map<String, String> headers) {
        String springId = getSessionId(SPRING_SECURITY);

        if (springId.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(SPRING_SECURITY);
            builder.append("=");
            builder.append(springId);

            if (headers.containsKey(COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(COOKIE_KEY));
            }

            Log.i(TAG, "쿠기가 추가되었습니다. " + builder.toString());

            headers.put(COOKIE_KEY, builder.toString());
        }
    }

    private void storeSessionIdPreference(String sessionId, String value) {
        SharedPreferences prefs = mContext.getSharedPreferences("session", mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(sessionId, value);
        editor.commit();
    }

    private String getSessionId(String sessionId) {
        SharedPreferences prefs = mContext.getSharedPreferences("session", mContext.MODE_PRIVATE);
        return prefs.getString(sessionId, "");
    }
}