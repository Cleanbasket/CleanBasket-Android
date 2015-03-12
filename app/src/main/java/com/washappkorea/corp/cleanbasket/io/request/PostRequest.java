package com.washappkorea.corp.cleanbasket.io.request;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.washappkorea.corp.cleanbasket.Config;
import com.washappkorea.corp.cleanbasket.util.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PostRequest {
    private Response.Listener<JSONObject> mListener;
    private Response.ErrorListener mErrListener;

    private String mTargetUrl;
    private Uri.Builder mUrl;

    private JSONObject mObj = new JSONObject();

    private Context mContext;

    public PostRequest(Context mContext) {
        this.mContext = mContext;
    }

    public void setUrl(String url) {
        mUrl = Uri.parse(Config.SERVER_ADDRESS).buildUpon();    // 빌더 선언
        mUrl.appendEncodedPath(url);
    }

    public void setListener(Response.Listener<JSONObject> listener, Response.ErrorListener errListener) {
        this.mListener = listener;
        this.mErrListener = errListener;
    }

    public void setParams(JSONObject jsonObject) {
        mObj = jsonObject;
        Log.i("PostRequest", mObj.toString());
    }

    public void setParams(String key, String value) {
        try {
            mObj.put(key, value);
        } catch (JSONException e) {
            Log.e("Err", e.toString());
        }
    }

    public void setParams(String key, int value) {
        try {
            mObj.put(key, value);
        } catch (JSONException e) {
            Log.e("Err", e.toString());
        }
    }

    public void setParams(String key, Boolean value) {
        try {
            mObj.put(key, value);
        } catch (JSONException e) {
            Log.e("Err", e.toString());
        }
    }

    public JsonObjectRequest doRequest() {
        mTargetUrl = mUrl.build().toString();

        Log.i("Uri", mTargetUrl);

        JsonObjectRequest postRequest = new JsonObjectRequest(mTargetUrl, mObj, mListener, mErrListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();

                if (headers == null || headers.equals(Collections.emptyMap())) {
                    headers = new HashMap<String, String>();
                }

                SessionManager.get(mContext).addSessionCookie(headers);

                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");

                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                SessionManager.get(mContext).checkSessionCookie(response.apacheHeaders);

                return super.parseNetworkResponse(response);
            }

            @Override
            public byte[] getBody() {
                return super.getBody();
            }
        };

        return postRequest;
    }
}
