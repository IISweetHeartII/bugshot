package com.bugshot.global.dto;

import com.bugshot.global.response.ValidationError;
import com.bugshot.global.response.code.BaseCode;
import com.bugshot.global.response.code.ErrorCode;
import com.bugshot.global.response.code.SuccessCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 통일된 API 응답 포맷
 * 모든 API는 이 래퍼를 사용하여 일관된 응답 구조를 제공합니다.
 *
 * 응답 필드:
 * - success: 성공 여부
 * - code: 응답 코드 (COMMON_200, PROJECT_404 등)
 * - message: 응답 메시지
 * - timestamp: 응답 시간
 * - path: 요청 경로 (에러 시)
 * - traceId: 추적 ID (에러 시)
 * - data: 응답 데이터
 * - errors: 유효성 검증 에러 목록
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@JsonPropertyOrder({"success", "code", "message", "timestamp", "path", "traceId", "data", "errors"})
public class ApiResponse<T> {

    @JsonProperty("success")
    private final boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String code;

    private final String message;

    private final LocalDateTime timestamp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String path;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String traceId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final List<ValidationError> errors;

    // ========== 기본 생성자 (기존 호환성 유지) ==========

    private ApiResponse(boolean success, T data, String message, LocalDateTime timestamp) {
        this(success, null, message, timestamp, null, null, data, null);
    }

    private ApiResponse(boolean success, String code, String message, LocalDateTime timestamp,
                        String path, String traceId, T data, List<ValidationError> errors) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
        this.traceId = traceId;
        this.data = data;
        this.errors = errors;
    }

    // ========== 기존 메서드 (호환성 유지) ==========

    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, SuccessCode.OK.getCode(), SuccessCode.OK.getMessage(),
                LocalDateTime.now(), null, null, data, null);
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, SuccessCode.OK.getCode(), SuccessCode.OK.getMessage(),
                LocalDateTime.now(), null, null, null, null);
    }

    /**
     * 에러 응답 생성
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, ErrorCode.BAD_REQUEST.getCode(), message,
                LocalDateTime.now(), null, null, null, null);
    }

    /**
     * 에러 응답 생성 (커스텀 데이터 포함)
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, ErrorCode.BAD_REQUEST.getCode(), message,
                LocalDateTime.now(), null, null, data, null);
    }

    // ========== 새로운 메서드 (코드 기반) ==========

    /**
     * SuccessCode 기반 성공 응답
     */
    public static <T> ApiResponse<T> onSuccess(SuccessCode successCode, T data) {
        return new ApiResponse<>(true, successCode.getCode(), successCode.getMessage(),
                LocalDateTime.now(), null, null, data, null);
    }

    /**
     * SuccessCode 기반 성공 응답 (데이터 없음)
     */
    public static <T> ApiResponse<T> onSuccess(SuccessCode successCode) {
        return new ApiResponse<>(true, successCode.getCode(), successCode.getMessage(),
                LocalDateTime.now(), null, null, null, null);
    }

    /**
     * ErrorCode 기반 에러 응답
     */
    public static <T> ApiResponse<T> onFailure(BaseCode errorCode) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(),
                LocalDateTime.now(), null, null, null, null);
    }

    /**
     * ErrorCode 기반 에러 응답 (path, traceId 포함)
     */
    public static <T> ApiResponse<T> onFailure(BaseCode errorCode, String path, String traceId) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(),
                LocalDateTime.now(), path, traceId, null, null);
    }

    /**
     * ErrorCode 기반 에러 응답 (커스텀 메시지)
     */
    public static <T> ApiResponse<T> onFailure(BaseCode errorCode, String customMessage) {
        return new ApiResponse<>(false, errorCode.getCode(), customMessage,
                LocalDateTime.now(), null, null, null, null);
    }

    /**
     * 유효성 검증 에러 응답
     */
    public static <T> ApiResponse<T> onFailure(BaseCode errorCode, List<ValidationError> errors,
                                                String path, String traceId) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(),
                LocalDateTime.now(), path, traceId, null, errors);
    }

    // ========== 편의 메서드 ==========

    /**
     * 200 OK 응답
     */
    public static <T> ApiResponse<T> ok(T data) {
        return onSuccess(SuccessCode.OK, data);
    }

    /**
     * 201 Created 응답
     */
    public static <T> ApiResponse<T> created(T data) {
        return onSuccess(SuccessCode.CREATED, data);
    }
}
