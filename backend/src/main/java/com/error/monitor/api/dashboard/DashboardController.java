package com.error.monitor.api.dashboard;

import com.error.monitor.api.dashboard.dto.DashboardStatsResponse;
import com.error.monitor.api.dashboard.dto.ErrorTrendResponse;
import com.error.monitor.global.dto.ApiResponse;
import com.error.monitor.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats(
            @RequestParam String projectId,
            @RequestParam(defaultValue = "7d") String period,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        log.info("Get dashboard stats: userId={}, projectId={}, period={}", userId, projectId, period);

        DashboardStatsResponse stats = dashboardService.getProjectStats(projectId, period);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 에러 트렌드 조회
     * GET /api/dashboard/trends?projectId=xxx&period=7d
     */
    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<List<ErrorTrendResponse>>> getTrends(
            @RequestParam String projectId,
            @RequestParam(defaultValue = "7d") String period,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        log.info("Get error trends: userId={}, projectId={}, period={}", userId, projectId, period);

        List<ErrorTrendResponse> trends = dashboardService.getErrorTrends(projectId, period);
        return ResponseEntity.ok(ApiResponse.success(trends));
    }
}
