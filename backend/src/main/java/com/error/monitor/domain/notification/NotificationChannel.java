package com.error.monitor.domain.notification;

import com.error.monitor.domain.common.BaseEntity;
import com.error.monitor.domain.error.Error;
import com.error.monitor.domain.project.Project;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notification_channels", indexes = {
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_enabled", columnList = "project_id, enabled")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationChannel extends BaseEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Channel Type
    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 20)
    private ChannelType channelType;

    // Configuration (JSON)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "JSON")
    private Map<String, Object> config;

    // Filtering
    @Enumerated(EnumType.STRING)
    @Column(name = "min_severity", length = 20)
    @Builder.Default
    private Error.Severity minSeverity = Error.Severity.MEDIUM;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    // Stats
    @Column(name = "total_notifications_sent")
    @Builder.Default
    private Integer totalNotificationsSent = 0;

    @Column(name = "last_notified_at")
    private LocalDateTime lastNotifiedAt;

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    // Business Methods
    public boolean shouldNotify(Error.Severity errorSeverity) {
        if (!enabled) {
            return false;
        }
        // CRITICAL > HIGH > MEDIUM > LOW
        return errorSeverity.ordinal() <= minSeverity.ordinal();
    }

    public void incrementNotificationCount() {
        this.totalNotificationsSent++;
        this.lastNotifiedAt = LocalDateTime.now();
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public void updateConfig(Map<String, Object> newConfig) {
        this.config = newConfig;
    }

    public void updateMinSeverity(Error.Severity severity) {
        this.minSeverity = severity;
    }

    // Helper Methods
    public String getWebhookUrl() {
        return (String) config.get("webhook_url");
    }

    public String getBotToken() {
        return (String) config.get("bot_token");
    }

    public String getChatId() {
        return (String) config.get("chat_id");
    }

    public enum ChannelType {
        DISCORD,
        SLACK,
        TELEGRAM,
        EMAIL,
        WEBHOOK
    }
}
