package com.error.monitor.service;

import com.error.monitor.api.dashboard.dto.DashboardStatsResponse;
import com.error.monitor.api.dashboard.dto.ErrorTrendResponse;
import com.error.monitor.domain.error.Error;
import com.error.monitor.domain.error.ErrorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 대시보드 통계 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ErrorRepository errorRepository;

    /**
     * 프로젝트 통계 조회
     */
    @Transactional(readOnly = true)
    public DashboardStatsResponse getProjectStats(String projectId, String period) {
        LocalDateTime startTime = getStartTime(period);
        LocalDateTime now = LocalDateTime.now();

        List<Error> allErrors = errorRepository.findByProjectId(projectId);
        List<Error> periodErrors = allErrors.stream()
                .filter(e -> e.getFirstSeenAt().isAfter(startTime))
                .toList();

        // 총 에러 발생 건수 (occurrence count 합산)
        long totalErrors = allErrors.stream()
                .mapToLong(Error::getOccurrenceCount)
                .sum();

        // 미해결 에러 수
        long unresolvedErrors = allErrors.stream()
                .filter(e -> e.getStatus() == Error.ErrorStatus.UNRESOLVED)
                .count();

        // 오늘 발생한 에러
        LocalDateTime todayStart = now.truncatedTo(ChronoUnit.DAYS);
        long todayErrors = allErrors.stream()
                .filter(e -> e.getLastSeenAt().isAfter(todayStart))
                .count();

        // 영향받은 사용자 수
        long affectedUsers = allErrors.stream()
                .flatMap(e -> e.getAffectedUsers().stream())
                .distinct()
                .count();

        // 심각도별 에러 수
        Map<Error.Severity, Long> severityMap = allErrors.stream()
                .collect(Collectors.groupingBy(Error::getSeverity, Collectors.counting()));

        DashboardStatsResponse.SeverityCount severityCount = DashboardStatsResponse.SeverityCount.builder()
                .critical(severityMap.getOrDefault(Error.Severity.CRITICAL, 0L))
                .high(severityMap.getOrDefault(Error.Severity.HIGH, 0L))
                .medium(severityMap.getOrDefault(Error.Severity.MEDIUM, 0L))
                .low(severityMap.getOrDefault(Error.Severity.LOW, 0L))
                .build();

        // 전일 대비 증감률 계산
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        long yesterdayErrors = allErrors.stream()
                .filter(e -> e.getLastSeenAt().isAfter(yesterdayStart) && e.getLastSeenAt().isBefore(todayStart))
                .count();
        double changeRate = yesterdayErrors == 0 ? 0 : ((double) (todayErrors - yesterdayErrors) / yesterdayErrors) * 100;

        return DashboardStatsResponse.builder()
                .totalErrors(totalErrors)
                .unresolvedErrors(unresolvedErrors)
                .todayErrors(todayErrors)
                .affectedUsers(affectedUsers)
                .changeRate(changeRate)
                .severityCount(severityCount)
                .build();
    }

    /**
     * 에러 트렌드 조회 (시간대별)
     */
    @Transactional(readOnly = true)
    public List<ErrorTrendResponse> getErrorTrends(String projectId, String period) {
        LocalDateTime startTime = getStartTime(period);
        LocalDateTime now = LocalDateTime.now();

        List<Error> errors = errorRepository.findByProjectId(projectId);

        // 기간에 따라 시간 단위 결정 (1일: 시간별, 7일: 일별, 30일: 일별)
        ChronoUnit unit = period.equals("1d") ? ChronoUnit.HOURS : ChronoUnit.DAYS;
        int intervals = period.equals("1d") ? 24 : (period.equals("7d") ? 7 : 30);

        List<ErrorTrendResponse> trends = new ArrayList<>();
        for (int i = 0; i < intervals; i++) {
            LocalDateTime intervalStart = startTime.plus(i, unit);
            LocalDateTime intervalEnd = intervalStart.plus(1, unit);

            long errorCount = errors.stream()
                    .filter(e -> e.getLastSeenAt().isAfter(intervalStart) && e.getLastSeenAt().isBefore(intervalEnd))
                    .count();

            long userCount = errors.stream()
                    .filter(e -> e.getLastSeenAt().isAfter(intervalStart) && e.getLastSeenAt().isBefore(intervalEnd))
                    .flatMap(e -> e.getAffectedUsers().stream())
                    .distinct()
                    .count();

            trends.add(ErrorTrendResponse.builder()
                    .timestamp(intervalStart)
                    .errorCount(errorCount)
                    .userCount(userCount)
                    .build());
        }

        return trends;
    }

    /**
     * 기간 문자열을 LocalDateTime으로 변환
     */
    private LocalDateTime getStartTime(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case "1d" -> now.minusDays(1);
            case "7d" -> now.minusDays(7);
            case "30d" -> now.minusDays(30);
            default -> now.minusDays(7);
        };
    }
}
