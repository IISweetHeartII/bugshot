package com.bugshot.domain.project.entity;

import com.bugshot.domain.auth.entity.User;
import com.bugshot.domain.common.BaseEntity;
import com.bugshot.domain.error.entity.Error;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Basic Info
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // API Key (for SDK)
    @Column(name = "api_key", unique = true, nullable = false, length = 64)
    private String apiKey;

    // Environment
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Environment environment = Environment.PRODUCTION;

    // Session Replay Settings
    @Column(name = "session_replay_enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private Boolean sessionReplayEnabled = true;

    @Column(name = "session_replay_sample_rate", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal sessionReplaySampleRate = BigDecimal.ONE;

    // Stats (denormalized)
    @Column(name = "total_errors")
    @Builder.Default
    private Integer totalErrors = 0;

    @Column(name = "total_users_affected")
    @Builder.Default
    private Integer totalUsersAffected = 0;

    @Column(name = "last_error_at")
    private LocalDateTime lastErrorAt;

    // Relationships
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Error> errors = new ArrayList<>();

    @PrePersist
    public void generateIdAndApiKey() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (apiKey == null) {
            apiKey = generateApiKey();
        }
    }

    // Business Methods
    public void incrementErrorCount() {
        this.totalErrors++;
        this.lastErrorAt = LocalDateTime.now();
    }

    public void updateUsersAffected(int count) {
        this.totalUsersAffected = count;
    }

    public String regenerateApiKey() {
        this.apiKey = generateApiKey();
        return this.apiKey;
    }

    private String generateApiKey() {
        return "sk_live_" + UUID.randomUUID().toString().replace("-", "");
    }

    public void updateSettings(String name, String description, Environment environment) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (environment != null) {
            this.environment = environment;
        }
    }

    public void updateSessionReplaySettings(Boolean enabled, BigDecimal sampleRate) {
        if (enabled != null) {
            this.sessionReplayEnabled = enabled;
        }
        if (sampleRate != null && sampleRate.compareTo(BigDecimal.ZERO) >= 0
                && sampleRate.compareTo(BigDecimal.ONE) <= 0) {
            this.sessionReplaySampleRate = sampleRate;
        }
    }

    public enum Environment {
        DEVELOPMENT,
        STAGING,
        PRODUCTION
    }
}
