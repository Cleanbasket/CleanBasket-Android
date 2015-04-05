package com.washappkorea.corp.cleanbasket;


public class Config {
    public final static String DAUM_MAP_API = "2d7bf0d4ff008afd21fa87672de24ea6e1c9ede7";
    public final static String DAUM_MAP_LOCAL_API = "75fd758f8d2afe314b00006129092860400a529c";

//    public final static String SERVER_ADDRESS = "http://www.cleanbasket.co.kr/";
    public final static String SERVER_ADDRESS = "http://192.168.11.2:8080/wash/";

    public final static String SEOUL_IMAGE_ADDRESS = "http://www.cleanbasket.co.kr/images/seoul.png";
    public final static String INCHEON_IMAGE_ADDRESS = "http://www.cleanbasket.co.kr/images/incheon.png";

    public final static String PLAY_STORE_URL = "market://details?id=com.bridge4biz.laundry";

    public static final String PACKAGE_NAME = "com.washappkorea.corp.cleanbasket";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
}
