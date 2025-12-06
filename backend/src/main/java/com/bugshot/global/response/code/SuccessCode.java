package com.bugshot.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * API 성공 응답 코드
 */
@Getter
@RequiredArgsConstructor
public enum SuccessCode implements BaseCode {

    // 공통
    OK(200, "COMMON_200", "성공"),
    CREATED(201, "COMMON_201", "생성 성공"),
    NO_CONTENT(204, "COMMON_204", "삭제 성공"),

    // 프로젝트
    PROJECT_CREATED(201, "PROJECT_201", "프로젝트 생성 성공"),
    PROJECT_UPDATED(200, "PROJECT_200", "프로젝트 수정 성공"),
    PROJECT_DELETED(200, "PROJECT_200", "프로젝트 삭제 성공"),

    // 에러
    ERROR_RESOLVED(200, "ERROR_200", "에러 해결 처리 완료"),
    ERROR_IGNORED(200, "ERROR_200", "에러 무시 처리 완료"),

    // 알림
    NOTIFICATION_SENT(200, "NOTIFICATION_200", "알림 전송 성공"),
    WEBHOOK_CREATED(201, "WEBHOOK_201", "웹훅 생성 성공");

    private final int status;
    private final String code;
    private final String message;
}
