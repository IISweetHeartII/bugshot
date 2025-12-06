package com.bugshot.domain.error.entity;

import com.bugshot.domain.common.BaseEntity;
import com.bugshot.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "errors", indexes = {
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_error_hash", columnList = "project_id, error_hash"),
    @Index(name = "idx_priority", columnList = "project_id, priority_score"),
    @Index(name = "idx_status", columnList = "project_id, status, last_seen_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Error extends BaseEntity {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Error Identification
    @Column(name = "error_hash", nullable = false, length = 64)
    private String errorHash;

    @Column(name = "error_type", nullable = false, length = 100)
    private String errorType;

    @Column(name = "error_message", nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    // Location
    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "method_name", length = 200)
    private String methodName;

    // Stack Trace (first occurrence)
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    // Priority (auto-calculated)
    @Column(name = "priority_score", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal priorityScore = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Severity severity = Severity.MEDIUM;

    // Stats
    @Column(name = "occurrence_count")
    @Builder.Default
    private Integer occurrenceCount = 1;

    @Column(name = "affected_users_count")
    @Builder.Default
    private Integer affectedUsersCount = 0;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ErrorStatus status = ErrorStatus.UNRESOLVED;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by", length = 36)
    private String resolvedBy;

    // Timestamps
    @Column(name = "first_seen_at", nullable = false)
    private LocalDateTime firstSeenAt;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    // Relationships
    @OneToMany(mappedBy = "error", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ErrorOccurrence> occurrences = new ArrayList<>();

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (firstSeenAt == null) {
            firstSeenAt = LocalDateTime.now();
        }
        if (lastSeenAt == null) {
            lastSeenAt = LocalDateTime.now();
        }
        if (errorHash == null) {
            errorHash = calculateErrorHash(errorType, filePath, lineNumber);
        }
    }

    // Business Methods
    public void incrementOccurrence() {
        this.occurrenceCount++;
        this.lastSeenAt = LocalDateTime.now();
    }

    public void updateAffectedUsersCount(int count) {
        this.affectedUsersCount = count;
    }

    public void calculatePriority(String url) {
        double pageWeight = determinePageWeight(url);
        double priority = occurrenceCount * affectedUsersCount * pageWeight;
        this.priorityScore = BigDecimal.valueOf(priority);
        this.severity = determineSeverity(priority, url);
    }

    private double determinePageWeight(String url) {
        if (url == null) return 1.0;

        if (url.contains("/checkout") || url.contains("/payment")) {
            return 10.0;
        } else if (url.contains("/login") || url.contains("/signup")) {
            return 8.0;
        } else if (url.equals("/")) {
            return 5.0;
        }
        return 1.0;
    }

    private Severity determineSeverity(double priority, String url) {
        if (priority > 1000 || (url != null && url.contains("checkout"))) {
            return Severity.CRITICAL;
        } else if (priority > 100) {
            return Severity.HIGH;
        } else if (priority > 10) {
            return Severity.MEDIUM;
        }
        return Severity.LOW;
    }

    public void resolve(String userId) {
        this.status = ErrorStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = userId;
    }

    public void ignore() {
        this.status = ErrorStatus.IGNORED;
    }

    public void reopen() {
        this.status = ErrorStatus.UNRESOLVED;
        this.resolvedAt = null;
        this.resolvedBy = null;
    }

    // Static Methods
    public static String calculateErrorHash(String errorType, String filePath, Integer lineNumber) {
        try {
            String input = errorType + "|" +
                          (filePath != null ? filePath : "") + "|" +
                          (lineNumber != null ? lineNumber.toString() : "");

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public enum Severity {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW
    }

    public enum ErrorStatus {
        UNRESOLVED,
        RESOLVED,
        IGNORED
    }
}
