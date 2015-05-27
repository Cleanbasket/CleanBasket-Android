package com.bridge4biz.laundry.ui;


import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;

public class WebViewActivity extends BaseActivity {
    private static final String TAG = WebViewActivity.class.getSimpleName();

    public static final int TERM_OF_USE = 0;
    public static final int PRIVACY = 1;
    public static final int SERVICE_INFO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

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
        customTitle.setText(getString(R.string.app_name));

        Bundle data = getIntent().getExtras();
        int type = data.getInt("type");

        WebView webView = (WebView) findViewById(R.id.webview);

        switch (type) {
            case TERM_OF_USE:
                getActionBar().setTitle(R.string.info_service);
                webView.loadUrl(Config.SERVER_ADDRESS + "term-of-use");
                break;
            case PRIVACY:
                getActionBar().setTitle(R.string.info_protection);
                webView.loadUrl(Config.SERVER_ADDRESS + "privacy");
                break;
            case SERVICE_INFO:
                getActionBar().setTitle(R.string.service_info);
                webView.loadUrl(Config.SERVER_ADDRESS + "service-info");
                break;
        }
    }
}
