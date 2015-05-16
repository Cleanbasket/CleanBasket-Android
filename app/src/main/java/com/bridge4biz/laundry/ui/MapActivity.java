package com.bridge4biz.laundry.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.model.District;
import com.bridge4biz.laundry.io.model.map.AddressComponent;
import com.bridge4biz.laundry.io.model.map.GeocodeResponse;
import com.bridge4biz.laundry.search.AddressSearcher;
import com.bridge4biz.laundry.search.GeocodeSearcher;
import com.bridge4biz.laundry.search.OnFinishAddrSearchListener;
import com.bridge4biz.laundry.search.OnFinishGeocodeSearchListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Locale;

public class MapActivity extends BaseActivity implements GoogleMap.OnCameraChangeListener, OnMapReadyCallback, OnFinishAddrSearchListener, View.OnClickListener {
    private static final String TAG = MapActivity.class.getSimpleName();
    private static final String SUBLOCALITY_LV1 = "sublocality_level_1";
    private static final String SUBLOCALITY_LV2 = "sublocality_level_2";
    private static final String LOCALITY = "locality";

    private EditText mEditTextSearchAddress;
    private ImageView mImageviewAddressSearch;
    private TextView mTextViewCurrentAddress;
    private MapView mMapView;
    private Button mAcceptButton;

    private GoogleMap map;
    private AddressSearcher addressSearcher;
    private GeocodeSearcher geocodeSearcher;

    private ArrayList<String> mDistricts = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        getActionBar().setTitle(R.string.map_search_title);

        mEditTextSearchAddress = (EditText) findViewById(R.id.edittext_address_search);
        mImageviewAddressSearch = (ImageView) findViewById(R.id.imageview_address_search);
        mTextViewCurrentAddress = (TextView) findViewById(R.id.textview_current_address);
        mAcceptButton = (Button) findViewById(R.id.button_accept_address);

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
            mDistricts.add(district.city + " " + district.district + " " + district.dong);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        this.map.setOnCameraChangeListener(this);
        moveToCurrentLocation(map);
    }

    private boolean isAvailableDistrict(String address) {
        if (mDistricts.contains(address))
            return true;
        else
            return false;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        LatLng current = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);

        findAddress(current);
    }

    /**
     * 현재 위치로 이동합니다
     */
    private void moveToCurrentLocation(GoogleMap map) {
        Bundle bundle = getIntent().getExtras();

        if (bundle != null &&
            bundle.containsKey("latitude") &&
            bundle.containsKey("longitude")) {
            double latitude = bundle.getDouble("latitude");
            double longitude = bundle.getDouble("longitude");

            LatLng current = new LatLng(latitude, longitude);
            map.setMyLocationEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 13));

            findAddress(current);
        }
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

        String[] fullAddress = address.split(" ");

        if (fullAddress.length < 4)
            return "";

        String formattedAddress = "";

        if (Locale.getDefault().getLanguage().equals("ko") || Locale.getDefault().equals("kr"))
            formattedAddress = fullAddress[1] + " " + fullAddress[2] + " " + fullAddress[3];
        else
            formattedAddress = fullAddress[0] + " " + fullAddress[1] + " " + fullAddress[2];

        return formattedAddress;
    }

    @Override
    public void onSuccess(GeocodeResponse geocodeResponse) {
        String address = "";

        if (geocodeResponse.getResults().size() == 0)
            return;
        else
            address = parseAddress(geocodeResponse);

        Log.i(TAG, address);

        mTextViewCurrentAddress.setText(address);

        if (isAvailableDistrict(address))
            mTextViewCurrentAddress.setBackgroundResource(R.color.point_color);
        else
            mTextViewCurrentAddress.setBackgroundResource(android.R.color.holo_red_light);

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

//    @Override
//    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
//        Log.i(TAG, "Current Location Update");
//        findAddress(mapPoint);
//        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
//    }
//
//    @Override
//    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
//
//    }
//
//    @Override
//    public void onCurrentLocationUpdateFailed(MapView mapView) {
//
//    }
//
//    @Override
//    public void onCurrentLocationUpdateCancelled(MapView mapView) {
//
//    }
//
//    @Override
//    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {

//    }
//
//    @Override
//    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
//
//    }
//
//    private void search() {
//        mEditTextSearchAddress.clearFocus();
//        String query = mEditTextSearchAddress.getText().toString();
//
//        if (query == null || query.length() == 0) {
//            showToast(getString(R.string.search_no_text));
//            return;
//        }
//
//        hideSoftKeyboard();
//        MapPoint.GeoCoordinate geoCoordinate = mMapView.getMapCenterPoint().getMapPointGeoCoord();
//        double latitude = geoCoordinate.latitude;
//        double longitude = geoCoordinate.longitude;
//        int radius = 10000;
//        int page = 1;
//        String apikey = Config.DAUM_MAP_API;
//
//        Searcher searcher = new Searcher();
//        searcher.searchKeyword(getApplicationContext(), query, latitude, longitude, radius, page, apikey, this);
//    }
//

//
//    private void showResult(List<Item> itemList) {
//        Item i;
//
//        if(itemList.size() > 0) {
//            i = itemList.get(0);
//
//            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(i.latitude, i.longitude);
//            mMapView.setMapCenterPoint(mapPoint, true);
//            findAddress(mapPoint);
//        }
//    }
//
//    @Override
//    public void onSuccess(List<Item> itemList) {
//        showResult(itemList);
//    }
//
//    @Override
//    public void onFail() {
//
//    }
//
//    private void showToast(final String text) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(MapActivity.this, text, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//


