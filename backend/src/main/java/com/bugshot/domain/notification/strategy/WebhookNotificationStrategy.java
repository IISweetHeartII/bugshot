package com.bugshot.domain.notification.strategy;

import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.entity.ErrorOccurrence;
import com.bugshot.domain.notification.entity.NotificationChannel;
import com.bugshot.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * 커스텀 Webhook 알림 전송 전략
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookNotificationStrategy implements NotificationStrategy {

    private final WebClient.Builder webClientBuilder;

    @Override
    public NotificationChannel.ChannelType getChannelType() {
        return NotificationChannel.ChannelType.WEBHOOK;
    }

    @Override
    public void send(NotificationChannel channel, Project project, Error error, ErrorOccurrence occurrence) {
        String webhookUrl = channel.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Custom webhook URL is missing");
            return;
        }

        Map<String, Object> payload = Map.of(
                "projectId", project.getId(),
                "projectName", project.getName(),
                "errorId", error.getId(),
                "errorType", error.getErrorType(),
                "errorMessage", error.getErrorMessage(),
                "severity", error.getSeverity().name(),
                "occurrenceCount", error.getOccurrenceCount(),
                "url", occurrence.getUrl(),
                "timestamp", occurrence.getOccurredAt().toString() + "Z"
        );

        webClientBuilder.build()
                .post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> log.info("Webhook notification sent: errorId={}", error.getId()),
                        e -> log.error("Webhook notification failed: errorId={}", error.getId(), e)
                );
    }

    @Override
    public void sendTest(NotificationChannel channel) {
        String webhookUrl = channel.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            throw new IllegalArgumentException("Webhook URL is required");
        }

        Map<String, Object> payload = Map.of(
                "type", "test",
                "message", "BugShot 웹훅 테스트",
                "timestamp", java.time.LocalDateTime.now().toString() + "Z"
        );

        webClientBuilder.build()
                .post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("Custom webhook test message sent");
    }
}
