package com.washappkorea.corp.cleanbasket.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.washappkorea.corp.cleanbasket.Config;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.search.Item;
import com.washappkorea.corp.cleanbasket.search.OnFinishSearchListener;
import com.washappkorea.corp.cleanbasket.search.Searcher;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import java.util.List;

public class MapActivity extends BaseActivity implements MapView.MapViewEventListener, MapView.CurrentLocationEventListener, OnFinishSearchListener, MapReverseGeoCoder.ReverseGeoCodingResultListener, View.OnClickListener {
    private static final String TAG = MapActivity.class.getSimpleName();

    private EditText mEditTextSearchAddress;
    private ImageView mImageviewAddressSearch;
    private TextView mTextViewCurrentAddress;
    private MapView mMapView;
    private Button mAcceptButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        getActionBar().setTitle(R.string.map_search_title);

        mEditTextSearchAddress = (EditText) findViewById(R.id.edittext_address_search);
        mImageviewAddressSearch = (ImageView) findViewById(R.id.imageview_address_search);
        mTextViewCurrentAddress = (TextView) findViewById(R.id.textview_current_address);
        mMapView = (MapView) findViewById(R.id.map_view);
        mAcceptButton = (Button) findViewById(R.id.button_accept_address);

        mMapView.setDaumMapApiKey(Config.DAUM_MAP_API);
        mMapView.setMapViewEventListener(this);
        mMapView.setCurrentLocationEventListener(this);

        mImageviewAddressSearch.setOnClickListener(this);
        mAcceptButton.setOnClickListener(this);
    }

    /**
     * 현재 위치로 이동합니다
     */
    private void moveToCurrentLocation(MapView mapView) {
        Bundle bundle = getIntent().getExtras();

        if (bundle != null &&
            bundle.containsKey("latitude") &&
            bundle.containsKey("longitude")) {
            double latitude = bundle.getDouble("latitude");
            double longitude = bundle.getDouble("longitude");

            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude);
            mapView.setMapCenterPoint(mapPoint, true);
            findAddress(mapPoint);
        }
        else
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
    }

    /**
     * 맵 포인트를 출력
     * @param mapPoint
     */
    private void findAddress(MapPoint mapPoint) {
        MapReverseGeoCoder reverseGeoCoder = new MapReverseGeoCoder(Config.DAUM_MAP_LOCAL_API, mapPoint, this, this);
        reverseGeoCoder.startFindingAddress();
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        moveToCurrentLocation(mapView);
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
        findAddress(mapPoint);
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        Log.i(TAG, "Current Location Update");
        findAddress(mapPoint);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        mTextViewCurrentAddress.setText(s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageview_address_search:
                mEditTextSearchAddress.clearFocus();
                String query = mEditTextSearchAddress.getText().toString();

                if (query == null || query.length() == 0) {
                    showToast(getString(R.string.search_no_text));
                    return;
                }

                hideSoftKeyboard();
                MapPoint.GeoCoordinate geoCoordinate = mMapView.getMapCenterPoint().getMapPointGeoCoord();
                double latitude = geoCoordinate.latitude;
                double longitude = geoCoordinate.longitude;
                int radius = 10000;
                int page = 1;
                String apikey = Config.DAUM_MAP_API;

                Searcher searcher = new Searcher();
                searcher.searchKeyword(getApplicationContext(), query, latitude, longitude, radius, page, apikey, this);
                break;

            case R.id.button_accept_address:
                Intent intent = new Intent();
                intent.putExtra("address", mTextViewCurrentAddress.getText().toString());
                setResult(OrderInfoFragment.ADDRESS_RESULT, intent);
                finish();
        }
    }

    private void showResult(List<Item> itemList) {
        Item i;

        if(itemList.size() > 0) {
            i = itemList.get(0);

            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(i.latitude, i.longitude);
            mMapView.setMapCenterPoint(mapPoint, true);
            findAddress(mapPoint);
        }
    }

    @Override
    public void onSuccess(List<Item> itemList) {
        showResult(itemList);
    }

    @Override
    public void onFail() {

    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MapActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditTextSearchAddress.getWindowToken(), 0);
    }
}
