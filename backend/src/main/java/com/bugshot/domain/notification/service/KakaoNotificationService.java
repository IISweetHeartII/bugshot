package com.bugshot.domain.notification.service;

import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.entity.ErrorOccurrence;
import com.bugshot.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoNotificationService {

    private final WebClient.Builder webClientBuilder;

    public void sendErrorNotification(String webhookUrl, Project project, Error error, ErrorOccurrence occurrence) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Kakao Work webhook URL is missing");
            return;
        }

        // Kakao Work Webhook Payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", "🚨 [" + project.getName() + "] 에러 발생: " + error.getErrorType());

        List<Map<String, Object>> blocks = new ArrayList<>();

        // Header
        blocks.add(createHeaderBlock(error.getErrorType()));

        // Error Message
        blocks.add(createTextBlock(error.getErrorMessage()));

        // Divider
        blocks.add(createDividerBlock());

        // Fields
        blocks.add(createDescriptionBlock(
            "프로젝트", project.getName(),
            "발생 횟수", error.getOccurrenceCount() + "회"
        ));

        blocks.add(createDescriptionBlock(
            "위치", formatLocation(error),
            "심각도", error.getSeverity().name()
        ));

        // Link
        if (occurrence.getUrl() != null) {
            blocks.add(createContextBlock("URL: " + occurrence.getUrl()));
        }

        payload.put("blocks", blocks);

        webClientBuilder.build()
            .post()
            .uri(webhookUrl)
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(String.class)
            .subscribe(
                response -> log.info("Kakao Work notification sent successfully"),
                err -> log.error("Failed to send Kakao Work notification", err)
            );
    }

    private Map<String, Object> createHeaderBlock(String text) {
        Map<String, Object> block = new HashMap<>();
        block.put("type", "header");
        block.put("text", text);
        block.put("style", "red");
        return block;
    }

    private Map<String, Object> createTextBlock(String text) {
        Map<String, Object> block = new HashMap<>();
        block.put("type", "text");
        block.put("text", text);
        return block;
    }

    private Map<String, Object> createDividerBlock() {
        Map<String, Object> block = new HashMap<>();
        block.put("type", "divider");
        return block;
    }

    private Map<String, Object> createDescriptionBlock(String term1, String desc1, String term2, String desc2) {
        Map<String, Object> block = new HashMap<>();
        block.put("type", "description");
        block.put("term", term1);

        Map<String, Object> content = new HashMap<>();
        content.put("type", "text");
        content.put("text", desc1);
        content.put("markdown", false);
        block.put("content", content);

        block.put("accent", true);

        return block;
    }

    private Map<String, Object> createContextBlock(String text) {
        Map<String, Object> block = new HashMap<>();
        block.put("type", "context");
        block.put("content", Map.of("type", "text", "text", text, "markdown", false));
        return block;
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
