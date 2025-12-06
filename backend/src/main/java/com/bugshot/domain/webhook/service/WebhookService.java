package com.bugshot.domain.webhook.service;

import com.bugshot.domain.project.entity.Project;
import com.bugshot.domain.project.repository.ProjectRepository;
import com.bugshot.domain.webhook.dto.WebhookConfigRequest;
import com.bugshot.domain.webhook.dto.WebhookConfigResponse;
import com.bugshot.domain.webhook.entity.WebhookConfig;
import com.bugshot.domain.webhook.repository.WebhookConfigRepository;
import com.bugshot.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 웹훅 설정 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebhookConfigRepository webhookRepository;
    private final ProjectRepository projectRepository;
    private final WebClient webClient;

    /**
     * 프로젝트의 웹훅 목록 조회
     */
    @Transactional(readOnly = true)
    public List<WebhookConfigResponse> getProjectWebhooks(String projectId) {
        List<WebhookConfig> configs = webhookRepository.findByProjectId(projectId);
        return configs.stream()
                .map(WebhookConfigResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 웹훅 생성
     */
    @Transactional
    public WebhookConfigResponse createWebhook(WebhookConfigRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("프로젝트", request.getProjectId()));

        WebhookConfig config = WebhookConfig.builder()
                .project(project)
                .type(convertType(request.getType()))
                .name(request.getName())
                .webhookUrl(request.getWebhookUrl())
                .enabled(request.isEnabled())
                .severityFilters(request.getSeverityFilters())
                .environmentFilters(request.getEnvironmentFilters())
                .totalSent(0L)
                .failureCount(0L)
                .build();

        config = webhookRepository.save(config);
        log.info("Webhook created: id={}, projectId={}, type={}",
                config.getId(), project.getId(), config.getType());

        return WebhookConfigResponse.from(config);
    }

    /**
     * 웹훅 수정
     */
    @Transactional
    public WebhookConfigResponse updateWebhook(String id, WebhookConfigRequest request) {
        WebhookConfig config = webhookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("웹훅", id));

        config.updateConfig(
                request.getName(),
                request.getWebhookUrl(),
                request.isEnabled(),
                request.getSeverityFilters(),
                request.getEnvironmentFilters()
        );

        config = webhookRepository.save(config);
        log.info("Webhook updated: id={}", id);

        return WebhookConfigResponse.from(config);
    }

    /**
     * 웹훅 삭제
     */
    @Transactional
    public void deleteWebhook(String id) {
        WebhookConfig config = webhookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("웹훅", id));

        webhookRepository.delete(config);
        log.info("Webhook deleted: id={}", id);
    }

    /**
     * 웹훅 테스트 전송
     */
    public String testWebhook(String id) {
        WebhookConfig config = webhookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("웹훅", id));

        try {
            sendTestMessage(config);
            return "테스트 메시지가 성공적으로 전송되었습니다.";
        } catch (Exception e) {
            log.error("Failed to send test webhook: id={}", id, e);
            throw new RuntimeException("웹훅 테스트 전송에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 테스트 메시지 전송
     */
    private void sendTestMessage(WebhookConfig config) {
        Map<String, Object> payload = switch (config.getType()) {
            case DISCORD -> createDiscordPayload("✅ 웹훅 테스트", "BugShot 웹훅이 정상적으로 작동합니다!");
            case SLACK -> createSlackPayload("✅ 웹훅 테스트", "BugShot 웹훅이 정상적으로 작동합니다!");
            default -> Map.of("text", "✅ BugShot 웹훅 테스트");
        };

        webClient.post()
                .uri(config.getWebhookUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Discord 페이로드 생성
     */
    private Map<String, Object> createDiscordPayload(String title, String description) {
        return Map.of(
                "embeds", List.of(
                        Map.of(
                                "title", title,
                                "description", description,
                                "color", 5814783, // Blue
                                "timestamp", LocalDateTime.now().toString()
                        )
                )
        );
    }

    /**
     * Slack 페이로드 생성
     */
    private Map<String, Object> createSlackPayload(String title, String text) {
        return Map.of(
                "text", title,
                "blocks", List.of(
                        Map.of(
                                "type", "section",
                                "text", Map.of(
                                        "type", "mrkdwn",
                                        "text", text
                                )
                        )
                )
        );
    }

    /**
     * WebhookType enum 변환
     */
    private WebhookConfig.WebhookType convertType(WebhookConfigRequest.WebhookType type) {
        return WebhookConfig.WebhookType.valueOf(type.name());
    }
}
