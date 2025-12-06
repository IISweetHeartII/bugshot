package com.bugshot.domain.error.service;

import com.bugshot.domain.error.dto.IngestRequest;
import com.bugshot.domain.error.dto.IngestResponse;
import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.entity.ErrorOccurrence;
import com.bugshot.domain.error.event.ErrorIngestedEvent;
import com.bugshot.domain.error.repository.ErrorOccurrenceRepository;
import com.bugshot.domain.error.repository.ErrorRepository;
import com.bugshot.domain.project.entity.Project;
import com.bugshot.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorService {

    private final ErrorRepository errorRepository;
    private final ErrorOccurrenceRepository occurrenceRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationEventPublisher eventPublisher;

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

        // 6. 이벤트 발행 - Observer Pattern 적용
        // 리스너들이 비동기로 다음 작업들을 처리:
        // - PriorityCalculationListener: 우선순위 계산
        // - SessionReplayListener: 세션 리플레이 저장
        // - NotificationListener: 알림 전송
        publishErrorIngestedEvent(project, error, occurrence, request);

        return IngestResponse.success(error.getId());
    }

    /**
     * ErrorIngestedEvent 발행
     * <p>
     * Observer Pattern: 에러 수집 완료 시 이벤트를 발행하여
     * 여러 리스너들이 독립적으로 후속 작업을 처리하도록 합니다.
     * </p>
     */
    private void publishErrorIngestedEvent(Project project, Error error,
                                            ErrorOccurrence occurrence, IngestRequest request) {
        ErrorIngestedEvent event = new ErrorIngestedEvent(
                this,
                project,
                error,
                occurrence,
                request.getContext().getUrl(),
                request.getSessionReplay(),
                project.getSessionReplayEnabled()
        );

        eventPublisher.publishEvent(event);
        log.debug("ErrorIngestedEvent published: {}", event);
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
