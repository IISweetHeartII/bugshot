package com.bugshot.domain.error.event.listener;

import com.bugshot.domain.error.event.ErrorIngestedEvent;
import com.bugshot.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 알림 전송 리스너
 * <p>
 * ErrorIngestedEvent를 수신하여 설정된 채널들로 알림을 비동기로 전송합니다.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleErrorIngested(ErrorIngestedEvent event) {
        log.debug("Sending notifications: errorId={}, projectId={}",
                event.getError().getId(), event.getProject().getId());

        try {
            notificationService.notifyError(
                    event.getProject(),
                    event.getError(),
                    event.getOccurrence()
            );

            log.info("Notifications sent: errorId={}", event.getError().getId());

        } catch (Exception e) {
            log.error("Failed to send notifications: errorId={}", event.getError().getId(), e);
            // 알림 전송 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }
}
