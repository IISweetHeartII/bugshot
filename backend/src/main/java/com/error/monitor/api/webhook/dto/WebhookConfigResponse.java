package com.error.monitor.api.webhook.dto;

import com.error.monitor.domain.webhook.WebhookConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 웹훅 설정 응답
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookConfigResponse {
    private String id;
    private String projectId;
    private String type;
    private String name;
    private String webhookUrl;
    private boolean enabled;
    private List<String> severityFilters;
    private List<String> environmentFilters;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastTriggeredAt;
    private long totalSent;
    private long failureCount;

    public static WebhookConfigResponse from(WebhookConfig config) {
        return WebhookConfigResponse.builder()
                .id(config.getId())
                .projectId(config.getProject().getId())
                .type(config.getType().name())
                .name(config.getName())
                .webhookUrl(maskWebhookUrl(config.getWebhookUrl()))
                .enabled(config.isEnabled())
                .severityFilters(config.getSeverityFilters())
                .environmentFilters(config.getEnvironmentFilters())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .lastTriggeredAt(config.getLastTriggeredAt())
                .totalSent(config.getTotalSent())
                .failureCount(config.getFailureCount())
                .build();
    }

    /**
     * 웹훅 URL 마스킹 (보안)
     */
    private static String maskWebhookUrl(String url) {
        if (url == null || url.length() < 20) {
            return "***";
        }
        return url.substring(0, 30) + "..." + url.substring(url.length() - 10);
    }
}
