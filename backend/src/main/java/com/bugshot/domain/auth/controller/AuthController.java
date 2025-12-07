package com.bugshot.domain.auth.controller;

import com.bugshot.domain.auth.dto.OAuthLoginRequest;
import com.bugshot.domain.auth.dto.OAuthLoginResponse;
import com.bugshot.domain.auth.dto.UsageStatsResponse;
import com.bugshot.domain.auth.service.AuthService;
import com.bugshot.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * OAuth 로그인 처리 (GitHub, Google)
     */
    @PostMapping("/oauth")
    public ResponseEntity<ApiResponse<OAuthLoginResponse>> oauthLogin(@Valid @RequestBody OAuthLoginRequest request) {
        log.info("OAuth login request: provider={}, email={}", request.getProvider(), request.getEmail());
        OAuthLoginResponse response = authService.processOAuthLogin(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<OAuthLoginResponse>> getCurrentUser(@RequestHeader("X-User-Id") String userId) {
        log.info("Get current user: userId={}", userId);
        OAuthLoginResponse response = authService.getUserInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 사용량 통계 조회
     * GET /api/auth/usage
     */
    @GetMapping("/usage")
    public ResponseEntity<ApiResponse<UsageStatsResponse>> getUsageStats(@RequestHeader("X-User-Id") String userId) {
        log.info("Get usage stats: userId={}", userId);
        UsageStatsResponse response = authService.getUsageStats(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
