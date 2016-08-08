package com.bridge4biz.laundry.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bridge4biz.laundry.R;
import com.squareup.picasso.Picasso;

public class MarketingPopupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketing_popup);

        ImageView popUpImage = (ImageView) findViewById(R.id.popUpImage);
        ImageView cancel = (ImageView) findViewById(R.id.cancel);

        Picasso.with(this).load("http://1.255.56.182/images/popup.png").into(popUpImage);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
