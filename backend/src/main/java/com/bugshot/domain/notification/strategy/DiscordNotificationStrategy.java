package com.bugshot.domain.notification.strategy;

import com.bugshot.domain.common.util.NotificationFormatter;
import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.entity.ErrorOccurrence;
import com.bugshot.domain.notification.discord.DiscordBotService;
import com.bugshot.domain.notification.entity.NotificationChannel;
import com.bugshot.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Discord 알림 전송 전략
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordNotificationStrategy implements NotificationStrategy {

    private final WebClient.Builder webClientBuilder;

    @Autowired(required = false)
    private DiscordBotService discordBotService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public NotificationChannel.ChannelType getChannelType() {
        return NotificationChannel.ChannelType.DISCORD;
    }

    @Override
    public void send(NotificationChannel channel, Project project, Error error, ErrorOccurrence occurrence) {
        // Discord Bot 우선 시도 (채널 ID가 설정된 경우)
        String channelId = (String) channel.getConfig().get("channelId");
        if (channelId != null && discordBotService != null) {
            if (sendViaBot(channelId, project, error, occurrence)) {
                return;
            }
            log.warn("Discord bot failed, falling back to webhook");
        }

        // Webhook으로 전송
        sendViaWebhook(channel.getWebhookUrl(), project, error, occurrence);
    }

    private boolean sendViaBot(String channelId, Project project, Error error, ErrorOccurrence occurrence) {
        try {
            DiscordBotService.ErrorNotificationData data = new DiscordBotService.ErrorNotificationData(
                    error.getId(),
                    project.getId(),
                    error.getErrorType(),
                    error.getErrorMessage(),
                    error.getSeverity().name(),
                    error.getFilePath(),
                    error.getLineNumber(),
                    error.getOccurrenceCount(),
                    error.getAffectedUsersCount(),
                    occurrence.getUrl(),
                    frontendBaseUrl + "/errors/" + error.getId(),
                    occurrence.getSessionId() != null
                            ? frontendBaseUrl + "/replays/" + occurrence.getSessionId()
                            : null
            );

            discordBotService.sendErrorNotification(channelId, data);
            log.info("Discord bot notification sent: errorId={}", error.getId());
            return true;
        } catch (Exception e) {
            log.warn("Discord bot notification failed: {}", e.getMessage());
            return false;
        }
    }

    private void sendViaWebhook(String webhookUrl, Project project, Error error, ErrorOccurrence occurrence) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Discord webhook URL is missing");
            return;
        }

        Map<String, Object> embed = new HashMap<>();
        embed.put("title", NotificationFormatter.getSeverityEmoji(error.getSeverity()) + " " + error.getErrorType());
        embed.put("description", error.getErrorMessage());
        embed.put("color", NotificationFormatter.getDiscordColor(error.getSeverity()));
        embed.put("timestamp", occurrence.getOccurredAt().toString());

        List<Map<String, Object>> fields = List.of(
                Map.of("name", "위치", "value", NotificationFormatter.formatLocation(error), "inline", true),
                Map.of("name", "발생 횟수", "value", error.getOccurrenceCount() + "회", "inline", true),
                Map.of("name", "영향받은 사용자", "value", error.getAffectedUsersCount() + "명", "inline", true),
                Map.of("name", "URL", "value", occurrence.getUrl())
        );
        embed.put("fields", fields);

        Map<String, Object> payload = Map.of("embeds", List.of(embed));

        webClientBuilder.build()
                .post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> log.info("Discord webhook notification sent: errorId={}", error.getId()),
                        e -> log.error("Discord webhook notification failed: errorId={}", error.getId(), e)
                );
    }

    @Override
    public void sendTest(NotificationChannel channel) {
        String webhookUrl = channel.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            throw new IllegalArgumentException("Discord webhook URL is required");
        }

        Map<String, Object> payload = Map.of(
                "embeds", List.of(Map.of(
                        "title", "✅ 웹훅 테스트",
                        "description", "BugShot 웹훅이 정상적으로 작동합니다!",
                        "color", 5814783
                ))
        );

        webClientBuilder.build()
                .post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("Discord test message sent");
    }
}
