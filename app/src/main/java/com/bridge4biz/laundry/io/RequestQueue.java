package com.bridge4biz.laundry.io;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.bridge4biz.laundry.io.cache.LruBitmapCache;


public class RequestQueue {
    public static final String TAG = RequestQueue.class
            .getSimpleName();

    private com.android.volley.RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private static Context mContext;
    private static RequestQueue mInstance;

    public RequestQueue(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized RequestQueue getInstance(Context context) {
        if(mInstance == null) {
            mContext = context;
            mInstance = new RequestQueue(context);
        }

        return mInstance;
    }

    public com.android.volley.RequestQueue getRequestQueue() { return mRequestQueue; }

    public ImageLoader getImageLoader() {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue, new LruBitmapCache());
        }

        return this.mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
