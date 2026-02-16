package com.bugshot.domain.replay.entity;

import com.bugshot.domain.error.entity.ErrorOccurrence;
import com.bugshot.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "session_replays", indexes = {
    @Index(name = "idx_session_id", columnList = "session_id"),
    @Index(name = "idx_error_occurrence", columnList = "error_occurrence_id"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SessionReplay {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "error_occurrence_id")
    private ErrorOccurrence errorOccurrence;

    // Session Info
    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    // Replay Data (S3 URL)
    @Column(name = "replay_data_url", nullable = false, length = 500)
    private String replayDataUrl;

    // Metadata
    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "events_count")
    private Integer eventsCount;

    @Column(name = "file_size_bytes")
    private Integer fileSizeBytes;

    // Compression
    @Column(name = "is_compressed")
    @Builder.Default
    private Boolean isCompressed = true;

    @Column(name = "compression_type", length = 20)
    @Builder.Default
    private String compressionType = "gzip";

    // Timestamps
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            // Default: 30 days retention
            expiresAt = LocalDateTime.now().plusDays(30);
        }
    }

    // Business Methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void extendExpiration(int days) {
        this.expiresAt = this.expiresAt.plusDays(days);
    }
}
