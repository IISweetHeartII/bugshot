package com.bugshot.domain.notification.service;

import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.entity.ErrorOccurrence;
import com.bugshot.domain.notification.entity.NotificationChannel;
import com.bugshot.domain.notification.repository.NotificationChannelRepository;
import com.bugshot.domain.notification.strategy.NotificationStrategy;
import com.bugshot.domain.notification.strategy.NotificationStrategyRegistry;
import com.bugshot.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 알림 전송 서비스 (Strategy Pattern 적용)
 * <p>
 * 기존의 거대한 switch문 대신 Strategy Pattern을 사용하여
 * 각 채널별 알림 로직을 분리했습니다.
 * </p>
 *
 * <pre>
 * Before (switch문):
 *   switch (channel.getChannelType()) {
 *       case DISCORD -> sendDiscordNotification(...);
 *       case SLACK -> sendSlackNotification(...);
 *       // 새 채널 추가 시 여기 수정 필요
 *   }
 *
 * After (Strategy Pattern):
 *   strategyRegistry.getStrategy(channelType).send(...);
 *   // 새 채널 추가 시 새 Strategy 클래스만 생성
 * </pre>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationChannelRepository channelRepository;
    private final NotificationStrategyRegistry strategyRegistry;

    /**
     * 에러 발생 시 모든 활성화된 채널로 알림 전송
     */
    @Async
    public void notifyError(Project project, Error error, ErrorOccurrence occurrence) {
        log.info("Sending notifications: projectId={}, errorId={}", project.getId(), error.getId());

        List<NotificationChannel> channels = channelRepository.findByProjectIdAndEnabled(
                project.getId(), true
        );

        if (channels.isEmpty()) {
            log.debug("No enabled notification channels for project: {}", project.getId());
            return;
        }

        int sentCount = 0;
        int failedCount = 0;

        for (NotificationChannel channel : channels) {
            if (!channel.shouldNotify(error.getSeverity())) {
                log.debug("Skipping channel {} - severity {} below threshold {}",
                        channel.getChannelType(), error.getSeverity(), channel.getMinSeverity());
                continue;
            }

            try {
                sendNotification(channel, project, error, occurrence);
                channel.incrementNotificationCount();
                channelRepository.save(channel);
                sentCount++;
            } catch (Exception e) {
                log.error("Failed to send notification via {}: {}",
                        channel.getChannelType(), e.getMessage());
                failedCount++;
            }
        }

        log.info("Notifications completed: errorId={}, sent={}, failed={}",
                error.getId(), sentCount, failedCount);
    }

    /**
     * Strategy Pattern을 사용한 알림 전송
     * <p>
     * switch문 대신 Registry에서 적절한 Strategy를 찾아 실행합니다.
     * </p>
     */
    private void sendNotification(NotificationChannel channel,
                                   Project project,
                                   Error error,
                                   ErrorOccurrence occurrence) {
        strategyRegistry.getStrategy(channel.getChannelType())
                .ifPresentOrElse(
                        strategy -> {
                            log.debug("Using {} strategy for channel",
                                    strategy.getChannelType());
                            strategy.send(channel, project, error, occurrence);
                        },
                        () -> log.warn("No strategy found for channel type: {}",
                                channel.getChannelType())
                );
    }

    /**
     * 특정 채널로 테스트 메시지 전송
     */
    public void sendTestNotification(NotificationChannel channel) {
        NotificationStrategy strategy = strategyRegistry.getStrategyOrThrow(channel.getChannelType());
        strategy.sendTest(channel);
        log.info("Test notification sent via {}", channel.getChannelType());
    }
}
