package com.bugshot.domain.notification.discord;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscordBotService {

    @Value("${app.discord.bot-token}")
    private String botToken;

    private final DiscordCommandHandler commandHandler;
    private final DiscordButtonHandler buttonHandler;

    private JDA jda;

    @PostConstruct
    public void init() {
        if (botToken == null || botToken.isBlank()) {
            log.warn("Discord bot token not configured. Bot will not start.");
            return;
        }

        try {
            log.info("Starting Discord bot...");

            jda = JDABuilder.createDefault(botToken)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(commandHandler)
                .addEventListeners(buttonHandler)
                .build()
                .awaitReady();

            // Register slash commands
            registerCommands();

            log.info("Discord bot started successfully! Bot ID: {}", jda.getSelfUser().getId());
        } catch (Exception e) {
            log.error("Failed to start Discord bot", e);
        }
    }

    private void registerCommands() {
        try {
            jda.updateCommands().addCommands(
                Commands.slash("errors", "에러 목록 조회")
                    .addOption(OptionType.STRING, "timerange", "시간 범위 (today, last-hour, last-7d)", false),

                Commands.slash("stats", "프로젝트 통계"),

                Commands.slash("resolve", "에러 해결 표시")
                    .addOption(OptionType.STRING, "errorid", "에러 ID", true),

                Commands.slash("ignore", "에러 무시")
                    .addOption(OptionType.STRING, "errorid", "에러 ID", true),

                Commands.slash("replay", "세션 리플레이 링크")
                    .addOption(OptionType.STRING, "errorid", "에러 ID", true)
            ).queue(
                success -> log.info("Slash commands registered successfully"),
                error -> log.error("Failed to register slash commands", error)
            );
        } catch (Exception e) {
            log.error("Failed to register commands", e);
        }
    }

    /**
     * Discord 채널에 에러 알림 전송
     */
    public void sendErrorNotification(String channelId, ErrorNotificationData data) {
        if (jda == null) {
            log.warn("Discord bot not initialized");
            return;
        }

        try {
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel == null) {
                log.warn("Channel not found: {}", channelId);
                return;
            }

            MessageEmbed embed = createErrorEmbed(data);

            channel.sendMessageEmbeds(embed)
                .setActionRow(
                    Button.link(data.getErrorUrl(), "상세 보기"),
                    data.getReplayUrl() != null ? Button.link(data.getReplayUrl(), "세션 리플레이") : null,
                    Button.success("resolve:" + data.getErrorId(), "해결"),
                    Button.secondary("ignore:" + data.getErrorId(), "무시")
                )
                .queue(
                    success -> log.info("Error notification sent to channel: {}", channelId),
                    error -> log.error("Failed to send notification", error)
                );

        } catch (Exception e) {
            log.error("Failed to send Discord notification", e);
        }
    }

    private MessageEmbed createErrorEmbed(ErrorNotificationData data) {
        Color color = switch (data.getSeverity()) {
            case "CRITICAL" -> new Color(0xED4245);
            case "HIGH" -> new Color(0xFEE75C);
            case "MEDIUM" -> new Color(0x57F287);
            case "LOW" -> new Color(0x99AAB5);
            default -> Color.GRAY;
        };

        String emoji = switch (data.getSeverity()) {
            case "CRITICAL" -> "🔴";
            case "HIGH" -> "🟡";
            case "MEDIUM" -> "🟢";
            case "LOW" -> "⚪";
            default -> "⚫";
        };

        EmbedBuilder builder = new EmbedBuilder()
            .setTitle(emoji + " " + data.getErrorType())
            .setDescription(data.getErrorMessage())
            .setColor(color)
            .addField("📍 위치", formatLocation(data), true)
            .addField("🔢 발생 횟수", data.getOccurrenceCount() + "회", true)
            .addField("👥 영향받은 사용자", data.getAffectedUsers() + "명", true)
            .addField("🌐 URL", data.getUrl(), false)
            .setTimestamp(java.time.Instant.now())
            .setFooter("BugShot", null);

        return builder.build();
    }

    private String formatLocation(ErrorNotificationData data) {
        if (data.getFilePath() != null && data.getLineNumber() != null) {
            return data.getFilePath() + ":" + data.getLineNumber();
        } else if (data.getFilePath() != null) {
            return data.getFilePath();
        }
        return "Unknown";
    }

    @PreDestroy
    public void shutdown() {
        if (jda != null) {
            log.info("Shutting down Discord bot...");
            jda.shutdown();
            try {
                jda.awaitShutdown(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Error while shutting down Discord bot", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 에러 알림 데이터 DTO
     */
    public static class ErrorNotificationData {
        private final String errorId;
        private final String projectId;
        private final String errorType;
        private final String errorMessage;
        private final String severity;
        private final String filePath;
        private final Integer lineNumber;
        private final int occurrenceCount;
        private final int affectedUsers;
        private final String url;
        private final String errorUrl;
        private final String replayUrl;

        public ErrorNotificationData(String errorId, String projectId, String errorType,
                                       String errorMessage, String severity, String filePath,
                                       Integer lineNumber, int occurrenceCount, int affectedUsers,
                                       String url, String errorUrl, String replayUrl) {
            this.errorId = errorId;
            this.projectId = projectId;
            this.errorType = errorType;
            this.errorMessage = errorMessage;
            this.severity = severity;
            this.filePath = filePath;
            this.lineNumber = lineNumber;
            this.occurrenceCount = occurrenceCount;
            this.affectedUsers = affectedUsers;
            this.url = url;
            this.errorUrl = errorUrl;
            this.replayUrl = replayUrl;
        }

        // Getters
        public String getErrorId() { return errorId; }
        public String getProjectId() { return projectId; }
        public String getErrorType() { return errorType; }
        public String getErrorMessage() { return errorMessage; }
        public String getSeverity() { return severity; }
        public String getFilePath() { return filePath; }
        public Integer getLineNumber() { return lineNumber; }
        public int getOccurrenceCount() { return occurrenceCount; }
        public int getAffectedUsers() { return affectedUsers; }
        public String getUrl() { return url; }
        public String getErrorUrl() { return errorUrl; }
        public String getReplayUrl() { return replayUrl; }
    }
}
