package com.bugshot.domain.webhook.entity;

import com.bugshot.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 웹훅 설정 (Discord, Slack 등)
 */
@Entity
@Table(name = "webhook_configs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WebhookConfig {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebhookType type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1000)
    private String webhookUrl;

    @Column(nullable = false)
    private boolean enabled;

    @ElementCollection
    @CollectionTable(name = "webhook_severity_filters", joinColumns = @JoinColumn(name = "webhook_id"))
    @Column(name = "severity")
    private List<String> severityFilters;

    @ElementCollection
    @CollectionTable(name = "webhook_environment_filters", joinColumns = @JoinColumn(name = "webhook_id"))
    @Column(name = "environment")
    private List<String> environmentFilters;

    @Column(nullable = false)
    private long totalSent;

    @Column(nullable = false)
    private long failureCount;

    private LocalDateTime lastTriggeredAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 웹훅 설정 업데이트
     */
    public void updateConfig(String name, String webhookUrl, boolean enabled,
                             List<String> severityFilters, List<String> environmentFilters) {
        this.name = name;
        this.webhookUrl = webhookUrl;
        this.enabled = enabled;
        this.severityFilters = severityFilters;
        this.environmentFilters = environmentFilters;
    }

    /**
     * 웹훅 전송 성공 기록
     */
    public void recordSuccess() {
        this.totalSent++;
        this.lastTriggeredAt = LocalDateTime.now();
    }

    /**
     * 웹훅 전송 실패 기록
     */
    public void recordFailure() {
        this.failureCount++;
        this.lastTriggeredAt = LocalDateTime.now();
    }

    public enum WebhookType {
        DISCORD,
        SLACK,
        TELEGRAM,
        CUSTOM
    }
}
