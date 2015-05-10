/**
 * Copyright 2014 Daum Kakao Corp.
 *
 * Redistribution and modification in source or binary forms are not permitted without specific prior written permission. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kakao;

import android.os.Bundle;
import com.kakao.helper.ServerProtocol;
import com.kakao.http.HttpRequestTask;
import com.kakao.rest.APIHttpRequestTask;

/**
 * 푸시 서비스 API 요청을 담당한다.
 * @author MJ
 */
public class PushService {

    /**
     * 현 기기의 푸시 토큰을 등록한다.
     * 푸시 토큰 등록 후 푸시 토큰 삭제하기 전 또는 만료되기 전까지 서버에서 관리되어 푸시 메시지를 받을 수 있다.
     * @param responseHandler 요청 결과에 따른 콜백
     * @param pushToken 등록할 푸시 토큰
     * @param deviceId 한 사용자가 여러 기기를 사용할 수 있기 때문에 기기에 대한 유일한 id도 필요
     */
    public static void registerPushToken(final PushRegisterHttpResponseHandler responseHandler, final String pushToken, final String deviceId) {
        String url = HttpRequestTask.createBaseURL(ServerProtocol.API_AUTHORITY, ServerProtocol.PUSH_REGISTER_PATH);

        final Bundle parameters = new Bundle();
        parameters.putString(ServerProtocol.DEVICE_ID_PARAM_NAME, deviceId);
        parameters.putString(ServerProtocol.PUSH_TYPE_PARAM_NAME, ServerProtocol.PUSH_GCM_TYPE);
        parameters.putString(ServerProtocol.PUSH_TOKEN_PARAM_NAME, pushToken);

        APIHttpRequestTask.requestPost(responseHandler, Integer.class, url, parameters);
    }

    /**
     * 현 사용자 ID로 등록된 모든 푸시토큰 정보를 반환한다.
     * @param responseHandler 요청 결과에 따른 콜백
     */
    public static void getPushTokens(final PushTokensHttpResponseHandler<PushTokenInfo[]> responseHandler) {
        String url = HttpRequestTask.createBaseURL(ServerProtocol.API_AUTHORITY, ServerProtocol.PUSH_TOKENS_PATH);
        APIHttpRequestTask.requestGet(responseHandler, PushTokenInfo[].class, url, null);
    }

    /**
     * 사용자의 해당 기기의 푸시 토큰을 삭제한다. 대게 로그아웃시에 사용할 수 있다.
     * @param responseHandler 요청 결과에 따른 콜백
     * @param deviceId 해당기기의 푸시 토큰만 삭제하기 위해 기기 id 필요
     */
    public static void deregisterPushToken(final PushDeregisterHttpResponseHandler responseHandler, final String deviceId) {
        String url = HttpRequestTask.createBaseURL(ServerProtocol.API_AUTHORITY, ServerProtocol.PUSH_DEREGISTER_PATH);

        final Bundle parameters = new Bundle();
        parameters.putString(ServerProtocol.DEVICE_ID_PARAM_NAME, deviceId);
        parameters.putString(ServerProtocol.PUSH_TYPE_PARAM_NAME, ServerProtocol.PUSH_GCM_TYPE);

        APIHttpRequestTask.requestPost(responseHandler, Void.class, url, parameters);
    }

    /**
     * 사용자의 모든 푸시 토큰을 삭제한다. 대게 앱 탈퇴시 사용할 수 있다.
     * @param responseHandler 요청 결과에 따른 콜백
     */
    public static void deregisterPushTokenAll(final PushDeregisterHttpResponseHandler responseHandler) {
        String url = HttpRequestTask.createBaseURL(ServerProtocol.API_AUTHORITY, ServerProtocol.PUSH_DEREGISTER_PATH);
        APIHttpRequestTask.requestPost(responseHandler, Void.class, url, null);
    }

    /**
     * 자기 자신에게 푸시 메시지를 전송한다. 테스트 용도로만 사용할 수 있다. 다른 사람에게 푸시를 보내기 위해서는 서버에서 어드민키로 REST API를 사용해야한다.
     * @param responseHandler 요청 결과에 따른 콜백
     * @param pushMessage 보낼 푸시 메시지
     * @param deviceId 푸시 메시지를 보낼 기기의 id
     */
    public static void sendPushMessage(final PushSendHttpResponseHandler responseHandler, final String pushMessage, final String deviceId) {
        String url = HttpRequestTask.createBaseURL(ServerProtocol.API_AUTHORITY, ServerProtocol.PUSH_SEND_PATH);

        final Bundle parameters = new Bundle();
        parameters.putString(ServerProtocol.DEVICE_ID_PARAM_NAME, deviceId);
        parameters.putString(ServerProtocol.PUSH_MESSAGE_PARAM_NAME, pushMessage);

        APIHttpRequestTask.requestPost(responseHandler, Void.class, url, parameters);
    }

}
