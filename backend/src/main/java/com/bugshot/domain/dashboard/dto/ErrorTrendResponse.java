package com.bugshot.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 에러 트렌드 응답 (시간대별 에러 발생 추이)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorTrendResponse {
    /**
     * 타임스탬프
     */
    private LocalDateTime timestamp;

    /**
     * 에러 발생 건수
     */
    private long errorCount;

    /**
     * 영향받은 사용자 수
     */
    private long userCount;
}
