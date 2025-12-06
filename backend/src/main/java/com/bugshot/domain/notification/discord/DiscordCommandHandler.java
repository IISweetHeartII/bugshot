package com.bugshot.domain.notification.discord;

import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.repository.ErrorRepository;
import com.bugshot.domain.project.entity.Project;
import com.bugshot.domain.project.repository.ProjectRepository;
import com.bugshot.domain.replay.service.SessionReplayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordCommandHandler extends ListenerAdapter {

    private final ErrorRepository errorRepository;
    private final ProjectRepository projectRepository;

    @Autowired(required = false)
    private SessionReplayService sessionReplayService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        log.info("Received command: {} from user: {}", command, event.getUser().getName());

        switch (command) {
            case "errors" -> handleErrorsCommand(event);
            case "stats" -> handleStatsCommand(event);
            case "resolve" -> handleResolveCommand(event);
            case "ignore" -> handleIgnoreCommand(event);
            case "replay" -> handleReplayCommand(event);
            default -> event.reply("알 수 없는 명령어입니다.").setEphemeral(true).queue();
        }
    }

    private void handleErrorsCommand(SlashCommandInteractionEvent event) {
        String timeRange = event.getOption("timerange") != null ?
            event.getOption("timerange").getAsString() : "today";

        try {
            // Get errors grouped by severity
            List<Error> errors = errorRepository.findTop10ByOrderByPriorityScoreDesc();

            long criticalCount = errors.stream().filter(e -> e.getSeverity().equals("CRITICAL")).count();
            long highCount = errors.stream().filter(e -> e.getSeverity().equals("HIGH")).count();
            long mediumCount = errors.stream().filter(e -> e.getSeverity().equals("MEDIUM")).count();

            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📊 에러 현황")
                .setDescription("시간 범위: " + timeRange)
                .setColor(new Color(0x5865F2))
                .addField("🔴 Critical", criticalCount + "건", true)
                .addField("🟡 High", highCount + "건", true)
                .addField("🟢 Medium", mediumCount + "건", true)
                .addField("\u200B", "\u200B", false);  // Empty field for spacing

            // Add top 3 errors
            int count = 1;
            for (Error error : errors.subList(0, Math.min(3, errors.size()))) {
                String emoji = getSeverityEmoji(error.getSeverity());
                embed.addField(
                    count + ". " + emoji + " " + error.getErrorType(),
                    error.getOccurrenceCount() + "회 발생 • " + error.getAffectedUsersCount() + "명 영향",
                    false
                );
                count++;
            }

            embed.setTimestamp(Instant.now())
                .setFooter("ErrorWatch", null);

            event.replyEmbeds(embed.build()).queue();
        } catch (Exception e) {
            log.error("Failed to fetch errors", e);
            event.reply("에러 조회 중 문제가 발생했습니다.").setEphemeral(true).queue();
        }
    }

    private String getSeverityEmoji(Error.Severity severity) {
        return switch (severity) {
            case CRITICAL -> "🔴";
            case HIGH -> "🟡";
            case MEDIUM -> "🟢";
            case LOW -> "⚪";
        };
    }

    private void handleStatsCommand(SlashCommandInteractionEvent event) {
        try {
            // Get statistics
            List<Error> allErrors = errorRepository.findAll();
            long totalErrors = allErrors.stream().mapToLong(Error::getOccurrenceCount).sum();
            long totalAffectedUsers = allErrors.stream()
                .mapToLong(Error::getAffectedUsersCount)
                .sum();

            // Find most common error
            Error topError = allErrors.stream()
                .max((e1, e2) -> Integer.compare(e1.getOccurrenceCount(), e2.getOccurrenceCount()))
                .orElse(null);

            // Find latest error
            Error latestError = errorRepository.findTopByOrderByLastSeenAtDesc()
                .orElse(null);

            String lastErrorTime = latestError != null ?
                formatRelativeTime(latestError.getLastSeenAt()) : "N/A";

            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📈 프로젝트 통계")
                .setColor(new Color(0x3BA55D))
                .addField("총 에러 수", String.format("%,d건", totalErrors), true)
                .addField("영향받은 사용자", String.format("%,d명", totalAffectedUsers), true)
                .addField("마지막 에러", lastErrorTime, true)
                .addField("\u200B", "\u200B", false);

            if (topError != null) {
                embed.addField("가장 많은 에러",
                    topError.getErrorType() + " (" + topError.getOccurrenceCount() + "회)",
                    false);
            }

            embed.setTimestamp(Instant.now())
                .setFooter("ErrorWatch", null);

            event.replyEmbeds(embed.build()).queue();
        } catch (Exception e) {
            log.error("Failed to fetch statistics", e);
            event.reply("통계 조회 중 문제가 발생했습니다.").setEphemeral(true).queue();
        }
    }

    private String formatRelativeTime(java.time.LocalDateTime dateTime) {
        java.time.Duration duration = java.time.Duration.between(dateTime, java.time.LocalDateTime.now());
        long minutes = duration.toMinutes();

        if (minutes < 1) return "방금 전";
        if (minutes < 60) return minutes + "분 전";

        long hours = duration.toHours();
        if (hours < 24) return hours + "시간 전";

        long days = duration.toDays();
        return days + "일 전";
    }

    private void handleResolveCommand(SlashCommandInteractionEvent event) {
        String errorId = event.getOption("errorid") != null ?
            event.getOption("errorid").getAsString() : null;

        if (errorId == null) {
            event.reply("에러 ID를 입력해주세요.").setEphemeral(true).queue();
            return;
        }

        try {
            Error error = errorRepository.findById(errorId)
                .orElseThrow(() -> new IllegalArgumentException("Error not found: " + errorId));

            error.resolve(event.getUser().getId());
            errorRepository.save(error);

            event.reply("✅ 에러가 해결됨으로 표시되었습니다: " + error.getErrorType()).queue();
            log.info("Resolving error: {} by {}", errorId, event.getUser().getName());
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        } catch (Exception e) {
            log.error("Failed to resolve error: " + errorId, e);
            event.reply("에러 처리 중 문제가 발생했습니다.").setEphemeral(true).queue();
        }
    }

    private void handleIgnoreCommand(SlashCommandInteractionEvent event) {
        String errorId = event.getOption("errorid") != null ?
            event.getOption("errorid").getAsString() : null;

        if (errorId == null) {
            event.reply("에러 ID를 입력해주세요.").setEphemeral(true).queue();
            return;
        }

        try {
            Error error = errorRepository.findById(errorId)
                .orElseThrow(() -> new IllegalArgumentException("Error not found: " + errorId));

            error.ignore();
            errorRepository.save(error);

            event.reply("⚪ 에러가 무시됨으로 표시되었습니다: " + error.getErrorType()).queue();
            log.info("Ignoring error: {} by {}", errorId, event.getUser().getName());
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        } catch (Exception e) {
            log.error("Failed to ignore error: " + errorId, e);
            event.reply("에러 처리 중 문제가 발생했습니다.").setEphemeral(true).queue();
        }
    }

    private void handleReplayCommand(SlashCommandInteractionEvent event) {
        String errorId = event.getOption("errorid") != null ?
            event.getOption("errorid").getAsString() : null;

        if (errorId == null) {
            event.reply("에러 ID를 입력해주세요.").setEphemeral(true).queue();
            return;
        }

        try {
            // SessionReplayService가 없으면 기본 URL 반환
            if (sessionReplayService == null) {
                String replayUrl = frontendBaseUrl + "/replays/" + errorId;
                event.reply("🎬 세션 리플레이: " + replayUrl).queue();
                return;
            }

            // 실제 세션 리플레이 조회
            var replay = sessionReplayService.getSessionReplay(errorId);

            // Pre-signed URL 생성 (24시간 유효)
            String downloadUrl = sessionReplayService.generateDownloadUrl(errorId, 86400);

            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🎬 세션 리플레이")
                .setColor(new Color(0x5865F2))
                .addField("에러 ID", errorId, true)
                .addField("녹화 시간", replay.getRecordedAt().toString(), true)
                .addField("재생 길이", replay.getDuration() + "초", true)
                .addField("파일 크기", formatFileSize(replay.getSize()), true)
                .addField("브라우저", replay.getUserInfo().getBrowser() != null ? replay.getUserInfo().getBrowser() : "N/A", true)
                .addField("OS", replay.getUserInfo().getOs() != null ? replay.getUserInfo().getOs() : "N/A", true)
                .addField("프론트엔드에서 보기", frontendBaseUrl + "/replays/" + errorId, false)
                .addField("다운로드 링크", "[여기를 클릭하세요](" + downloadUrl + ")\n(24시간 유효)", false)
                .setTimestamp(Instant.now())
                .setFooter("ErrorWatch", null);

            event.replyEmbeds(embed.build()).queue();

        } catch (IllegalArgumentException e) {
            event.reply("❌ 해당 에러에 대한 세션 리플레이를 찾을 수 없습니다.").setEphemeral(true).queue();
            log.warn("Session replay not found for error: {}", errorId);
        } catch (Exception e) {
            log.error("Failed to fetch session replay for error: " + errorId, e);
            event.reply("세션 리플레이 조회 중 문제가 발생했습니다.").setEphemeral(true).queue();
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
