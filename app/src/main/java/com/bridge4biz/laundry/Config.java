package com.bridge4biz.laundry;


public class Config {
    public final static String DAUM_MAP_API = "ed518de9a2cb643cc334a8f3fa8008fb";
    public final static String DAUM_MAP_LOCAL_API = "ed518de9a2cb643cc334a8f3fa8008fb";
    public final static String ZOPIM_API = "3IvIR4PxJLUypCdpgbBmJLBjYby32CVD";

    public final static String IMAGE_SERVER = "https://www.cleanbasket.co.kr/";
    public final static String IMAGE_SERVER_ITEM = "https://www.cleanbasket.co.kr/images/item/ic_item_";

//    public final static String SERVER_ADDRESS = "https://www.cleanbasket.co.kr/";
//    public final static String SERVER_ADDRESS = "http://192.168.11.4:8080/wash/";
//    public final static String SERVER_ADDRESS = "http://211.110.140.180:8080/";
//    public final static String SERVER_ADDRESS = "http://10.7.22.67:8080/wash/";
    public final static String SERVER_ADDRESS = "http://192.168.58.1:8080/wash/";

    public final static String SEOUL_IMAGE_ADDRESS = "http://www.cleanbasket.co.kr/images/seoul.png";
    public final static String INCHEON_IMAGE_ADDRESS = "http://www.cleanbasket.co.kr/images/incheon.png";
    public final static String NICE_PAYMENT_ADDRESS = "https://pg.nicepay.co.kr/issue/IssueLoader.jsp?TID=";
    public final static String NICE_PAYMENT_ADDRESS_SUFFIX = "&type=0";

    public final static String PLAY_STORE_URL = "market://details?id=com.bridge4biz.laundry";

    public static final String PACKAGE_NAME = "com.bridge4biz.laundry";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static final String URQA_API_Key = "CB78C771";

    public static final double CLASS_CLEAN_BASKET = 0.01;
    public static final double CLASS_SILVER_BASKET = 0.02;
    public static final double CLASS_GOLD_BASKET = 0.03;
    public static final double CLASS_LOVE_BASKET = 0.04;

    public static final int DEFAULT_MAX_RETRIES = 0;
    public static final float DEFAULT_BACKOFF_MULT = 1.0f;
}
