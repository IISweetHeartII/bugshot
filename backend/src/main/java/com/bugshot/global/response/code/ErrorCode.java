package com.bugshot.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * API 에러 응답 코드
 * 도메인별로 체계적인 에러 코드를 관리합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseCode {

    // ========== 공통 에러 (4xx) ==========
    BAD_REQUEST(400, "COMMON_400", "잘못된 요청입니다"),
    MISSING_PARAMETER(400, "COMMON_400", "필수 파라미터가 누락되었습니다"),
    VALIDATION_FAILED(400, "COMMON_400", "입력값 검증에 실패했습니다"),
    UNAUTHORIZED(401, "COMMON_401", "인증이 필요합니다"),
    FORBIDDEN(403, "COMMON_403", "접근 권한이 없습니다"),
    NOT_FOUND(404, "COMMON_404", "요청한 리소스를 찾을 수 없습니다"),
    METHOD_NOT_ALLOWED(405, "COMMON_405", "지원하지 않는 HTTP 메서드입니다"),
    CONFLICT(409, "COMMON_409", "이미 존재하는 리소스입니다"),
    RATE_LIMIT_EXCEEDED(429, "COMMON_429", "요청 횟수를 초과했습니다"),

    // ========== 공통 에러 (5xx) ==========
    INTERNAL_SERVER_ERROR(500, "COMMON_500", "서버 내부 오류가 발생했습니다"),
    DATABASE_ERROR(500, "COMMON_500", "데이터베이스 오류가 발생했습니다"),

    // ========== 인증/사용자 관련 ==========
    USER_NOT_FOUND(404, "USER_404", "사용자를 찾을 수 없습니다"),
    USER_ALREADY_EXISTS(409, "USER_409", "이미 존재하는 사용자입니다"),
    INVALID_USER_ID(400, "USER_400", "유효하지 않은 사용자 ID입니다"),

    // ========== 프로젝트 관련 ==========
    PROJECT_NOT_FOUND(404, "PROJECT_404", "프로젝트를 찾을 수 없습니다"),
    PROJECT_ACCESS_DENIED(403, "PROJECT_403", "프로젝트에 대한 접근 권한이 없습니다"),
    PROJECT_LIMIT_EXCEEDED(400, "PROJECT_400", "프로젝트 생성 한도를 초과했습니다"),
    INVALID_API_KEY(401, "PROJECT_401", "유효하지 않은 API 키입니다"),

    // ========== 에러 관련 ==========
    ERROR_NOT_FOUND(404, "ERROR_404", "에러를 찾을 수 없습니다"),
    ERROR_ACCESS_DENIED(403, "ERROR_403", "에러에 대한 접근 권한이 없습니다"),

    // ========== 세션 리플레이 관련 ==========
    REPLAY_NOT_FOUND(404, "REPLAY_404", "세션 리플레이를 찾을 수 없습니다"),
    REPLAY_UPLOAD_FAILED(500, "REPLAY_500", "세션 리플레이 업로드에 실패했습니다"),

    // ========== 알림/웹훅 관련 ==========
    NOTIFICATION_SEND_FAILED(500, "NOTIFICATION_500", "알림 전송에 실패했습니다"),
    WEBHOOK_NOT_FOUND(404, "WEBHOOK_404", "웹훅을 찾을 수 없습니다"),
    WEBHOOK_INVALID_URL(400, "WEBHOOK_400", "유효하지 않은 웹훅 URL입니다"),

    // ========== 데이터 수집 관련 ==========
    INGEST_INVALID_PAYLOAD(400, "INGEST_400", "유효하지 않은 에러 데이터입니다"),
    INGEST_PROJECT_INACTIVE(400, "INGEST_400", "비활성화된 프로젝트입니다");

    private final int status;
    private final String code;
    private final String message;
}
