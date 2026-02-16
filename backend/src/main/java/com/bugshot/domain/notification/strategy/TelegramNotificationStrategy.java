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

import java.util.Map;

/**
 * Telegram 알림 전송 전략
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationStrategy implements NotificationStrategy {

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendMessage";

    private final WebClient.Builder webClientBuilder;

    @Override
    public NotificationChannel.ChannelType getChannelType() {
        return NotificationChannel.ChannelType.TELEGRAM;
    }

    @Override
    public void send(NotificationChannel channel, Project project, Error error, ErrorOccurrence occurrence) {
        String botToken = channel.getBotToken();
        String chatId = channel.getChatId();

        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            log.warn("Telegram bot token or chat ID is missing");
            return;
        }

        String message = buildMessage(project, error, occurrence);
        String apiUrl = String.format(TELEGRAM_API_URL, botToken);

        Map<String, Object> payload = Map.of(
                "chat_id", chatId,
                "text", message,
                "parse_mode", "HTML"
        );

        webClientBuilder.build()
                .post()
                .uri(apiUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> log.info("Telegram notification sent: errorId={}", error.getId()),
                        e -> log.error("Telegram notification failed: errorId={}", error.getId(), e)
                );
    }

    private String buildMessage(Project project, Error error, ErrorOccurrence occurrence) {
        String emoji = NotificationFormatter.getSeverityEmoji(error.getSeverity());
        String location = NotificationFormatter.formatLocation(error);

        return String.format("""
                %s <b>%s</b>

                <b>프로젝트:</b> %s
                <b>에러:</b> %s
                <b>위치:</b> %s
                <b>발생 횟수:</b> %d회
                <b>URL:</b> %s
                """,
                emoji,
                error.getSeverity().name(),
                project.getName(),
                error.getErrorMessage(),
                location,
                error.getOccurrenceCount(),
                occurrence.getUrl()
        );
    }

    @Override
    public void sendTest(NotificationChannel channel) {
        String botToken = channel.getBotToken();
        String chatId = channel.getChatId();

        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            throw new IllegalArgumentException("Telegram bot token and chat ID are required");
        }

        String apiUrl = String.format(TELEGRAM_API_URL, botToken);
        Map<String, Object> payload = Map.of(
                "chat_id", chatId,
                "text", "✅ BugShot 텔레그램 알림 테스트 메시지입니다."
        );

        webClientBuilder.build()
                .post()
                .uri(apiUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("Telegram test message sent");
    }
}
