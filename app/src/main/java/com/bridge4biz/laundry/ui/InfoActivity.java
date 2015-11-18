package com.bridge4biz.laundry.ui;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.listener.NetworkErrorListener;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.model.OrderItemInfo;
import com.bridge4biz.laundry.io.request.GetRequest;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;

public class InfoActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = InfoActivity.class.getSimpleName();

    private ImageView mProductImageView;
    private TextView mProductTextView;
    private TextView mProductContentTextView;
    private Button mFinishButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.75f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.activity_info);
        mProductImageView = (ImageView) findViewById(R.id.imageview_product);
        mProductTextView = (TextView) findViewById(R.id.textview_header);
        mProductContentTextView = (TextView) findViewById(R.id.textview_content);
        mProductContentTextView = (TextView) findViewById(R.id.textview_content);
        mFinishButton = (Button) findViewById(R.id.button_okay);

        mFinishButton.setOnClickListener(this);

        String type = getIntent().getStringExtra("type");

        if (type.equals("item")) {
            int popId = getIntent().getIntExtra("popId", -1);

            if (popId < 0) {
                finish();
                CleanBasketApplication.getInstance().showToast(getString(R.string.general_error));
            } else {
                getOrderItemInfo(popId);
            }
        }
        else if (type.equals("card")) {
            fillItemContent(new OrderItemInfo(
                    getString(R.string.card_info_title),
                    getText(R.string.card_info).toString(),
                    "images/info/card.jpg"
            ));
        }
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    private void getOrderItemInfo(int popId) {
        GetRequest getRequest = new GetRequest(this);
        getRequest.setUrl(AddressManager.GET_ITEM_INFO + popId);
        getRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonData jsonData = null;

                try {
                    jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response, JsonData.class);
                } catch (JsonSyntaxException e) {
                    return;
                }

                switch (jsonData.constant) {
                    case Constants.SUCCESS:
                        try {
                            OrderItemInfo orderItemInfo = CleanBasketApplication.getInstance().getGson().fromJson(jsonData.data, OrderItemInfo.class);

                            fillItemContent(orderItemInfo);

                        } catch (JsonSyntaxException e) {
                            Log.e(TAG, e.toString());
                        } catch (NullPointerException e) {
                            Log.e(TAG, e.toString());
                        }

                        break;
                }
            }
        }, new NetworkErrorListener(this));
        RequestQueue.getInstance(this).addToRequestQueue(getRequest.doRequest());
    }
    
    public void fillItemContent(OrderItemInfo orderItemInfo) {
        Picasso.with(this).load(Config.IMAGE_SERVER + orderItemInfo.getImg()).into(mProductImageView);

        mProductTextView.setText(orderItemInfo.getTitle());
        mProductContentTextView.setText(Html.fromHtml(orderItemInfo.getContent()));
    }
}
