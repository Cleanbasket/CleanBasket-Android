package com.bridge4biz.laundry.search;

import com.bridge4biz.laundry.io.model.map.ResponseElement;

public interface OnFinishDaumGeocodeSearchListener {
	public void onSuccess(ResponseElement responseElement);
	public void onFail();
}
