package com.bugshot.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대시보드 통계 응답
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    /**
     * 총 에러 발생 건수
     */
    private long totalErrors;

    /**
     * 미해결 에러 수
     */
    private long unresolvedErrors;

    /**
     * 오늘 발생한 에러 수
     */
    private long todayErrors;

    /**
     * 영향받은 사용자 수
     */
    private long affectedUsers;

    /**
     * 전일 대비 증감률 (%)
     */
    private double changeRate;

    /**
     * 평균 응답 시간 (ms)
     */
    private Double avgResponseTime;

    /**
     * 심각도별 에러 수
     */
    private SeverityCount severityCount;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeverityCount {
        private long critical;
        private long high;
        private long medium;
        private long low;
    }
}
