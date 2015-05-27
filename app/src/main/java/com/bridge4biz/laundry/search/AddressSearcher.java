package com.bridge4biz.laundry.search;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.map.GeocodeResponse;
import com.bridge4biz.laundry.io.request.GetRawRequest;
import com.google.gson.JsonSyntaxException;

import java.util.Locale;

public class AddressSearcher {
	public static final String TAG = AddressSearcher.class.getSimpleName();

	public static final String GOOGLE_MAP_ADDR = "http://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&sensor=true&language=%s";

	OnFinishAddrSearchListener onFinishAddrSearchListener;

	public void searchAddr(Context context, double latitude, double longitude, OnFinishAddrSearchListener onFinishAddrSearchListener) {
    	this.onFinishAddrSearchListener = onFinishAddrSearchListener;

		String url = buildAddrSearchApiUrlString(latitude, longitude);
		fetchData(context, url);
    }

	private String buildAddrSearchApiUrlString(double latitude, double longitude) {
        String language = Locale.getDefault().getLanguage();

    	return String.format(GOOGLE_MAP_ADDR, latitude, longitude, language);
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
                    onFinishAddrSearchListener.onFail();
                }

                if (geocodeResponse != null)
                    onFinishAddrSearchListener.onSuccess(geocodeResponse);
                else
                    onFinishAddrSearchListener.onFail();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onFinishAddrSearchListener.onFail();
            }
        });
        RequestQueue.getInstance(context).addToRequestQueue(getRequest.doRequest());
	}
}
