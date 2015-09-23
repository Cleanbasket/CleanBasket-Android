package com.bridge4biz.laundry.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bridge4biz.laundry.R;
import com.zopim.android.sdk.prechat.ZopimChatActivity;

public class NotificationFragment extends Fragment implements View.OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);

        Button buttonStartChat = (Button) rootView.findViewById(R.id.button_start_chat);
        buttonStartChat.setOnClickListener(this);

        TextView textView = (TextView) rootView.findViewById(R.id.textview_call_customer);
        SpannableString content = new SpannableString(getString(R.string.call_customer_center));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        textView.setText(content);
        textView.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start_chat:
                ZopimChatActivity.startActivity(getActivity(), null);
                break;

            case R.id.textview_call_customer:
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + getString(R.string.number_customer_center)));
                startActivity(callIntent);
                break;
        }
    }
}
