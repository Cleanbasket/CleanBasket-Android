package com.bridge4biz.laundry.search;

import com.bridge4biz.laundry.io.model.map.GeocodeResponse;

public interface OnFinishAddrSearchListener {
	public void onSuccess(GeocodeResponse geocodeResponse);
	public void onFail();
}
