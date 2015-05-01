package com.washappkorea.corp.cleanbasket.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.toolbox.RequestFuture;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kakao.APIErrorResult;
import com.kakao.LogoutResponseCallback;
import com.kakao.MeResponseCallback;
import com.kakao.SignupResponseCallback;
import com.kakao.UserManagement;
import com.kakao.UserProfile;
import com.washappkorea.corp.cleanbasket.CleanBasketApplication;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.RequestQueue;
import com.washappkorea.corp.cleanbasket.io.model.JsonData;
import com.washappkorea.corp.cleanbasket.io.model.UserData;
import com.washappkorea.corp.cleanbasket.io.request.PostRequest;
import com.washappkorea.corp.cleanbasket.io.request.StringRequest;
import com.washappkorea.corp.cleanbasket.util.AddressManager;
import com.washappkorea.corp.cleanbasket.util.Constants;
import com.washappkorea.corp.cleanbasket.util.HashGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class KakaoRegisterActivity extends BaseActivity {
    private static final String TAG = KakaoRegisterActivity.class.getSimpleName();

    private Gson mGson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGson = new Gson();

        requestMe();
    }

    protected void redirectLoginActivity() {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
        finish();
    }

    protected void redirectMainActivity() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 가입 입력창의 정보를 모아서 가입 API를 호출한다.
     */
    private void signUp() {
        UserManagement.requestSignup(new SignupResponseCallback() {
            @Override
            protected void onSuccess(final long userId) {
                signUpServer(userId);
            }

            @Override
            protected void onSessionClosedFailure(final APIErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            protected void onFailure(final APIErrorResult errorResult) {
                String message = "failed to sign up. msg=" + errorResult;
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                redirectLoginActivity();
            }
        }, null);
    }

    private void signUpServer(final long userId) {
        UserData userData = new UserData();
        userData.email = String.valueOf(userId);
        userData.password = HashGenerator.makeHash(String.valueOf(userId));

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        PostRequest postRequest = new PostRequest(this);
        String body = CleanBasketApplication.getInstance().getGson().toJson(userData);

        try {
            JSONObject jsonObject = new JSONObject(body);
            postRequest.setParams(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        postRequest.setUrl(AddressManager.REGISTER);
        postRequest.setListener(requestFuture, requestFuture);
        RequestQueue.getInstance(this).addToRequestQueue(postRequest.doRequest());

        JSONObject response = null;

        try {
            response = requestFuture.get();
        } catch (InterruptedException e) {
            Log.i(TAG, e.toString());
        } catch (ExecutionException e) {
            Log.i(TAG, e.toString());
        }

        JsonData jsonData = null;

        try {
            jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);
        } catch (JsonSyntaxException e) {
            return;
        } catch (NullPointerException e) {
            return;
        }

        if (jsonData.constant < 0) {
            CleanBasketApplication.getInstance().showToast(getString(R.string.network_error));
            redirectLoginActivity();
            return;
        }

        switch (jsonData.constant) {
            case Constants.ACCOUNT_DUPLICATION:
                login(userId);
                break;
            case Constants.ERROR:
                CleanBasketApplication.getInstance().showToast(getString(R.string.toast_error));
                logout();
                break;

            case Constants.SUCCESS:
                CleanBasketApplication.getInstance().showToast(getString(R.string.sign_up_success));
                login(userId);
                break;
        }

        return;
    }

    private void login(final long userId) {
        RequestFuture<String> requestFuture = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(this);
        stringRequest.setParams("email", String.valueOf(userId));
        stringRequest.setParams("password", String.valueOf(userId));
        stringRequest.setParams("remember", "true");
        stringRequest.setUrl(AddressManager.LOGIN);
        stringRequest.setListener(requestFuture, requestFuture);
        RequestQueue.getInstance(this).addToRequestQueue(stringRequest.doRequest());

        String response = null;

        try {
            response = requestFuture.get();
        } catch (InterruptedException e) {
            Log.i(TAG, e.toString());
        } catch (ExecutionException e) {
            Log.i(TAG, e.toString());
        }

        JsonData jsonData = null;

        try {
            jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);
        } catch (JsonSyntaxException e) {
            logout();
            return;
        } catch (NullPointerException e) {
            logout();
            return;
        }

        if (jsonData.constant < 0) {
            CleanBasketApplication.getInstance().showToast(getString(R.string.network_error));
            logout();
            return;
        }

        switch (jsonData.constant) {
            case Constants.SESSION_EXPIRED:
                CleanBasketApplication.getInstance().showToast(getString(R.string.session_invalid));
                logout();
                break;
            case Constants.ACCOUNT_DISABLED:
                CleanBasketApplication.getInstance().showToast(getString(R.string.disable_error));
                logout();
                break;
            case Constants.SUCCESS:
                logoutKakao();
                break;
        }

        return;
    }

    private void logout() {
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            protected void onSuccess(final long userId) {
                redirectLoginActivity();
            }

            @Override
            protected void onFailure(final APIErrorResult apiErrorResult) {
                redirectLoginActivity();
            }
        });
    }

    private void logoutKakao() {
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            protected void onSuccess(final long userId) {
                redirectMainActivity();
                finish();
            }

            @Override
            protected void onFailure(final APIErrorResult apiErrorResult) {
                redirectLoginActivity();
            }
        });
    }
    /**
     * 사용자의 상태를 알아 보기 위해 me API 호출을 한다.
     */
    private void requestMe() {
        UserManagement.requestMe(new MeResponseCallback() {

            @Override
            protected void onSuccess(final UserProfile userProfile) {
                login(userProfile.getId());
            }

            @Override
            protected void onNotSignedUp() {
                signUp();
            }

            @Override
            protected void onSessionClosedFailure(final APIErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            protected void onFailure(final APIErrorResult errorResult) {
                redirectLoginActivity();
            }
        });
    }
}
