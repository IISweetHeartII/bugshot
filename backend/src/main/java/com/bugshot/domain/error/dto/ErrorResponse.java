package com.bugshot.domain.error.dto;

import com.bugshot.domain.error.entity.Error;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private String id;
    private String projectId;
    private String errorType;
    private String errorMessage;
    private String filePath;
    private Integer lineNumber;
    private String methodName;
    private String stackTrace;
    private BigDecimal priorityScore;
    private String severity;
    private Integer occurrenceCount;
    private Integer affectedUsersCount;
    private String status;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private LocalDateTime firstSeenAt;
    private LocalDateTime lastSeenAt;

    public static ErrorResponse from(Error error) {
        return ErrorResponse.builder()
            .id(error.getId())
            .projectId(error.getProject().getId())
            .errorType(error.getErrorType())
            .errorMessage(error.getErrorMessage())
            .filePath(error.getFilePath())
            .lineNumber(error.getLineNumber())
            .methodName(error.getMethodName())
            .stackTrace(error.getStackTrace())
            .priorityScore(error.getPriorityScore())
            .severity(error.getSeverity().name())
            .occurrenceCount(error.getOccurrenceCount())
            .affectedUsersCount(error.getAffectedUsersCount())
            .status(error.getStatus().name())
            .resolvedAt(error.getResolvedAt())
            .resolvedBy(error.getResolvedBy())
            .firstSeenAt(error.getFirstSeenAt())
            .lastSeenAt(error.getLastSeenAt())
            .build();
    }
}
