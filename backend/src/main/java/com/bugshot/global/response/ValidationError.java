package com.bugshot.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 유효성 검증 실패 시 필드별 에러 정보
 */
@Getter
@AllArgsConstructor
public class ValidationError {
    private final String field;
    private final Object rejectedValue;
    private final String reason;
}
