package com.washappkorea.corp.cleanbasket.ui;


import android.os.Bundle;
import android.webkit.WebView;

import com.washappkorea.corp.cleanbasket.Config;
import com.washappkorea.corp.cleanbasket.R;

public class WebViewActivity extends BaseActivity {
    private static final String TAG = WebViewActivity.class.getSimpleName();

    public static final int TERM_OF_USE = 0;
    public static final int PRIVACY = 1;
    public static final int SERVICE_INFO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        Bundle data = getIntent().getExtras();
        int type = data.getInt("type");

        WebView webView = (WebView) findViewById(R.id.webview);

        switch (type) {
            case TERM_OF_USE:
                webView.loadUrl(Config.SERVER_ADDRESS + "term-of-use");
                break;
            case PRIVACY:
                webView.loadUrl(Config.SERVER_ADDRESS + "privacy");
                break;
            case SERVICE_INFO:
                webView.loadUrl(Config.SERVER_ADDRESS + "service-info");
                break;
        }
    }
}
