package com.bugshot.domain.error.event.listener;

import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.event.ErrorIngestedEvent;
import com.bugshot.domain.error.repository.ErrorOccurrenceRepository;
import com.bugshot.domain.error.repository.ErrorRepository;
import com.bugshot.domain.project.entity.Project;
import com.bugshot.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 에러 우선순위 계산 리스너
 * <p>
 * ErrorIngestedEvent를 수신하여 에러의 우선순위를 비동기로 계산합니다.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PriorityCalculationListener {

    private final ErrorRepository errorRepository;
    private final ErrorOccurrenceRepository occurrenceRepository;
    private final ProjectRepository projectRepository;

    @Async
    @EventListener
    @Transactional
    public void handleErrorIngested(ErrorIngestedEvent event) {
        String errorId = event.getError().getId();
        String url = event.getContextUrl();

        log.debug("Calculating priority for error: {}", errorId);

        try {
            Error error = errorRepository.findById(errorId)
                    .orElseThrow(() -> new IllegalStateException("Error not found: " + errorId));

            // 영향받은 사용자 수 계산
            long affectedUsers = occurrenceRepository.countDistinctUsersByErrorId(errorId);
            error.updateAffectedUsersCount((int) affectedUsers);

            // 우선순위 점수 계산
            error.calculatePriority(url);

            // 프로젝트 통계 업데이트
            Project project = error.getProject();
            long totalAffected = errorRepository.countTotalAffectedUsers(project.getId());
            project.updateUsersAffected((int) totalAffected);

            errorRepository.save(error);
            projectRepository.save(project);

            log.info("Priority calculated: errorId={}, score={}, severity={}",
                    errorId, error.getPriorityScore(), error.getSeverity());

        } catch (Exception e) {
            log.error("Failed to calculate priority for error: {}", errorId, e);
            // 우선순위 계산 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }
}
