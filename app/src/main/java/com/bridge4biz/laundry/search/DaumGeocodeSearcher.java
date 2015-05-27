package com.bridge4biz.laundry.search;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.map.ResponseElement;
import com.bridge4biz.laundry.io.request.GetRawRequest;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

public class DaumGeocodeSearcher {
	public static final String TAG = DaumGeocodeSearcher.class.getSimpleName();

    public static final String DAUM_MAP_ADDR = "https://apis.daum.net/local/geo/addr2coord?apikey=%s&q=%s&output=json";
	
	OnFinishDaumGeocodeSearchListener onFinishGeocodeSearchListener;

	public void searchGeocode(Context context, String query, OnFinishDaumGeocodeSearchListener onFinishGeocodeSearchListener) {
    	this.onFinishGeocodeSearchListener = onFinishGeocodeSearchListener;

		String url = buildGeocodeSearchApiUrlString(query);
		fetchData(context, url);
    }

	private String buildGeocodeSearchApiUrlString(String query) {
    	String encodedQuery = "";
        String language = Locale.getDefault().getLanguage();

        try {
			encodedQuery = URLEncoder.encode(query, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	return String.format(DAUM_MAP_ADDR, Config.DAUM_MAP_LOCAL_API, encodedQuery);
    }

    private void fetchData(Context context, String urlString) {
        GetRawRequest getRequest = new GetRawRequest(context);
        getRequest.setUrl(urlString);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ResponseElement responseElement = null;

                try {
                    responseElement = CleanBasketApplication.getInstance().getGson().fromJson(response, ResponseElement.class);
                } catch (JsonSyntaxException e) {
                    onFinishGeocodeSearchListener.onFail();
                }

                if (responseElement != null)
                    onFinishGeocodeSearchListener.onSuccess(responseElement);
                else
                    onFinishGeocodeSearchListener.onFail();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onFinishGeocodeSearchListener.onFail();
            }
        });
        RequestQueue.getInstance(context).addToRequestQueue(getRequest.doRequest());
    }
}
