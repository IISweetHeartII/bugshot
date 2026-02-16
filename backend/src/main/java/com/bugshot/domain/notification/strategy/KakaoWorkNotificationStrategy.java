package com.bugshot.domain.notification.strategy;

import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.entity.ErrorOccurrence;
import com.bugshot.domain.notification.entity.NotificationChannel;
import com.bugshot.domain.notification.service.KakaoNotificationService;
import com.bugshot.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * KakaoWork 알림 전송 전략
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoWorkNotificationStrategy implements NotificationStrategy {

    @Autowired(required = false)
    private KakaoNotificationService kakaoNotificationService;

    @Override
    public NotificationChannel.ChannelType getChannelType() {
        return NotificationChannel.ChannelType.KAKAO_WORK;
    }

    @Override
    public void send(NotificationChannel channel, Project project, Error error, ErrorOccurrence occurrence) {
        if (kakaoNotificationService == null) {
            log.warn("KakaoNotificationService is not available");
            return;
        }

        String webhookUrl = channel.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Kakao Work webhook URL is missing");
            return;
        }

        try {
            kakaoNotificationService.sendErrorNotification(webhookUrl, project, error, occurrence);
            log.info("KakaoWork notification sent: errorId={}", error.getId());
        } catch (Exception e) {
            log.error("KakaoWork notification failed: errorId={}", error.getId(), e);
        }
    }
}
