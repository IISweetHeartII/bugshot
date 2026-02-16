package com.bugshot.domain.notification.strategy;

import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.entity.ErrorOccurrence;
import com.bugshot.domain.notification.entity.NotificationChannel;
import com.bugshot.domain.notification.service.EmailNotificationService;
import com.bugshot.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Email 알림 전송 전략
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationStrategy implements NotificationStrategy {

    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;

    @Override
    public NotificationChannel.ChannelType getChannelType() {
        return NotificationChannel.ChannelType.EMAIL;
    }

    @Override
    public void send(NotificationChannel channel, Project project, Error error, ErrorOccurrence occurrence) {
        if (emailNotificationService == null) {
            log.warn("EmailNotificationService is not available");
            return;
        }

        String recipientEmail = (String) channel.getConfig().get("email");
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("Email recipient is missing in notification channel config");
            return;
        }

        try {
            emailNotificationService.sendErrorNotification(recipientEmail, project, error, occurrence);
            log.info("Email notification sent: errorId={}, to={}", error.getId(), recipientEmail);
        } catch (Exception e) {
            log.error("Email notification failed: errorId={}, to={}", error.getId(), recipientEmail, e);
        }
    }
}
