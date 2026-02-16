package com.bugshot.domain.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 웹훅 설정 요청
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookConfigRequest {
    /**
     * 프로젝트 ID
     */
    @NotBlank(message = "프로젝트 ID는 필수입니다.")
    private String projectId;

    /**
     * 웹훅 타입 (DISCORD, SLACK, TELEGRAM 등)
     */
    @NotNull(message = "웹훅 타입은 필수입니다.")
    private WebhookType type;

    /**
     * 웹훅 URL
     */
    @NotBlank(message = "웹훅 URL은 필수입니다.")
    private String webhookUrl;

    /**
     * 웹훅 이름
     */
    @NotBlank(message = "웹훅 이름은 필수입니다.")
    private String name;

    /**
     * 활성화 여부
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * 알림을 받을 에러 심각도
     */
    private List<String> severityFilters;

    /**
     * 알림을 받을 환경
     */
    private List<String> environmentFilters;

    public enum WebhookType {
        DISCORD,
        SLACK,
        TELEGRAM,
        CUSTOM
    }
}
