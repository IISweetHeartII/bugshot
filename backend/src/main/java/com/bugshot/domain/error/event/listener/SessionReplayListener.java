package com.bugshot.domain.error.event.listener;

import com.bugshot.domain.error.event.ErrorIngestedEvent;
import com.bugshot.domain.replay.service.SessionReplayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 세션 리플레이 저장 리스너
 * <p>
 * ErrorIngestedEvent를 수신하여 세션 리플레이 데이터를 비동기로 저장합니다.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionReplayListener {

    private final SessionReplayService sessionReplayService;

    @Async
    @EventListener
    public void handleErrorIngested(ErrorIngestedEvent event) {
        // 세션 리플레이가 없거나 비활성화된 경우 스킵
        if (!event.hasSessionReplay()) {
            log.debug("Session replay skipped: errorId={}, hasData={}, enabled={}",
                    event.getError().getId(),
                    event.getSessionReplayData() != null,
                    event.isSessionReplayEnabled());
            return;
        }

        String projectId = event.getProject().getId();
        String occurrenceId = event.getOccurrence().getId();

        log.debug("Saving session replay: projectId={}, occurrenceId={}", projectId, occurrenceId);

        try {
            sessionReplayService.saveReplay(
                    projectId,
                    occurrenceId,
                    event.getSessionReplayData()
            );

            log.info("Session replay saved: occurrenceId={}, eventsCount={}",
                    occurrenceId,
                    event.getSessionReplayData().getEvents() != null
                            ? event.getSessionReplayData().getEvents().size() : 0);

        } catch (Exception e) {
            log.error("Failed to save session replay: occurrenceId={}", occurrenceId, e);
            // 세션 리플레이 저장 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }
}
