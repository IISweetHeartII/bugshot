package com.bugshot.domain.webhook.controller;

import com.bugshot.domain.webhook.dto.WebhookConfigRequest;
import com.bugshot.domain.webhook.dto.WebhookConfigResponse;
import com.bugshot.domain.webhook.service.WebhookService;
import com.bugshot.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 웹훅 설정 API (Discord, Slack 등)
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * 프로젝트의 웹훅 목록 조회
     * GET /api/webhooks?projectId=xxx
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WebhookConfigResponse>>> getWebhooks(
            @RequestParam String projectId,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        log.info("Get webhooks: userId={}, projectId={}", userId, projectId);

        List<WebhookConfigResponse> webhooks = webhookService.getProjectWebhooks(projectId);
        return ResponseEntity.ok(ApiResponse.success(webhooks));
    }

    /**
     * 웹훅 생성
     * POST /api/webhooks
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WebhookConfigResponse>> createWebhook(
            @Valid @RequestBody WebhookConfigRequest request,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        log.info("Create webhook: userId={}, projectId={}, type={}",
                userId, request.getProjectId(), request.getType());

        WebhookConfigResponse webhook = webhookService.createWebhook(request);
        return ResponseEntity.ok(ApiResponse.success(webhook));
    }

    /**
     * 웹훅 수정
     * PUT /api/webhooks/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WebhookConfigResponse>> updateWebhook(
            @PathVariable String id,
            @Valid @RequestBody WebhookConfigRequest request,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        log.info("Update webhook: userId={}, webhookId={}", userId, id);

        WebhookConfigResponse webhook = webhookService.updateWebhook(id, request);
        return ResponseEntity.ok(ApiResponse.success(webhook));
    }

    /**
     * 웹훅 삭제
     * DELETE /api/webhooks/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWebhook(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        log.info("Delete webhook: userId={}, webhookId={}", userId, id);

        webhookService.deleteWebhook(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 웹훅 테스트 전송
     * POST /api/webhooks/{id}/test
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<ApiResponse<String>> testWebhook(
            @PathVariable String id,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        log.info("Test webhook: userId={}, webhookId={}", userId, id);

        String result = webhookService.testWebhook(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
