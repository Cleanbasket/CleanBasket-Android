package com.bridge4biz.laundry.io.request;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.util.SessionManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StringRequest {
    private Response.Listener<String> mListener;
    private Response.ErrorListener mErrListener;

    private String mTargetUrl;
    private Uri.Builder mUrl;

    private Map<String, String> mRequestEntity = new HashMap<String, String>();

    private Context mContext;

    public StringRequest(Context mContext) {
        this.mContext = mContext;
    }

    public void setUrl(String url) {
        mUrl = Uri.parse(Config.SERVER_ADDRESS).buildUpon();    // 빌더 선언
        mUrl.appendEncodedPath(url);
    }

    public void setListener(Response.Listener<String> listener, Response.ErrorListener errListener) {
        this.mListener = listener;
        this.mErrListener = errListener;
    }

    public void setParams(String key, String value) {
        this.mRequestEntity.put(key, value);
    }

    public com.android.volley.toolbox.StringRequest doRequest() {
        mTargetUrl = mUrl.build().toString();

        Log.i("Uri", mTargetUrl);

        com.android.volley.toolbox.StringRequest getRequest = new com.android.volley.toolbox.StringRequest(Request.Method.POST, mTargetUrl, mListener, mErrListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();

                if (headers == null || headers.equals(Collections.emptyMap())) {
                    headers = new HashMap<String, String>();
                }

                SessionManager.get(mContext).addSessionCookie(headers);

                headers.put("Content-Type", "application/x-www-form-urlencoded");

                return headers;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                SessionManager.get(mContext).checkSessionCookie(response.apacheHeaders);

                return super.parseNetworkResponse(response);
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return mRequestEntity;
            }
        };

        return getRequest;
    }
}
