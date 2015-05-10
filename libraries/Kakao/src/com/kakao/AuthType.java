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

/**
 * @author MJ
 */
public enum AuthType {
    KAKAO_TALK(0),
    KAKAO_STORY(1),
    KAKAO_ACCOUNT(2);

    private final int number;

    AuthType(int i) {
        this.number = i;
    }

    public int getNumber() {
        return number;
    }

    public static AuthType valueOf(int number){
        if(number == KAKAO_TALK.getNumber()) {
            return KAKAO_TALK;
        } else if (number == KAKAO_STORY.getNumber()) {
            return KAKAO_STORY;
        } else if (number == KAKAO_ACCOUNT.getNumber()) {
            return KAKAO_ACCOUNT;
        } else {
            return null;
        }
    }
}
