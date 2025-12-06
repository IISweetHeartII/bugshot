package com.bugshot.domain.error.event;

import com.bugshot.domain.error.dto.IngestRequest;
import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.entity.ErrorOccurrence;
import com.bugshot.domain.project.entity.Project;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 에러가 수집되었을 때 발행되는 이벤트
 * <p>
 * Observer Pattern을 활용하여 에러 수집 후 필요한 작업들을
 * 느슨하게 결합된 리스너들이 처리하도록 합니다.
 * </p>
 *
 * <pre>
 * 이벤트 발행 시 트리거되는 작업들:
 * - 우선순위 계산 (PriorityCalculationListener)
 * - 세션 리플레이 저장 (SessionReplayListener)
 * - 알림 전송 (NotificationListener)
 * </pre>
 */
@Getter
public class ErrorIngestedEvent extends ApplicationEvent {

    private final Project project;
    private final Error error;
    private final ErrorOccurrence occurrence;
    private final String contextUrl;
    private final IngestRequest.SessionReplayData sessionReplayData;
    private final boolean sessionReplayEnabled;

    public ErrorIngestedEvent(
            Object source,
            Project project,
            Error error,
            ErrorOccurrence occurrence,
            String contextUrl,
            IngestRequest.SessionReplayData sessionReplayData,
            boolean sessionReplayEnabled
    ) {
        super(source);
        this.project = project;
        this.error = error;
        this.occurrence = occurrence;
        this.contextUrl = contextUrl;
        this.sessionReplayData = sessionReplayData;
        this.sessionReplayEnabled = sessionReplayEnabled;
    }

    /**
     * 세션 리플레이 데이터가 있는지 확인
     */
    public boolean hasSessionReplay() {
        return sessionReplayData != null && sessionReplayEnabled;
    }

    @Override
    public String toString() {
        return String.format("ErrorIngestedEvent[errorId=%s, projectId=%s, hasReplay=%s]",
                error.getId(), project.getId(), hasSessionReplay());
    }
}
