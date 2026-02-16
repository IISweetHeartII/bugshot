package com.bugshot.domain.notification.strategy;

import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.entity.ErrorOccurrence;
import com.bugshot.domain.notification.entity.NotificationChannel;
import com.bugshot.domain.project.entity.Project;

/**
 * 알림 전송 전략 인터페이스 (Strategy Pattern)
 * <p>
 * 각 알림 채널(Discord, Slack, Email 등)의 전송 로직을 캡슐화합니다.
 * 새로운 채널 추가 시 이 인터페이스를 구현하는 클래스만 추가하면 됩니다.
 * </p>
 *
 * <pre>
 * 사용 예시:
 * {@code
 * NotificationStrategy strategy = registry.getStrategy(ChannelType.DISCORD);
 * strategy.send(channel, project, error, occurrence);
 * }
 * </pre>
 */
public interface NotificationStrategy {

    /**
     * 이 전략이 처리하는 채널 타입 반환
     *
     * @return 지원하는 채널 타입
     */
    NotificationChannel.ChannelType getChannelType();

    /**
     * 알림 전송
     *
     * @param channel    알림 채널 설정
     * @param project    프로젝트 정보
     * @param error      에러 정보
     * @param occurrence 에러 발생 정보
     */
    void send(NotificationChannel channel, Project project, Error error, ErrorOccurrence occurrence);

    /**
     * 테스트 메시지 전송
     *
     * @param channel 알림 채널 설정
     */
    default void sendTest(NotificationChannel channel) {
        // 기본 구현: 지원하지 않음
        throw new UnsupportedOperationException(
                "Test message not supported for " + getChannelType());
    }
}
