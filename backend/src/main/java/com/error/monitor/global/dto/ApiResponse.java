package com.error.monitor.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 통일된 API 응답 포맷
 * 모든 API는 이 래퍼를 사용하여 일관된 응답 구조를 제공합니다.
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    /**
     * 성공 여부
     */
    private boolean success;

    /**
     * 응답 데이터
     */
    private T data;

    /**
     * 에러 메시지 (성공 시 null)
     */
    private String message;

    /**
     * 응답 타임스탬프
     */
    private LocalDateTime timestamp;

    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, LocalDateTime.now());
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, null, LocalDateTime.now());
    }

    /**
     * 에러 응답 생성
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, LocalDateTime.now());
    }

    /**
     * 에러 응답 생성 (커스텀 데이터 포함)
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, data, message, LocalDateTime.now());
    }
}
