package com.bugshot.domain.auth.dto;

import com.bugshot.domain.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용량 통계 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageStatsResponse {

    // 플랜 정보
    private String planType;

    // 프로젝트 사용량
    private int projectCount;
    private int projectLimit;

    // 월간 이벤트 사용량
    private long monthlyEvents;
    private long monthlyEventLimit;

    // 세션 리플레이 보관 기간 (일)
    private int sessionReplayRetentionDays;

    /**
     * 플랜별 제한 설정
     */
    public static UsageStatsResponse of(User user, int projectCount, long monthlyEvents) {
        PlanLimits limits = PlanLimits.of(user.getPlanType());

        return UsageStatsResponse.builder()
                .planType(user.getPlanType().name())
                .projectCount(projectCount)
                .projectLimit(limits.projectLimit)
                .monthlyEvents(monthlyEvents)
                .monthlyEventLimit(limits.monthlyEventLimit)
                .sessionReplayRetentionDays(limits.sessionReplayRetentionDays)
                .build();
    }

    /**
     * 플랜별 제한 값
     */
    @Getter
    @AllArgsConstructor
    public static class PlanLimits {
        private final int projectLimit;
        private final long monthlyEventLimit;
        private final int sessionReplayRetentionDays;

        public static PlanLimits of(User.PlanType planType) {
            return switch (planType) {
                case FREE -> new PlanLimits(3, 10_000, 7);
                case PRO -> new PlanLimits(10, 100_000, 30);
                case TEAM -> new PlanLimits(50, 1_000_000, 90);
            };
        }
    }
}
