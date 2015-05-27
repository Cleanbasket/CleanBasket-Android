package com.bridge4biz.laundry.ui;


import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.model.District;
import com.bridge4biz.laundry.io.model.map.AddressComponent;
import com.bridge4biz.laundry.io.model.map.GeocodeResponse;
import com.bridge4biz.laundry.io.model.map.ResponseElement;
import com.bridge4biz.laundry.search.AddressSearcher;
import com.bridge4biz.laundry.search.DaumGeocodeSearcher;
import com.bridge4biz.laundry.search.GeocodeSearcher;
import com.bridge4biz.laundry.search.OnFinishAddrSearchListener;
import com.bridge4biz.laundry.search.OnFinishDaumGeocodeSearchListener;
import com.bridge4biz.laundry.search.OnFinishGeocodeSearchListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;

import java.util.ArrayList;
import java.util.Locale;

public class MapActivity extends BaseActivity implements MapReverseGeoCoder.ReverseGeoCodingResultListener, GoogleMap.OnCameraChangeListener, OnMapReadyCallback, OnFinishAddrSearchListener, View.OnClickListener {
    private static final String TAG = MapActivity.class.getSimpleName();
    private static final String SUBLOCALITY_LV1 = "sublocality_level_1";
    private static final String SUBLOCALITY_LV2 = "sublocality_level_2";
    private static final String LOCALITY = "locality";
    public static final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";

    private EditText mEditTextSearchAddress;
    private ImageView mImageviewAddressSearch;
    private TextView mTextViewCurrentAddress;
    private TextView mTextViewAddressStatus;
    private RelativeLayout mAcceptButton;

    private GoogleMap map;
    private AddressSearcher addressSearcher;
    private GeocodeSearcher geocodeSearcher;

    private double mLastLatitude;
    private double mLastLongitude;

    private ArrayList<String> mDistricts = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(R.layout.action_layout);
        TextView customTitle = (TextView) getActionBar().getCustomView().findViewById(R.id.actionbar_title);
        ImageView backButton = (ImageView) getActionBar().getCustomView().findViewById(R.id.imageview_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        customTitle.setText(getString(R.string.map_search_title));

        mEditTextSearchAddress = (EditText) findViewById(R.id.edittext_address_search);
        mImageviewAddressSearch = (ImageView) findViewById(R.id.imageview_address_search);
        mTextViewCurrentAddress = (TextView) findViewById(R.id.textview_current_address);
        mTextViewAddressStatus = (TextView) findViewById(R.id.textview_address_status);
        mAcceptButton = (RelativeLayout) findViewById(R.id.button_accept_address);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);

        mImageviewAddressSearch.setOnClickListener(this);
        mAcceptButton.setOnClickListener(this);

        mEditTextSearchAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.use || id == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                    return true;
                }

                return false;
            }
        });

        ArrayList<District> districts = (ArrayList<District>) CleanBasketApplication.mInstance.getDBHelper().getDistrictDao().queryForAll();

        for (District district : districts) {
            if (TextUtils.isEmpty(district.dong))
                mDistricts.add(district.city + " " + district.district);
            else
                mDistricts.add(district.city + " " + district.district + " " + district.dong);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        this.map.setOnCameraChangeListener(this);
        moveToCurrentLocation();
    }

    private boolean isAvailableDistrict(String address) {
        String[] fullAddress;

        if (Locale.getDefault().getLanguage().equals("ko") || Locale.getDefault().equals("kr")) {
            fullAddress = address.split(" ");
            if (fullAddress.length < 3) return false;

            if (mDistricts.contains(fullAddress[0] + " " + fullAddress[1]))
                return true;
            else if (mDistricts.contains(fullAddress[0] + " " + fullAddress[1] + " " + fullAddress[2]))
                return true;
            else
                return false;
        }
        else {
            fullAddress = address.split(", ");
            if (fullAddress.length < 3) return false;

            if (mDistricts.contains(fullAddress[2] + " " + fullAddress[1]))
                return true;
            else if (mDistricts.contains(fullAddress[2] + " " + fullAddress[1] + " " + fullAddress[0]))
                return true;
            else
                return false;
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        LatLng current = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);

        findAddress(current);
    }

    /**
     * 현재 위치로 이동합니다
     */
    private void moveToCurrentLocation() {
        Bundle bundle = getIntent().getExtras();

        double latitude = 37.4999072;
        double longitude = 127.0373932;

        if (bundle != null &&
            bundle.containsKey("latitude") &&
            bundle.containsKey("longitude")) {
            latitude = bundle.getDouble("latitude");
            longitude = bundle.getDouble("longitude");
        }

        LatLng current = new LatLng(latitude, longitude);
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 13));

        findAddress(current);
    }

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        setAddress(s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {

    }

    private void moveToLocation(LatLng latLng) {
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

        findAddress(latLng);
    }

    /**
     * 맵 포인트를 출력
     * @param current
     */
    private void findAddress(LatLng current) {
        if (addressSearcher == null)
            addressSearcher = new AddressSearcher();

        mLastLatitude = current.latitude;
        mLastLongitude = current.longitude;

        addressSearcher.searchAddr(this, current.latitude, current.longitude, this);
    }

    private String parseAddress(GeocodeResponse geocodeResponse) {
        String address = "";

        for (int i = 0; i < geocodeResponse.getResults().size(); i++) {
            ArrayList<AddressComponent> tempAddresses = (ArrayList<AddressComponent>) geocodeResponse.getResults().get(i).getAddress_components();

            for (int j = 0; j < tempAddresses.size(); j++) {
                AddressComponent addressComponent = tempAddresses.get(j);
                if (addressComponent.getTypes().contains(SUBLOCALITY_LV2) && addressComponent.getLongName().endsWith(getString(R.string.dong))) {
                    address = geocodeResponse.getResults().get(i).getFormatted_address();
                }
            }
        }

        String[] fullAddress;

        String formattedAddress = "";

        if (Locale.getDefault().getLanguage().equals("ko") || Locale.getDefault().equals("kr")) {
            fullAddress = address.split(" ");
            if (fullAddress.length < 4) return "";
            formattedAddress = fullAddress[1] + " " + fullAddress[2] + " " + fullAddress[3];
        }
        else {
            fullAddress = address.split(", ");
            if (fullAddress.length < 4) return "";
            formattedAddress = fullAddress[0] + ", " + fullAddress[1] + ", " + fullAddress[2];
        }

        return formattedAddress;
    }

    @Override
    public void onSuccess(GeocodeResponse geocodeResponse) {
        String address = "";

        if (geocodeResponse.getStatus().equals(OVER_QUERY_LIMIT)) {
            Log.i(TAG, OVER_QUERY_LIMIT);

            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(mLastLatitude, mLastLongitude);
            MapReverseGeoCoder reverseGeoCoder = new MapReverseGeoCoder(Config.DAUM_MAP_API, mapPoint, this, this);
            reverseGeoCoder.startFindingAddress();

            return;
        }

        if (geocodeResponse.getResults().size() == 0)
            return;
        else
            address = parseAddress(geocodeResponse);

        setAddress(address);
    }

    private void setAddress(String address) {
        mTextViewCurrentAddress.setText(address);

        if (isAvailableDistrict(address)) {
            mTextViewCurrentAddress.setBackgroundResource(R.color.point_color);
            mTextViewAddressStatus.setText(getResources().getString(R.string.area_available));
        }
        else {
            mTextViewCurrentAddress.setBackgroundResource(R.color.address_textview_bacground);
            mTextViewAddressStatus.setText(getResources().getString(R.string.area_unavailable_error));
        }

        mTextViewCurrentAddress.setText(address);
    }

    @Override
    public void onFail() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_address_search:
                search();
                break;

            case R.id.button_accept_address:
                if (TextUtils.isEmpty(mTextViewCurrentAddress.getText().toString()))
                    return;

                if (!isAvailableDistrict(mTextViewCurrentAddress.getText().toString())) {
                    CleanBasketApplication.getInstance().showToast(getString(R.string.area_unavailable_error));
                    return;
                }

                Intent intent = new Intent();
                intent.putExtra("address", mTextViewCurrentAddress.getText().toString());
                setResult(OrderInfoFragment.ADDRESS_RESULT, intent);
                finish();
        }
    }

    private void daumSearch() {
        mEditTextSearchAddress.clearFocus();
        String query = mEditTextSearchAddress.getText().toString();

        DaumGeocodeSearcher daumGeocodeSearcher = new DaumGeocodeSearcher();
        daumGeocodeSearcher.searchGeocode(this, query, new OnFinishDaumGeocodeSearchListener() {
            @Override
            public void onSuccess(ResponseElement responseElement) {
                Log.i(TAG, responseElement.toString());
            }

            @Override
            public void onFail() {

            }
        });
    }

    private void search() {
        mEditTextSearchAddress.clearFocus();
        String query = mEditTextSearchAddress.getText().toString();

        if (query == null || query.length() == 0) {
            CleanBasketApplication.getInstance().showToast(getString(R.string.search_no_text));
            return;
        }

        hideSoftKeyboard();

        if (geocodeSearcher == null)
            geocodeSearcher = new GeocodeSearcher();

        geocodeSearcher.searchGeocode(this, query, new OnFinishGeocodeSearchListener() {
            @Override
            public void onSuccess(GeocodeResponse geocodeResponse) {
                if (geocodeResponse.getStatus().equals(OVER_QUERY_LIMIT)) {

                }

                if (geocodeResponse.getResults().size() == 0)
                    return;

                double latitude = geocodeResponse.getResults().get(0).getGeometry().getLocation().getLat();
                double longitude = geocodeResponse.getResults().get(0).getGeometry().getLocation().getLng();

                LatLng current = new LatLng(latitude, longitude);

                moveToLocation(current);
            }

            @Override
            public void onFail() {

            }
        });
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditTextSearchAddress.getWindowToken(), 0);
    }
}