package com.error.monitor.service;

import com.error.monitor.discord.DiscordBotService;
import com.error.monitor.domain.error.Error;
import com.error.monitor.domain.error.ErrorOccurrence;
import com.error.monitor.domain.notification.NotificationChannel;
import com.error.monitor.domain.notification.NotificationChannelRepository;
import com.error.monitor.domain.project.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationChannelRepository channelRepository;
    private final WebClient.Builder webClientBuilder;

    @Autowired(required = false)
    private DiscordBotService discordBotService;

    @Async
    public void notifyError(Project project, Error error, ErrorOccurrence occurrence) {
        log.info("Sending notifications for error: project={}, error={}", project.getId(), error.getId());

        List<NotificationChannel> channels = channelRepository.findByProjectIdAndEnabled(
            project.getId(), true
        );

        for (NotificationChannel channel : channels) {
            try {
                if (channel.shouldNotify(error.getSeverity())) {
                    sendNotification(channel, project, error, occurrence);
                    channel.incrementNotificationCount();
                    channelRepository.save(channel);
                }
            } catch (Exception e) {
                log.error("Failed to send notification via " + channel.getChannelType(), e);
            }
        }
    }

    private void sendNotification(NotificationChannel channel,
                                    Project project,
                                    Error error,
                                    ErrorOccurrence occurrence) {
        switch (channel.getChannelType()) {
            case DISCORD -> sendDiscordNotification(channel, project, error, occurrence);
            case SLACK -> sendSlackNotification(channel, project, error, occurrence);
            case WEBHOOK -> sendWebhookNotification(channel, project, error, occurrence);
            default -> log.warn("Unsupported channel type: {}", channel.getChannelType());
        }
    }

    private void sendDiscordNotification(NotificationChannel channel,
                                           Project project,
                                           Error error,
                                           ErrorOccurrence occurrence) {
        // Try Discord Bot first if channel ID is configured
        String channelId = (String) channel.getConfig().get("channelId");
        if (channelId != null && discordBotService != null) {
            try {
                // Use Discord Bot with interactive buttons
                DiscordBotService.ErrorNotificationData data = new DiscordBotService.ErrorNotificationData(
                    error.getId().toString(),
                    project.getId().toString(),
                    error.getErrorType(),
                    error.getErrorMessage(),
                    error.getSeverity().name(),
                    error.getFilePath(),
                    error.getLineNumber(),
                    error.getOccurrenceCount(),
                    error.getAffectedUsersCount(),
                    occurrence.getUrl(),
                    "https://errorwatch.io/errors/" + error.getId(), // TODO: Use actual frontend URL
                    null // TODO: Add replay URL if available
                );

                discordBotService.sendErrorNotification(channelId, data);
                log.info("Discord bot notification sent successfully");
                return;
            } catch (Exception e) {
                log.warn("Failed to send Discord bot notification, falling back to webhook", e);
            }
        }

        // Fallback to webhook
        String webhookUrl = channel.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Discord webhook URL is missing");
            return;
        }

        // Create Discord embed
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", getSeverityEmoji(error.getSeverity()) + " " + error.getErrorType());
        embed.put("description", error.getErrorMessage());
        embed.put("color", getSeverityColor(error.getSeverity()));
        embed.put("timestamp", occurrence.getOccurredAt().toString());

        List<Map<String, Object>> fields = List.of(
            Map.of("name", "위치", "value", formatLocation(error), "inline", true),
            Map.of("name", "발생 횟수", "value", error.getOccurrenceCount() + "회", "inline", true),
            Map.of("name", "영향받은 사용자", "value", error.getAffectedUsersCount() + "명", "inline", true),
            Map.of("name", "URL", "value", occurrence.getUrl())
        );
        embed.put("fields", fields);

        Map<String, Object> payload = Map.of(
            "embeds", List.of(embed)
        );

        // Send to Discord
        webClientBuilder.build()
            .post()
            .uri(webhookUrl)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(String.class)
            .subscribe(
                response -> log.info("Discord notification sent successfully"),
                error1 -> log.error("Failed to send Discord notification", error1)
            );
    }

    private void sendSlackNotification(NotificationChannel channel,
                                         Project project,
                                         Error error,
                                         ErrorOccurrence occurrence) {
        String webhookUrl = channel.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Slack webhook URL is missing");
            return;
        }

        // Create Slack message
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", getSlackColor(error.getSeverity()));
        attachment.put("title", error.getErrorType());
        attachment.put("text", error.getErrorMessage());
        attachment.put("fields", List.of(
            Map.of("title", "발생 횟수", "value", error.getOccurrenceCount() + "회", "short", true),
            Map.of("title", "위치", "value", formatLocation(error), "short", true)
        ));

        Map<String, Object> payload = Map.of(
            "text", getSeverityEmoji(error.getSeverity()) + " 에러 발생",
            "attachments", List.of(attachment)
        );

        // Send to Slack
        webClientBuilder.build()
            .post()
            .uri(webhookUrl)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(String.class)
            .subscribe(
                response -> log.info("Slack notification sent successfully"),
                error1 -> log.error("Failed to send Slack notification", error1)
            );
    }

    private void sendWebhookNotification(NotificationChannel channel,
                                          Project project,
                                          Error error,
                                          ErrorOccurrence occurrence) {
        String webhookUrl = channel.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Webhook URL is missing");
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
            "timestamp", occurrence.getOccurredAt().toString()
        );

        webClientBuilder.build()
            .post()
            .uri(webhookUrl)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(String.class)
            .subscribe(
                response -> log.info("Webhook notification sent successfully"),
                error1 -> log.error("Failed to send webhook notification", error1)
            );
    }

    private String getSeverityEmoji(Error.Severity severity) {
        return switch (severity) {
            case CRITICAL -> "🔴";
            case HIGH -> "🟡";
            case MEDIUM -> "🟢";
            case LOW -> "⚪";
        };
    }

    private int getSeverityColor(Error.Severity severity) {
        return switch (severity) {
            case CRITICAL -> 0xED4245; // Red
            case HIGH -> 0xFEE75C;     // Yellow
            case MEDIUM -> 0x57F287;   // Green
            case LOW -> 0x99AAB5;      // Gray
        };
    }

    private String getSlackColor(Error.Severity severity) {
        return switch (severity) {
            case CRITICAL -> "danger";
            case HIGH -> "warning";
            case MEDIUM -> "good";
            case LOW -> "#99AAB5";
        };
    }

    private String formatLocation(Error error) {
        if (error.getFilePath() != null && error.getLineNumber() != null) {
            return error.getFilePath() + ":" + error.getLineNumber();
        } else if (error.getFilePath() != null) {
            return error.getFilePath();
        }
        return "Unknown";
    }
}
