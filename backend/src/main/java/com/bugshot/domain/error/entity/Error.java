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

    /**
     * 우선순위 및 심각도 계산
     * <p>
     * 공식: priority = baseScore + (occurrence * users * pageWeight * errorTypeWeight * recencyBoost)
     * - baseScore: 최소 1점 보장
     * - errorTypeWeight: TypeError, ReferenceError 등은 더 높은 가중치
     * - recencyBoost: 최근 1시간 내 발생하면 1.5배
     * </p>
     */
    public void calculatePriority(String url) {
        double baseScore = 1.0;
        double pageWeight = determinePageWeight(url);
        double errorTypeWeight = determineErrorTypeWeight();
        double recencyBoost = calculateRecencyBoost();

        // 0 방지를 위해 +1
        double occurrenceFactor = Math.log10(occurrenceCount + 1) + 1;  // 로그 스케일로 급격한 증가 완화
        double usersFactor = Math.max(1, affectedUsersCount);  // 최소 1

        double priority = baseScore + (occurrenceFactor * usersFactor * pageWeight * errorTypeWeight * recencyBoost);

        this.priorityScore = BigDecimal.valueOf(Math.round(priority * 100.0) / 100.0);  // 소수점 2자리
        this.severity = determineSeverity(priority, url);
    }

    /**
     * 에러 타입별 가중치
     */
    private double determineErrorTypeWeight() {
        if (errorType == null) return 1.0;

        String type = errorType.toLowerCase();

        // Critical 타입들 - 코드 버그 가능성 높음
        if (type.contains("typeerror") || type.contains("referenceerror")) {
            return 2.5;
        }
        // High 타입들 - 런타임 에러
        if (type.contains("syntaxerror") || type.contains("rangeerror") || type.contains("urierror")) {
            return 2.0;
        }
        // Medium 타입들 - 네트워크/비동기 관련
        if (type.contains("networkerror") || type.contains("fetcherror") || type.contains("promise")) {
            return 1.5;
        }
        // 일반 에러
        if (type.contains("error")) {
            return 1.2;
        }
        // 세션/이벤트 관련 - 상대적으로 낮은 우선순위
        if (type.contains("session") || type.contains("event")) {
            return 0.8;
        }

        return 1.0;
    }

    /**
     * 최신성 부스트 - 최근에 발생한 에러일수록 높은 가중치
     */
    private double calculateRecencyBoost() {
        if (lastSeenAt == null) return 1.0;

        long hoursSinceLastSeen = java.time.Duration.between(lastSeenAt, LocalDateTime.now()).toHours();

        if (hoursSinceLastSeen < 1) {
            return 2.0;  // 1시간 이내: 2배
        } else if (hoursSinceLastSeen < 6) {
            return 1.5;  // 6시간 이내: 1.5배
        } else if (hoursSinceLastSeen < 24) {
            return 1.2;  // 24시간 이내: 1.2배
        }
        return 1.0;
    }

    private double determinePageWeight(String url) {
        if (url == null) return 1.0;

        // 결제/체크아웃 - 최고 우선순위
        if (url.contains("/checkout") || url.contains("/payment") || url.contains("/order")) {
            return 10.0;
        }
        // 인증 관련
        if (url.contains("/login") || url.contains("/signup") || url.contains("/auth")) {
            return 8.0;
        }
        // 핵심 기능
        if (url.contains("/dashboard") || url.contains("/api/")) {
            return 5.0;
        }
        // 홈페이지
        if (url.equals("/") || url.endsWith(".com") || url.endsWith(".kr")) {
            return 3.0;
        }
        return 1.0;
    }

    private Severity determineSeverity(double priority, String url) {
        // 체크아웃 페이지의 모든 에러는 최소 HIGH
        boolean isCriticalPage = url != null &&
            (url.contains("/checkout") || url.contains("/payment"));

        if (priority > 50 || (isCriticalPage && priority > 20)) {
            return Severity.CRITICAL;
        } else if (priority > 20 || isCriticalPage) {
            return Severity.HIGH;
        } else if (priority > 8) {
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
