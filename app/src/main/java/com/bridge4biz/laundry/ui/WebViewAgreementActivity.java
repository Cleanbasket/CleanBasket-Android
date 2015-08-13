package com.bridge4biz.laundry.ui;


import android.app.ActionBar;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;

public class WebViewAgreementActivity extends BaseActivity {
    private static final String TAG = WebViewAgreementActivity.class.getSimpleName();

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

        Uri data = getIntent().getData();
        String type = data.getHost();

        WebView webView = (WebView) findViewById(R.id.webview);

        if(type.equals("privacy")) {
            webView.loadUrl(Config.SERVER_ADDRESS + "payment-term-of-use");
        }
    }
}
