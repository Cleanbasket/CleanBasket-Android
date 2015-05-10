package com.bridge4biz.laundry.io.request;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.util.SessionManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GetRequest {
    private Response.Listener<String> mListener;
    private Response.ErrorListener mErrListener;

    private String mTargetUrl;
    private Uri.Builder mUrl;

    private Context mContext;

    public GetRequest(Context mContext) {
        this.mContext = mContext;
    }

    public void setUrl(String url) {
        mUrl = Uri.parse(Config.SERVER_ADDRESS).buildUpon();    // 빌더 선언
        mUrl.appendEncodedPath(url);
    }

    public void setParams(String key, int value) {
        mUrl.appendQueryParameter(key, String.valueOf(value));
    }

    public void setListener(Response.Listener<String> listener, Response.ErrorListener errListener) {
        this.mListener = listener;
        this.mErrListener = errListener;
    }

    public StringRequest doRequest() {
        mTargetUrl = mUrl.build().toString();

        Log.i("Uri", mTargetUrl);

        StringRequest getRequest = new StringRequest(Request.Method.GET, mTargetUrl, mListener, mErrListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();

                if (headers == null || headers.equals(Collections.emptyMap())) {
                    headers = new HashMap<String, String>();
                }

                SessionManager.get(mContext).addSessionCookie(headers);

                return headers;
            }
        };

        return getRequest;
    }
}
