package com.bugshot.domain.notification.service;

import com.bugshot.domain.common.util.NotificationFormatter;
import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.entity.ErrorOccurrence;
import com.bugshot.domain.notification.entity.NotificationChannel;
import com.bugshot.domain.notification.repository.NotificationChannelRepository;
import com.bugshot.domain.notification.strategy.NotificationStrategy;
import com.bugshot.domain.notification.strategy.NotificationStrategyRegistry;
import com.bugshot.domain.project.entity.Project;
import com.bugshot.domain.webhook.entity.WebhookConfig;
import com.bugshot.domain.webhook.repository.WebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 알림 전송 서비스 (Strategy Pattern 적용)
 * <p>
 * 기존의 거대한 switch문 대신 Strategy Pattern을 사용하여
 * 각 채널별 알림 로직을 분리했습니다.
 * </p>
 *
 * <pre>
 * Before (switch문):
 *   switch (channel.getChannelType()) {
 *       case DISCORD -> sendDiscordNotification(...);
 *       case SLACK -> sendSlackNotification(...);
 *       // 새 채널 추가 시 여기 수정 필요
 *   }
 *
 * After (Strategy Pattern):
 *   strategyRegistry.getStrategy(channelType).send(...);
 *   // 새 채널 추가 시 새 Strategy 클래스만 생성
 * </pre>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationChannelRepository channelRepository;
    private final NotificationStrategyRegistry strategyRegistry;
    private final WebhookConfigRepository webhookConfigRepository;
    private final WebClient.Builder webClientBuilder;

    /**
     * 에러 발생 시 모든 활성화된 채널로 알림 전송
     * <p>
     * NotificationChannel과 WebhookConfig 두 가지 소스에서 알림을 전송합니다.
     * </p>
     */
    @Async
    @Transactional
    public void notifyError(Project project, Error error, ErrorOccurrence occurrence) {
        log.info("Sending notifications: projectId={}, errorId={}", project.getId(), error.getId());

        int sentCount = 0;
        int failedCount = 0;

        // 1. NotificationChannel을 통한 알림 전송 (기존 로직)
        List<NotificationChannel> channels = channelRepository.findByProjectIdAndEnabled(
                project.getId(), true
        );

        for (NotificationChannel channel : channels) {
            if (!channel.shouldNotify(error.getSeverity())) {
                log.debug("Skipping channel {} - severity {} below threshold {}",
                        channel.getChannelType(), error.getSeverity(), channel.getMinSeverity());
                continue;
            }

            try {
                sendNotification(channel, project, error, occurrence);
                channel.incrementNotificationCount();
                channelRepository.save(channel);
                sentCount++;
            } catch (Exception e) {
                log.error("Failed to send notification via {}: {}",
                        channel.getChannelType(), e.getMessage());
                failedCount++;
            }
        }

        // 2. WebhookConfig를 통한 알림 전송 (프론트엔드에서 설정한 웹훅)
        List<WebhookConfig> webhooks = webhookConfigRepository.findByProjectIdAndEnabledTrue(
                project.getId()
        );

        log.info("Found {} enabled webhooks for project: {}", webhooks.size(), project.getId());

        for (WebhookConfig webhook : webhooks) {
            try {
                sendWebhookNotification(webhook, project, error, occurrence);
                webhook.recordSuccess();
                webhookConfigRepository.save(webhook);
                sentCount++;
                log.info("Webhook notification sent: webhookId={}, type={}", webhook.getId(), webhook.getType());
            } catch (Exception e) {
                log.error("Failed to send webhook notification: webhookId={}, error={}",
                        webhook.getId(), e.getMessage());
                webhook.recordFailure();
                webhookConfigRepository.save(webhook);
                failedCount++;
            }
        }

        log.info("Notifications completed: errorId={}, sent={}, failed={}",
                error.getId(), sentCount, failedCount);
    }

    /**
     * Strategy Pattern을 사용한 알림 전송
     * <p>
     * switch문 대신 Registry에서 적절한 Strategy를 찾아 실행합니다.
     * </p>
     */
    private void sendNotification(NotificationChannel channel,
                                   Project project,
                                   Error error,
                                   ErrorOccurrence occurrence) {
        strategyRegistry.getStrategy(channel.getChannelType())
                .ifPresentOrElse(
                        strategy -> {
                            log.debug("Using {} strategy for channel",
                                    strategy.getChannelType());
                            strategy.send(channel, project, error, occurrence);
                        },
                        () -> log.warn("No strategy found for channel type: {}",
                                channel.getChannelType())
                );
    }

    /**
     * 특정 채널로 테스트 메시지 전송
     */
    public void sendTestNotification(NotificationChannel channel) {
        NotificationStrategy strategy = strategyRegistry.getStrategyOrThrow(channel.getChannelType());
        strategy.sendTest(channel);
        log.info("Test notification sent via {}", channel.getChannelType());
    }

    /**
     * WebhookConfig를 통한 알림 전송
     * <p>
     * 프론트엔드에서 설정한 웹훅(WebhookConfig)을 통해 알림을 전송합니다.
     * 각 웹훅 타입(DISCORD, SLACK, TELEGRAM, CUSTOM)에 맞는 형식으로 페이로드를 생성합니다.
     * </p>
     */
    private void sendWebhookNotification(WebhookConfig webhook, Project project,
                                          Error error, ErrorOccurrence occurrence) {
        Map<String, Object> payload = switch (webhook.getType()) {
            case DISCORD -> createDiscordPayload(project, error, occurrence);
            case SLACK -> createSlackPayload(project, error, occurrence);
            case TELEGRAM -> createTelegramPayload(project, error, occurrence);
            case CUSTOM -> createCustomPayload(project, error, occurrence);
        };

        webClientBuilder.build()
                .post()
                .uri(webhook.getWebhookUrl())
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 동기 처리로 성공/실패 여부 확인
    }

    /**
     * Discord Embed 형식의 페이로드 생성
     */
    private Map<String, Object> createDiscordPayload(Project project, Error error, ErrorOccurrence occurrence) {
        Map<String, Object> embed = new HashMap<>();
        embed.put("title", NotificationFormatter.getSeverityEmoji(error.getSeverity()) + " " + error.getErrorType());
        embed.put("description", truncateMessage(error.getErrorMessage(), 200));
        embed.put("color", NotificationFormatter.getDiscordColor(error.getSeverity()));
        embed.put("timestamp", occurrence.getOccurredAt().toString());

        List<Map<String, Object>> fields = List.of(
                Map.of("name", "프로젝트", "value", project.getName(), "inline", true),
                Map.of("name", "위치", "value", NotificationFormatter.formatLocation(error), "inline", true),
                Map.of("name", "발생 횟수", "value", error.getOccurrenceCount() + "회", "inline", true),
                Map.of("name", "영향받은 사용자", "value", error.getAffectedUsersCount() + "명", "inline", true),
                Map.of("name", "URL", "value", occurrence.getUrl() != null ? occurrence.getUrl() : "N/A", "inline", false)
        );
        embed.put("fields", fields);

        Map<String, Object> footer = new HashMap<>();
        footer.put("text", "BugShot Error Monitoring");
        embed.put("footer", footer);

        return Map.of("embeds", List.of(embed));
    }

    /**
     * Slack Block 형식의 페이로드 생성
     */
    private Map<String, Object> createSlackPayload(Project project, Error error, ErrorOccurrence occurrence) {
        String severityEmoji = NotificationFormatter.getSeverityEmoji(error.getSeverity());
        String color = NotificationFormatter.getSlackColor(error.getSeverity());

        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", color);
        attachment.put("title", severityEmoji + " " + error.getErrorType());
        attachment.put("text", truncateMessage(error.getErrorMessage(), 200));
        attachment.put("fields", List.of(
                Map.of("title", "프로젝트", "value", project.getName(), "short", true),
                Map.of("title", "위치", "value", NotificationFormatter.formatLocation(error), "short", true),
                Map.of("title", "발생 횟수", "value", error.getOccurrenceCount() + "회", "short", true),
                Map.of("title", "영향받은 사용자", "value", error.getAffectedUsersCount() + "명", "short", true)
        ));
        attachment.put("footer", "BugShot Error Monitoring");
        attachment.put("ts", occurrence.getOccurredAt().toEpochSecond(java.time.ZoneOffset.UTC));

        return Map.of(
                "text", severityEmoji + " [" + project.getName() + "] 에러 발생: " + error.getErrorType(),
                "attachments", List.of(attachment)
        );
    }

    /**
     * Telegram 형식의 페이로드 생성
     */
    private Map<String, Object> createTelegramPayload(Project project, Error error, ErrorOccurrence occurrence) {
        String severityEmoji = NotificationFormatter.getSeverityEmoji(error.getSeverity());
        String message = String.format(
                "%s *[%s] 에러 발생*\n\n" +
                "*에러 타입:* %s\n" +
                "*메시지:* %s\n" +
                "*위치:* %s\n" +
                "*발생 횟수:* %d회\n" +
                "*영향받은 사용자:* %d명\n" +
                "*URL:* %s",
                severityEmoji,
                escapeMarkdown(project.getName()),
                escapeMarkdown(error.getErrorType()),
                escapeMarkdown(truncateMessage(error.getErrorMessage(), 150)),
                escapeMarkdown(NotificationFormatter.formatLocation(error)),
                error.getOccurrenceCount(),
                error.getAffectedUsersCount(),
                occurrence.getUrl() != null ? occurrence.getUrl() : "N/A"
        );

        return Map.of(
                "text", message,
                "parse_mode", "Markdown"
        );
    }

    /**
     * Custom Webhook 형식의 페이로드 생성 (JSON 원본 데이터)
     */
    private Map<String, Object> createCustomPayload(Project project, Error error, ErrorOccurrence occurrence) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "error.occurred");
        payload.put("timestamp", occurrence.getOccurredAt().toString());

        Map<String, Object> projectData = new HashMap<>();
        projectData.put("id", project.getId());
        projectData.put("name", project.getName());
        payload.put("project", projectData);

        Map<String, Object> errorData = new HashMap<>();
        errorData.put("id", error.getId());
        errorData.put("type", error.getErrorType());
        errorData.put("message", error.getErrorMessage());
        errorData.put("severity", error.getSeverity().name());
        errorData.put("filePath", error.getFilePath());
        errorData.put("lineNumber", error.getLineNumber());
        errorData.put("occurrenceCount", error.getOccurrenceCount());
        errorData.put("affectedUsersCount", error.getAffectedUsersCount());
        payload.put("error", errorData);

        Map<String, Object> occurrenceData = new HashMap<>();
        occurrenceData.put("url", occurrence.getUrl());
        occurrenceData.put("userAgent", occurrence.getUserAgent());
        occurrenceData.put("browser", occurrence.getBrowser());
        occurrenceData.put("os", occurrence.getOs());
        payload.put("occurrence", occurrenceData);

        return payload;
    }

    /**
     * 메시지 길이 제한
     */
    private String truncateMessage(String message, int maxLength) {
        if (message == null) return "N/A";
        if (message.length() <= maxLength) return message;
        return message.substring(0, maxLength - 3) + "...";
    }

    /**
     * Telegram Markdown 이스케이프
     */
    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_")
                   .replace("*", "\\*")
                   .replace("[", "\\[")
                   .replace("`", "\\`");
    }
}
