package com.error.monitor.service;

import com.error.monitor.api.ingest.dto.IngestRequest;
import com.error.monitor.api.ingest.dto.IngestResponse;
import com.error.monitor.domain.error.Error;
import com.error.monitor.domain.error.ErrorOccurrence;
import com.error.monitor.domain.error.ErrorOccurrenceRepository;
import com.error.monitor.domain.error.ErrorRepository;
import com.error.monitor.domain.project.Project;
import com.error.monitor.domain.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorService {

    private final ErrorRepository errorRepository;
    private final ErrorOccurrenceRepository occurrenceRepository;
    private final ProjectRepository projectRepository;
    private final NotificationService notificationService;
    private final SessionReplayService sessionReplayService;

    @Transactional
    public IngestResponse ingestError(IngestRequest request) {
        log.info("Ingesting error: type={}, url={}", request.getError().getType(), request.getContext().getUrl());

        // 1. Validate API key and get project
        Project project = projectRepository.findByApiKey(request.getApiKey())
            .orElseThrow(() -> new IllegalArgumentException("Invalid API key"));

        // 2. Calculate error hash for grouping
        String errorHash = Error.calculateErrorHash(
            request.getError().getType(),
            request.getError().getFile(),
            request.getError().getLine()
        );

        // 3. Find existing error or create new one
        Error error = errorRepository.findByProjectIdAndErrorHash(project.getId(), errorHash)
            .map(existing -> {
                log.info("Found existing error group: id={}", existing.getId());
                existing.incrementOccurrence();
                return existing;
            })
            .orElseGet(() -> {
                log.info("Creating new error group: hash={}", errorHash);
                Error newError = Error.builder()
                    .project(project)
                    .errorHash(errorHash)
                    .errorType(request.getError().getType())
                    .errorMessage(request.getError().getMessage())
                    .filePath(request.getError().getFile())
                    .lineNumber(request.getError().getLine())
                    .methodName(request.getError().getMethod())
                    .stackTrace(request.getError().getStackTrace())
                    .build();
                return newError;
            });

        error = errorRepository.save(error);

        // 4. Create error occurrence
        ErrorOccurrence occurrence = ErrorOccurrence.builder()
            .error(error)
            .url(request.getContext().getUrl())
            .httpMethod(request.getContext().getHttpMethod())
            .userAgent(request.getContext().getUserAgent())
            .ipAddress(request.getContext().getIpAddress())
            .userIdentifier(request.getContext().getUserId())
            .sessionId(request.getContext().getSessionId())
            .browser(request.getContext().getBrowser())
            .os(request.getContext().getOs())
            .device(request.getContext().getDevice())
            .requestHeaders(request.getContext().getHeaders())
            .requestParams(request.getContext().getParams())
            .customData(request.getContext().getCustomData())
            .build();

        occurrence = occurrenceRepository.save(occurrence);

        // 5. Update project stats
        project.incrementErrorCount();
        projectRepository.save(project);

        // 6. Calculate priority asynchronously
        final String errorId = error.getId();
        final String occurrenceUrl = request.getContext().getUrl();
        CompletableFuture.runAsync(() -> calculateAndUpdatePriority(errorId, occurrenceUrl));

        // 7. Save session replay asynchronously
        if (request.getSessionReplay() != null && project.getSessionReplayEnabled()) {
            final String occurrenceId = occurrence.getId();
            CompletableFuture.runAsync(() ->
                sessionReplayService.saveReplay(project.getId(), occurrenceId, request.getSessionReplay())
            );
        }

        // 8. Send notifications asynchronously
        final Error finalError = error;
        final ErrorOccurrence finalOccurrence = occurrence;
        CompletableFuture.runAsync(() ->
            notificationService.notifyError(project, finalError, finalOccurrence)
        );

        return IngestResponse.success(error.getId());
    }

    @Async
    @Transactional
    public void calculateAndUpdatePriority(String errorId, String url) {
        try {
            Error error = errorRepository.findById(errorId)
                .orElseThrow(() -> new IllegalArgumentException("Error not found: " + errorId));

            // Count affected users
            long affectedUsers = occurrenceRepository.countDistinctUsersByErrorId(errorId);
            error.updateAffectedUsersCount((int) affectedUsers);

            // Calculate priority
            error.calculatePriority(url);

            // Update project's total affected users
            Project project = error.getProject();
            long totalAffected = errorRepository.countTotalAffectedUsers(project.getId());
            project.updateUsersAffected((int) totalAffected);

            errorRepository.save(error);
            projectRepository.save(project);

            log.info("Updated priority for error {}: score={}, severity={}",
                errorId, error.getPriorityScore(), error.getSeverity());
        } catch (Exception e) {
            log.error("Failed to calculate priority for error: " + errorId, e);
        }
    }

    @Transactional(readOnly = true)
    public Error getError(String errorId) {
        return errorRepository.findById(errorId)
            .orElseThrow(() -> new IllegalArgumentException("Error not found: " + errorId));
    }

    @Transactional
    public void resolveError(String errorId, String userId) {
        Error error = getError(errorId);
        error.resolve(userId);
        errorRepository.save(error);
        log.info("Error resolved: id={}, by={}", errorId, userId);
    }

    @Transactional
    public void ignoreError(String errorId) {
        Error error = getError(errorId);
        error.ignore();
        errorRepository.save(error);
        log.info("Error ignored: id={}", errorId);
    }

    @Transactional
    public void reopenError(String errorId) {
        Error error = getError(errorId);
        error.reopen();
        errorRepository.save(error);
        log.info("Error reopened: id={}", errorId);
    }
}
