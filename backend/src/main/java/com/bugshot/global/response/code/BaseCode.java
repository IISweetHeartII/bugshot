package com.bugshot.global.response.code;

/**
 * 응답 코드 기본 인터페이스
 * SuccessCode와 ErrorCode가 공통으로 구현합니다.
 */
public interface BaseCode {
    int getStatus();
    String getCode();
    String getMessage();
}
