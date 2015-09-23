package com.bridge4biz.laundry.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.bridge4biz.laundry.Config;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.ui.dialog.EmailDialog;
import com.kakao.auth.AuthType;

import java.util.Locale;

public class LoginFragment extends Fragment {
    public static final String ARG_OBJECT = LoginFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = null;
        Bundle args = getArguments();

        switch (args.getInt(ARG_OBJECT)) {
            case 1:
                rootView = inflater.inflate(R.layout.fragment_service_info, container, false);
                break;

            case 2:
                rootView = inflater.inflate(R.layout.fragment_service_map, container, false);

                ImageView mSeoulMap = (ImageView) rootView.findViewById(R.id.imageview_seoul_map);
                ImageView mIncheonMap = (ImageView) rootView.findViewById(R.id.imageview_incheon_map);

                ImageLoader imageLoader = RequestQueue.getInstance(getActivity()).getImageLoader();
                imageLoader.get(Config.SEOUL_IMAGE_ADDRESS,
                        ImageLoader.getImageListener(
                                mSeoulMap, R.drawable.ic_login_seoul_map, R.drawable.ic_login_seoul_map
                        ));
                imageLoader.get(Config.INCHEON_IMAGE_ADDRESS,
                        ImageLoader.getImageListener(
                                mIncheonMap, R.drawable.ic_login_incheon_map, R.drawable.ic_login_incheon_map
                        ));
                break;

            case 3:
                rootView = inflater.inflate(R.layout.fragment_service_login, container, false);

                RelativeLayout mKakaoLogin = (RelativeLayout) rootView.findViewById(R.id.layout_button_kakao);
                RelativeLayout mEmailLogin = (RelativeLayout) rootView.findViewById(R.id.layout_button_email);
                TextView mTextView = (TextView) rootView.findViewById(R.id.textview_agreement);

                mKakaoLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((LoginActivity) getActivity()).session.getCurrentSession().open(AuthType.KAKAO_TALK, getActivity());
                    }
                });

                mEmailLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EmailDialog ed = EmailDialog.newInstance(new EmailDialog.OnLoginSuccess() {
                            @Override
                            public void onLoginSuccess() {
                                getActivity().finish();
                            }
                        });
                        ed.show(getActivity().getSupportFragmentManager(), "ed");
                    }
                });

                // 동의 텍스트 생성
                StringBuilder html = new StringBuilder();

                if (Locale.getDefault().getLanguage().equals("ko") || Locale.getDefault().equals("kr")) {
                    html.append(getString(R.string.login_agreement_a));
                    html.append(" <a href='com.bridge4biz.laundry.ui.WebViewAgreementActivity://privacy'>");
                    html.append(getString(R.string.login_agreement_b));
                    html.append("</a> ");
                    html.append(getString(R.string.login_agreement_c));
                    html.append(" <a href='com.bridge4biz.laundry.ui.WebViewAgreementActivity://term-of-use'>");
                    html.append(getString(R.string.login_agreement_d));
                    html.append("</a>");
                    html.append(getString(R.string.login_agreement_e));
                }
                else {
                    html.append(getString(R.string.login_agreement_a));
                    html.append(" <a href='com.bridge4biz.laundry.ui.WebViewAgreementActivity://privacy'>");
                    html.append(getString(R.string.login_agreement_b));
                    html.append("</a> ");
                    html.append(getString(R.string.login_agreement_c));
                    html.append(" <a href='com.bridge4biz.laundry.ui.WebViewAgreementActivity://term-of-use'>");
                    html.append(getString(R.string.login_agreement_d));
                    html.append("</a> ");
                    html.append(getString(R.string.login_agreement_e));
                }

                mTextView.setClickable(false);
                mTextView.setText(Html.fromHtml(html.toString()));
                mTextView.setMovementMethod(LinkMovementMethod.getInstance());
                break;
        }

        return rootView;
    }
}
