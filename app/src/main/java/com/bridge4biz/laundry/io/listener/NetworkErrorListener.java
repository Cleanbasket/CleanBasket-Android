package com.bridge4biz.laundry.io.listener;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.R;

public class NetworkErrorListener implements Response.ErrorListener {
    private Context context;

    public NetworkErrorListener(Context context) {
        this.context = context;
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        try {
            Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
            Log.e("Error", volleyError.getMessage().toString());
        } catch(NullPointerException e) {
            Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show();
            Log.e("Error", e.toString());
        }
    }
}
