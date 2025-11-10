package com.error.monitor.discord;

import com.error.monitor.domain.error.Error;
import com.error.monitor.domain.error.ErrorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.awt.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordButtonHandler extends ListenerAdapter {

    private final ErrorRepository errorRepository;

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        log.info("Button clicked: {} by user: {}", buttonId, event.getUser().getName());

        // Button ID format: "action:errorId"
        String[] parts = buttonId.split(":");
        if (parts.length != 2) {
            event.reply("잘못된 버튼 형식입니다.").setEphemeral(true).queue();
            return;
        }

        String action = parts[0];
        String errorId = parts[1];

        switch (action) {
            case "resolve" -> handleResolve(event, errorId);
            case "ignore" -> handleIgnore(event, errorId);
            case "reopen" -> handleReopen(event, errorId);
            default -> event.reply("알 수 없는 동작입니다.").setEphemeral(true).queue();
        }
    }

    private void handleResolve(ButtonInteractionEvent event, String errorId) {
        try {
            Error error = errorRepository.findById(errorId)
                .orElseThrow(() -> new IllegalArgumentException("Error not found: " + errorId));

            error.resolve(event.getUser().getId());
            errorRepository.save(error);

            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("✅ 에러 해결됨")
                .setDescription("**" + error.getErrorType() + "**가 해결됨으로 표시되었습니다.")
                .setColor(new Color(0x3BA55D))
                .addField("에러 ID", errorId, true)
                .addField("처리자", event.getUser().getAsMention(), true)
                .addField("발생 횟수", error.getOccurrenceCount() + "회", true);

            event.replyEmbeds(embed.build()).queue();
            log.info("Error {} resolved by {}", errorId, event.getUser().getName());
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        } catch (Exception e) {
            log.error("Failed to resolve error: " + errorId, e);
            event.reply("에러 처리 중 문제가 발생했습니다.").setEphemeral(true).queue();
        }
    }

    private void handleIgnore(ButtonInteractionEvent event, String errorId) {
        try {
            Error error = errorRepository.findById(errorId)
                .orElseThrow(() -> new IllegalArgumentException("Error not found: " + errorId));

            error.ignore();
            errorRepository.save(error);

            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⚪ 에러 무시됨")
                .setDescription("**" + error.getErrorType() + "**가 무시됨으로 표시되었습니다.")
                .setColor(new Color(0x99AAB5))
                .addField("에러 ID", errorId, true)
                .addField("처리자", event.getUser().getAsMention(), true)
                .addField("발생 횟수", error.getOccurrenceCount() + "회", true);

            event.replyEmbeds(embed.build()).queue();
            log.info("Error {} ignored by {}", errorId, event.getUser().getName());
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        } catch (Exception e) {
            log.error("Failed to ignore error: " + errorId, e);
            event.reply("에러 처리 중 문제가 발생했습니다.").setEphemeral(true).queue();
        }
    }

    private void handleReopen(ButtonInteractionEvent event, String errorId) {
        try {
            Error error = errorRepository.findById(errorId)
                .orElseThrow(() -> new IllegalArgumentException("Error not found: " + errorId));

            error.reopen();
            errorRepository.save(error);

            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🔄 에러 재오픈됨")
                .setDescription("**" + error.getErrorType() + "**가 재오픈되었습니다.")
                .setColor(new Color(0xFEE75C))
                .addField("에러 ID", errorId, true)
                .addField("처리자", event.getUser().getAsMention(), true)
                .addField("발생 횟수", error.getOccurrenceCount() + "회", true);

            event.replyEmbeds(embed.build()).queue();
            log.info("Error {} reopened by {}", errorId, event.getUser().getName());
        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        } catch (Exception e) {
            log.error("Failed to reopen error: " + errorId, e);
            event.reply("에러 처리 중 문제가 발생했습니다.").setEphemeral(true).queue();
        }
    }
}
