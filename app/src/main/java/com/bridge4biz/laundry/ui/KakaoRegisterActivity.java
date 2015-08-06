package com.bridge4biz.laundry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bridge4biz.laundry.CleanBasketApplication;
import com.bridge4biz.laundry.R;
import com.bridge4biz.laundry.io.RequestQueue;
import com.bridge4biz.laundry.io.model.JsonData;
import com.bridge4biz.laundry.io.model.UserData;
import com.bridge4biz.laundry.io.request.PostRequest;
import com.bridge4biz.laundry.io.request.StringRequest;
import com.bridge4biz.laundry.util.AddressManager;
import com.bridge4biz.laundry.util.Constants;
import com.bridge4biz.laundry.util.HashGenerator;
import com.google.gson.JsonSyntaxException;
import com.kakao.APIErrorResult;
import com.kakao.LogoutResponseCallback;
import com.kakao.MeResponseCallback;
import com.kakao.SignupResponseCallback;
import com.kakao.UserManagement;
import com.kakao.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

public class KakaoRegisterActivity extends BaseActivity {
    private static final String TAG = KakaoRegisterActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestMe();
    }

    protected void redirectLoginActivity() {
        Intent intent = new Intent();
        intent.setAction("com.bridge4biz.laundry.ui.LoginActivity");
        startActivity(intent);
    }

    protected void redirectMainActivity() {
        Intent intent = new Intent();
        intent.setAction("com.bridge4biz.laundry.ui.MainActivity");
        startActivity(intent);
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
        postRequest.setListener(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JsonData jsonData;

                try {
                    jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response.toString(), JsonData.class);
                } catch (JsonSyntaxException e) {
                    return;
                } catch (NullPointerException e) {
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                logout();
            }
        });
        RequestQueue.getInstance(this).addToRequestQueue(postRequest.doRequest());
    }

    private void login(final long userId) {
        StringRequest stringRequest = new StringRequest(this);
        stringRequest.setParams("email", String.valueOf(userId));
        stringRequest.setParams("password", HashGenerator.makeHash(String.valueOf(userId)));
        stringRequest.setParams("remember", "true");
        stringRequest.setUrl(AddressManager.LOGIN);
        stringRequest.setListener(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JsonData jsonData;
                jsonData = CleanBasketApplication.getInstance().getGson().fromJson(response, JsonData.class);

                switch (jsonData.constant) {
                    case Constants.SESSION_EXPIRED:
                        CleanBasketApplication.getInstance().showToast(getString(R.string.session_invalid));
                        logout();
                        break;

                    case Constants.EMAIL_ERROR:
                        signUpServer(userId);
                        break;

                    case Constants.PASSWORD_ERROR:
                        logout();
                        break;

                    case Constants.ACCOUNT_DISABLED:
                        CleanBasketApplication.getInstance().showToast(getString(R.string.disable_error));
                        logout();
                        break;

                    case Constants.SUCCESS:
                        logoutKakao();
                        CleanBasketApplication.getInstance().storeRegistrationId(getBaseContext(), null);
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                logout();
                redirectLoginActivity();
            }
        });
        RequestQueue.getInstance(this).addToRequestQueue(stringRequest.doRequest());
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
        try {
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
        } catch (IllegalStateException e) {
            Log.e(TAG, e.toString());
        }
    }
}
