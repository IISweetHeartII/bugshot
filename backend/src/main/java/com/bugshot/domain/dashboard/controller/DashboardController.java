package com.bugshot.domain.dashboard.controller;

import com.bugshot.domain.dashboard.dto.DashboardStatsResponse;
import com.bugshot.domain.dashboard.dto.ErrorTrendResponse;
import com.bugshot.domain.dashboard.service.DashboardService;
import com.bugshot.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 대시보드 통계 API
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 대시보드 통계 조회
     * GET /api/dashboard/stats?projectId=xxx&period=7d
     * projectId가 "all"이면 사용자의 모든 프로젝트 통계를 합산
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats(
            @RequestParam(defaultValue = "all") String projectId,
            @RequestParam(defaultValue = "7d") String period,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        log.info("Get dashboard stats: userId={}, projectId={}, period={}", userId, projectId, period);

        DashboardStatsResponse stats = dashboardService.getProjectStats(userId, projectId, period);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 에러 트렌드 조회
     * GET /api/dashboard/trends?projectId=xxx&period=7d
     * projectId가 "all"이면 사용자의 모든 프로젝트 트렌드를 합산
     */
    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<List<ErrorTrendResponse>>> getTrends(
            @RequestParam(defaultValue = "all") String projectId,
            @RequestParam(defaultValue = "7d") String period,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        log.info("Get error trends: userId={}, projectId={}, period={}", userId, projectId, period);

        List<ErrorTrendResponse> trends = dashboardService.getErrorTrends(userId, projectId, period);
        return ResponseEntity.ok(ApiResponse.success(trends));
    }
}
