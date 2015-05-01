package com.washappkorea.corp.cleanbasket.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.toolbox.ImageLoader;
import com.kakao.AuthType;
import com.kakao.Session;
import com.kakao.SessionCallback;
import com.kakao.exception.KakaoException;
import com.kakao.helper.StoryProtocol;
import com.kakao.helper.TalkProtocol;
import com.washappkorea.corp.cleanbasket.Config;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.ui.dialog.EmailDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginFragment extends Fragment {
    public static final String ARG_OBJECT = LoginFragment.class.getSimpleName();

    private final SessionCallback mySessionCallback = new MySessionStatusCallback();
    private Session session;

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
                                mSeoulMap, R.drawable.ic_loading, R.drawable.ic_loading
                        ));
                imageLoader.get(Config.INCHEON_IMAGE_ADDRESS,
                        ImageLoader.getImageListener(
                                mIncheonMap, R.drawable.ic_loading, R.drawable.ic_loading
                        ));
                break;

            case 3:
                rootView = inflater.inflate(R.layout.fragment_service_login, container, false);

                RelativeLayout mKakaoLogin = (RelativeLayout) rootView.findViewById(R.id.layout_button_kakao);
                RelativeLayout mEmailLogin = (RelativeLayout) rootView.findViewById(R.id.layout_button_email);

                mKakaoLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 카톡 또는 카스가 존재하면 옵션을 보여주고, 존재하지 않으면 바로 직접 로그인창.
                        final List<AuthType> authTypes = getAuthTypes();
                        Session.getCurrentSession().open(authTypes.get(0), getActivity());
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

                // 세션 콜백 추가
                Session.initialize(getActivity(), AuthType.KAKAO_TALK);
                session = Session.getCurrentSession();
                session.addCallback(mySessionCallback);
                break;
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        switch (getArguments().getInt(ARG_OBJECT)) {
            case 3:
                if (!session.isClosed()) {
                    session.implicitOpen();
                }
        }
    }

    private class MySessionStatusCallback implements SessionCallback {
        @Override
        public void onSessionOpened() {
            Log.i(ARG_OBJECT, "onSessionOpened()");
            Intent intent = new Intent();
            intent.setAction("com.washappkorea.corp.cleanbasket.ui.KakaoRegisterActivity");
            startActivity(intent);
        }

        @Override
        public void onSessionClosed(final KakaoException exception) {
            Log.i(ARG_OBJECT, "onSessionClosed()");
        }

        @Override
        public void onSessionOpening() {
            Log.i(ARG_OBJECT, "onSessionOpening()");
        }
    }

    private List<AuthType> getAuthTypes() {
        final List<AuthType> availableAuthTypes = new ArrayList<AuthType>();

        if(TalkProtocol.existCapriLoginActivityInTalk(getActivity(), true)){
            availableAuthTypes.add(AuthType.KAKAO_TALK);
        }

        if(StoryProtocol.existCapriLoginActivityInStory(getActivity(), true)){
            availableAuthTypes.add(AuthType.KAKAO_STORY);
        }

        availableAuthTypes.add(AuthType.KAKAO_ACCOUNT);

        final AuthType[] selectedAuthTypes = Session.getCurrentSession().getAuthTypes();
        availableAuthTypes.retainAll(Arrays.asList(selectedAuthTypes));

        // 개발자가 설정한 것과 available 한 타입이 없다면 직접계정 입력이 뜨도록 한다.
        if(availableAuthTypes.size() == 0){
            availableAuthTypes.add(AuthType.KAKAO_ACCOUNT);
        }

        return availableAuthTypes;
    }
}
