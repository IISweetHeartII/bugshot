package com.bugshot.domain.error.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "error_occurrences", indexes = {
    @Index(name = "idx_error_id", columnList = "error_id, occurred_at"),
    @Index(name = "idx_session_id", columnList = "session_id"),
    @Index(name = "idx_occurred_at", columnList = "occurred_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ErrorOccurrence {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "error_id", nullable = false)
    private Error error;

    // Request Info
    @Column(nullable = false, length = 1000)
    private String url;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    // User Identification (anonymous)
    @Column(name = "user_identifier", length = 100)
    private String userIdentifier;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    // Environment
    @Column(length = 50)
    private String browser;

    @Column(length = 50)
    private String os;

    @Column(length = 50)
    private String device;

    // Context (JSON)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_headers", columnDefinition = "JSON")
    private Map<String, Object> requestHeaders;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_params", columnDefinition = "JSON")
    private Map<String, Object> requestParams;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_data", columnDefinition = "JSON")
    private Map<String, Object> customData;

    // Session Replay
    @Column(name = "session_replay_id", length = 36)
    private String sessionReplayId;

    // Timestamp
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
    }

    // Business Methods
    public void attachSessionReplay(String replayId) {
        this.sessionReplayId = replayId;
    }
}
