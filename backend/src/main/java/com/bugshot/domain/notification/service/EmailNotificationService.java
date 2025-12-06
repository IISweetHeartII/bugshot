package com.bugshot.domain.notification.service;

import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.entity.ErrorOccurrence;
import com.bugshot.domain.project.entity.Project;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    /**
     * 에러 알림 이메일 전송
     */
    public void sendErrorNotification(String toEmail, Project project, Error error, ErrorOccurrence occurrence) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(buildSubject(error));

            String htmlContent = buildHtmlContent(project, error, occurrence);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Error notification email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send email notification to: " + toEmail, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    /**
     * 이메일 제목 생성
     */
    private String buildSubject(Error error) {
        String emoji = getSeverityEmoji(error.getSeverity());
        return String.format("[%s %s] %s", emoji, error.getSeverity().name(), error.getErrorType());
    }

    /**
     * HTML 이메일 본문 생성 (Thymeleaf 없이 직접 생성)
     */
    private String buildHtmlContent(Project project, Error error, ErrorOccurrence occurrence) {
        String severityColor = getSeverityColor(error.getSeverity());
        String severityEmoji = getSeverityEmoji(error.getSeverity());
        String errorUrl = frontendBaseUrl + "/errors/" + error.getId();
        String projectUrl = frontendBaseUrl + "/projects/" + project.getId();
        String replayUrl = occurrence.getSessionId() != null
                ? frontendBaseUrl + "/replays/" + occurrence.getSessionId()
                : null;

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"ko\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.append("<title>Error Notification</title>");
        html.append("<style>");
        html.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }");
        html.append(".container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }");
        html.append(".header { background-color: ").append(severityColor).append("; color: white; padding: 30px 40px; }");
        html.append(".header h1 { margin: 0; font-size: 24px; font-weight: 600; }");
        html.append(".header .emoji { font-size: 32px; margin-right: 10px; }");
        html.append(".content { padding: 40px; }");
        html.append(".error-info { background-color: #f9f9f9; border-left: 4px solid ").append(severityColor).append("; padding: 20px; margin: 20px 0; border-radius: 4px; }");
        html.append(".error-message { font-size: 18px; font-weight: 600; color: #333; margin-bottom: 15px; }");
        html.append(".error-details { font-size: 14px; color: #666; line-height: 1.8; }");
        html.append(".detail-row { display: flex; margin: 8px 0; }");
        html.append(".detail-label { font-weight: 600; min-width: 120px; color: #555; }");
        html.append(".detail-value { color: #333; word-break: break-all; }");
        html.append(".stats { display: flex; justify-content: space-around; margin: 30px 0; }");
        html.append(".stat { text-align: center; }");
        html.append(".stat-value { font-size: 28px; font-weight: 700; color: ").append(severityColor).append("; }");
        html.append(".stat-label { font-size: 12px; color: #888; margin-top: 5px; text-transform: uppercase; }");
        html.append(".btn { display: inline-block; padding: 12px 24px; background-color: ").append(severityColor).append("; color: white; text-decoration: none; border-radius: 6px; font-weight: 600; margin: 10px 10px 10px 0; }");
        html.append(".btn:hover { opacity: 0.9; }");
        html.append(".footer { background-color: #f9f9f9; padding: 20px 40px; text-align: center; font-size: 12px; color: #888; border-top: 1px solid #e0e0e0; }");
        html.append(".code { background-color: #2d2d2d; color: #f8f8f2; padding: 15px; border-radius: 6px; font-family: 'Courier New', monospace; font-size: 13px; overflow-x: auto; margin: 15px 0; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class=\"container\">");

        // Header
        html.append("<div class=\"header\">");
        html.append("<span class=\"emoji\">").append(severityEmoji).append("</span>");
        html.append("<h1>").append(error.getSeverity().name()).append(" Error Detected</h1>");
        html.append("</div>");

        // Content
        html.append("<div class=\"content\">");
        html.append("<div class=\"error-info\">");
        html.append("<div class=\"error-message\">").append(escapeHtml(error.getErrorType())).append("</div>");
        html.append("<div class=\"error-details\">");
        html.append("<div class=\"detail-row\"><span class=\"detail-label\">Message:</span><span class=\"detail-value\">").append(escapeHtml(error.getErrorMessage())).append("</span></div>");
        html.append("<div class=\"detail-row\"><span class=\"detail-label\">Location:</span><span class=\"detail-value\">").append(escapeHtml(formatLocation(error))).append("</span></div>");
        html.append("<div class=\"detail-row\"><span class=\"detail-label\">URL:</span><span class=\"detail-value\">").append(escapeHtml(occurrence.getUrl())).append("</span></div>");
        html.append("<div class=\"detail-row\"><span class=\"detail-label\">Browser:</span><span class=\"detail-value\">").append(escapeHtml(occurrence.getBrowser() != null ? occurrence.getBrowser() : "Unknown")).append("</span></div>");
        html.append("<div class=\"detail-row\"><span class=\"detail-label\">Time:</span><span class=\"detail-value\">").append(occurrence.getOccurredAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</span></div>");
        html.append("</div>");
        html.append("</div>");

        // Statistics
        html.append("<div class=\"stats\">");
        html.append("<div class=\"stat\"><div class=\"stat-value\">").append(error.getOccurrenceCount()).append("</div><div class=\"stat-label\">Occurrences</div></div>");
        html.append("<div class=\"stat\"><div class=\"stat-value\">").append(error.getAffectedUsersCount()).append("</div><div class=\"stat-label\">Affected Users</div></div>");
        html.append("<div class=\"stat\"><div class=\"stat-value\">").append(error.getPriorityScore()).append("</div><div class=\"stat-label\">Priority Score</div></div>");
        html.append("</div>");

        // Stack Trace (if available)
        if (error.getStackTrace() != null && !error.getStackTrace().isEmpty()) {
            html.append("<h3 style=\"color: #333; margin-top: 30px;\">Stack Trace</h3>");
            html.append("<div class=\"code\">").append(escapeHtml(truncateStackTrace(error.getStackTrace()))).append("</div>");
        }

        // Action Buttons
        html.append("<div style=\"margin-top: 30px;\">");
        html.append("<a href=\"").append(errorUrl).append("\" class=\"btn\">View Error Details</a>");
        if (replayUrl != null) {
            html.append("<a href=\"").append(replayUrl).append("\" class=\"btn\">Watch Session Replay</a>");
        }
        html.append("<a href=\"").append(projectUrl).append("\" class=\"btn\">View Project</a>");
        html.append("</div>");

        html.append("</div>");

        // Footer
        html.append("<div class=\"footer\">");
        html.append("<p><strong>").append(escapeHtml(project.getName())).append("</strong> • BugShot</p>");
        html.append("<p>You're receiving this email because you're subscribed to error notifications for this project.</p>");
        html.append("<p style=\"color: #bbb; margin-top: 15px;\">© 2025 BugShot. All rights reserved.</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private String getSeverityEmoji(Error.Severity severity) {
        return switch (severity) {
            case CRITICAL -> "🔴";
            case HIGH -> "🟡";
            case MEDIUM -> "🟢";
            case LOW -> "⚪";
        };
    }

    private String getSeverityColor(Error.Severity severity) {
        return switch (severity) {
            case CRITICAL -> "#ED4245";
            case HIGH -> "#FEE75C";
            case MEDIUM -> "#57F287";
            case LOW -> "#99AAB5";
        };
    }

    private String formatLocation(Error error) {
        if (error.getFilePath() != null && error.getLineNumber() != null) {
            return error.getFilePath() + ":" + error.getLineNumber();
        } else if (error.getFilePath() != null) {
            return error.getFilePath();
        }
        return "Unknown";
    }

    private String truncateStackTrace(String stackTrace) {
        if (stackTrace.length() > 2000) {
            return stackTrace.substring(0, 2000) + "\n\n... (truncated)";
        }
        return stackTrace;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br>");
    }
}
