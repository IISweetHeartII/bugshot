package com.bugshot.domain.replay.controller;

import com.bugshot.domain.replay.dto.SessionReplayResponse;
import com.bugshot.domain.replay.service.SessionReplayService;
import com.bugshot.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 세션 리플레이 API
 */
@RestController
@RequestMapping("/api/replays")
@RequiredArgsConstructor
@Slf4j
public class SessionReplayController {

    private final SessionReplayService sessionReplayService;

    /**
     * 세션 리플레이 조회
     * GET /api/replays/{errorId}
     */
    @GetMapping("/{errorId}")
    public ResponseEntity<ApiResponse<SessionReplayResponse>> getSessionReplay(
            @PathVariable String errorId,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        log.info("Get session replay: userId={}, errorId={}", userId, errorId);

        SessionReplayResponse replay = sessionReplayService.getSessionReplay(errorId);
        return ResponseEntity.ok(ApiResponse.success(replay));
    }

    /**
     * 세션 리플레이 Pre-signed URL 생성
     * GET /api/replays/{errorId}/download-url
     */
    @GetMapping("/{errorId}/download-url")
    public ResponseEntity<ApiResponse<String>> getDownloadUrl(
            @PathVariable String errorId,
            @RequestParam(defaultValue = "3600") int expirationSeconds,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        log.info("Generate replay download URL: userId={}, errorId={}, expiration={}",
                userId, errorId, expirationSeconds);

        String downloadUrl = sessionReplayService.generateDownloadUrl(errorId, expirationSeconds);
        return ResponseEntity.ok(ApiResponse.success(downloadUrl));
    }
}
