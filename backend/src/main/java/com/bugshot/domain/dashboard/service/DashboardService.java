package com.bugshot.domain.dashboard.service;

import com.bugshot.domain.dashboard.dto.DashboardStatsResponse;
import com.bugshot.domain.dashboard.dto.ErrorTrendResponse;
import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.repository.ErrorRepository;
import com.bugshot.domain.project.entity.Project;
import com.bugshot.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
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
    private final ProjectRepository projectRepository;

    /**
     * 프로젝트 통계 조회
     * - Redis 캐싱 적용 (10분)
     * - projectId가 "all"이면 userId의 모든 프로젝트 통계를 합산
     */
    @Cacheable(value = "dashboardStats", key = "#userId + ':' + #projectId + ':' + #period")
    @Transactional(readOnly = true)
    public DashboardStatsResponse getProjectStats(String userId, String projectId, String period) {
        LocalDateTime startTime = getStartTime(period);
        LocalDateTime now = LocalDateTime.now();

        // Fetch all errors for the project(s)
        List<Error> allErrors;
        if ("all".equalsIgnoreCase(projectId)) {
            // 사용자의 모든 프로젝트 에러 조회
            List<String> projectIds = projectRepository.findByUserId(userId).stream()
                    .map(Project::getId)
                    .collect(Collectors.toList());

            if (projectIds.isEmpty()) {
                return buildEmptyStats();
            }

            allErrors = errorRepository.findRecentErrorsForProjects(projectIds, startTime.minusYears(10));
        } else {
            allErrors = errorRepository.findRecentErrors(projectId, startTime.minusYears(10));
        }

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

        // 영향받은 사용자 수 (affectedUsersCount 합산)
        long affectedUsers = allErrors.stream()
                .mapToLong(Error::getAffectedUsersCount)
                .sum();

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
     * 빈 통계 응답 생성
     */
    private DashboardStatsResponse buildEmptyStats() {
        return DashboardStatsResponse.builder()
                .totalErrors(0)
                .unresolvedErrors(0)
                .todayErrors(0)
                .affectedUsers(0)
                .changeRate(0)
                .severityCount(DashboardStatsResponse.SeverityCount.builder()
                        .critical(0)
                        .high(0)
                        .medium(0)
                        .low(0)
                        .build())
                .build();
    }

    /**
     * 에러 트렌드 조회 (시간대별)
     * - Redis 캐싱 적용 (10분)
     * - projectId가 "all"이면 userId의 모든 프로젝트 트렌드를 합산
     */
    @Cacheable(value = "errorTrends", key = "#userId + ':' + #projectId + ':' + #period")
    @Transactional(readOnly = true)
    public List<ErrorTrendResponse> getErrorTrends(String userId, String projectId, String period) {
        LocalDateTime startTime = getStartTime(period);
        LocalDateTime now = LocalDateTime.now();

        List<Error> errors;
        if ("all".equalsIgnoreCase(projectId)) {
            List<String> projectIds = projectRepository.findByUserId(userId).stream()
                    .map(Project::getId)
                    .collect(Collectors.toList());

            if (projectIds.isEmpty()) {
                return Collections.emptyList();
            }

            errors = errorRepository.findRecentErrorsForProjects(projectIds, startTime);
        } else {
            errors = errorRepository.findRecentErrors(projectId, startTime);
        }

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
                    .mapToLong(Error::getAffectedUsersCount)
                    .sum();

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
