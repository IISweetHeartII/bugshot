package com.bugshot.domain.notification.strategy;

import com.bugshot.domain.common.util.NotificationFormatter;
import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.entity.ErrorOccurrence;
import com.bugshot.domain.notification.entity.NotificationChannel;
import com.bugshot.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Slack 알림 전송 전략
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SlackNotificationStrategy implements NotificationStrategy {

    private final WebClient.Builder webClientBuilder;

    @Override
    public NotificationChannel.ChannelType getChannelType() {
        return NotificationChannel.ChannelType.SLACK;
    }

    @Override
    public void send(NotificationChannel channel, Project project, Error error, ErrorOccurrence occurrence) {
        String webhookUrl = channel.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Slack webhook URL is missing");
            return;
        }

        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", NotificationFormatter.getSlackColor(error.getSeverity()));
        attachment.put("title", error.getErrorType());
        attachment.put("text", error.getErrorMessage());
        attachment.put("fields", List.of(
                Map.of("title", "발생 횟수", "value", error.getOccurrenceCount() + "회", "short", true),
                Map.of("title", "위치", "value", NotificationFormatter.formatLocation(error), "short", true)
        ));

        Map<String, Object> payload = Map.of(
                "text", NotificationFormatter.getSeverityEmoji(error.getSeverity()) + " 에러 발생",
                "attachments", List.of(attachment)
        );

        webClientBuilder.build()
                .post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> log.info("Slack notification sent: errorId={}", error.getId()),
                        e -> log.error("Slack notification failed: errorId={}", error.getId(), e)
                );
    }

    @Override
    public void sendTest(NotificationChannel channel) {
        String webhookUrl = channel.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            throw new IllegalArgumentException("Slack webhook URL is required");
        }

        Map<String, Object> payload = Map.of(
                "text", "✅ 웹훅 테스트",
                "blocks", List.of(Map.of(
                        "type", "section",
                        "text", Map.of(
                                "type", "mrkdwn",
                                "text", "BugShot 웹훅이 정상적으로 작동합니다!"
                        )
                ))
        );

        webClientBuilder.build()
                .post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("Slack test message sent");
    }
}
