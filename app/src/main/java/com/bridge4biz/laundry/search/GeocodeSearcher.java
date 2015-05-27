package com.bridge4biz.laundry.search;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.map.GeocodeResponse;
import com.bridge4biz.laundry.io.request.GetRawRequest;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

public class GeocodeSearcher {
	public static final String TAG = GeocodeSearcher.class.getSimpleName();

	public static final String GOOGLE_MAP_GEOCODER = "http://maps.google.com/maps/api/geocode/json?address=%s&sensor=true&launguage=%s";
	
	OnFinishGeocodeSearchListener onFinishGeocodeSearchListener;

	public void searchGeocode(Context context, String query, OnFinishGeocodeSearchListener onFinishGeocodeSearchListener) {
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
    	return String.format(GOOGLE_MAP_GEOCODER, encodedQuery, language);
    }

    private void fetchData(Context context, String urlString) {
        GetRawRequest getRequest = new GetRawRequest(context);
        getRequest.setUrl(urlString);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GeocodeResponse geocodeResponse = null;

                try {
                    geocodeResponse = CleanBasketApplication.getInstance().getGson().fromJson(response, GeocodeResponse.class);
                } catch (JsonSyntaxException e) {
                    onFinishGeocodeSearchListener.onFail();
                }

                if (geocodeResponse != null)
                    onFinishGeocodeSearchListener.onSuccess(geocodeResponse);
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
