package com.washappkorea.corp.cleanbasket.util;

import com.google.gson.Gson;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;

import org.apache.http.entity.ByteArrayEntity;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class RequestEntity extends HashMap<String, Object> {
	private static final long serialVersionUID = -3639394754277679752L;
	private Gson mGson;

	public RequestEntity() {
        mGson = CleanBasketApplication.getInstance().getGson();
	}

	public ByteArrayEntity getEntity() {
		try {
			ByteArrayEntity entity = new ByteArrayEntity(mGson.toJson(this, HashMap.class).getBytes("UTF-8"));
			return entity;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ByteArrayEntity getEntity(Object o) {
		try {
			ByteArrayEntity entity = new ByteArrayEntity(mGson.toJson(o).getBytes("UTF-8"));
			return entity;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		super.toString();
		return mGson.toJson(this, HashMap.class);
	}

}
