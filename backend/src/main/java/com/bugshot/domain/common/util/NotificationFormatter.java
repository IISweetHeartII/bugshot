package com.bugshot.domain.common.util;

import com.bugshot.domain.error.entity.Error;

/**
 * 알림 서비스들에서 공통으로 사용하는 포맷팅 유틸리티
 * <p>
 * Discord, Slack, Email, Webhook 등 다양한 알림 채널에서 일관된 형식을 유지하기 위해 사용됩니다.
 * </p>
 */
public final class NotificationFormatter {

    private NotificationFormatter() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }

    /**
     * 심각도에 따른 이모지 반환
     *
     * @param severity 에러 심각도
     * @return 심각도를 나타내는 이모지
     */
    public static String getSeverityEmoji(Error.Severity severity) {
        return switch (severity) {
            case CRITICAL -> "🔴";
            case HIGH -> "🟡";
            case MEDIUM -> "🟢";
            case LOW -> "⚪";
        };
    }

    /**
     * Discord embed용 심각도 색상 (정수형 컬러 코드)
     *
     * @param severity 에러 심각도
     * @return Discord embed에서 사용할 색상 코드
     */
    public static int getDiscordColor(Error.Severity severity) {
        return switch (severity) {
            case CRITICAL -> 0xED4245; // Red
            case HIGH -> 0xFEE75C;     // Yellow
            case MEDIUM -> 0x57F287;   // Green
            case LOW -> 0x99AAB5;      // Gray
        };
    }

    /**
     * HTML/CSS용 심각도 색상 (HEX 문자열)
     *
     * @param severity 에러 심각도
     * @return CSS에서 사용할 HEX 색상 문자열
     */
    public static String getHexColor(Error.Severity severity) {
        return switch (severity) {
            case CRITICAL -> "#ED4245";
            case HIGH -> "#FEE75C";
            case MEDIUM -> "#57F287";
            case LOW -> "#99AAB5";
        };
    }

    /**
     * Slack 메시지용 심각도 색상
     *
     * @param severity 에러 심각도
     * @return Slack attachment에서 사용할 색상 값
     */
    public static String getSlackColor(Error.Severity severity) {
        return switch (severity) {
            case CRITICAL -> "danger";
            case HIGH -> "warning";
            case MEDIUM -> "good";
            case LOW -> "#99AAB5";
        };
    }

    /**
     * 에러 발생 위치를 포맷팅
     *
     * @param error 에러 엔티티
     * @return "파일경로:라인번호" 형식의 위치 문자열
     */
    public static String formatLocation(Error error) {
        if (error.getFilePath() != null && error.getLineNumber() != null) {
            return error.getFilePath() + ":" + error.getLineNumber();
        } else if (error.getFilePath() != null) {
            return error.getFilePath();
        }
        return "Unknown";
    }

    /**
     * 에러 발생 위치를 포맷팅 (개별 파라미터)
     *
     * @param filePath   파일 경로
     * @param lineNumber 라인 번호
     * @return "파일경로:라인번호" 형식의 위치 문자열
     */
    public static String formatLocation(String filePath, Integer lineNumber) {
        if (filePath != null && lineNumber != null) {
            return filePath + ":" + lineNumber;
        } else if (filePath != null) {
            return filePath;
        }
        return "Unknown";
    }

    /**
     * 스택 트레이스를 지정된 길이로 자름
     *
     * @param stackTrace 원본 스택 트레이스
     * @param maxLength  최대 길이
     * @return 잘린 스택 트레이스
     */
    public static String truncateStackTrace(String stackTrace, int maxLength) {
        if (stackTrace == null) {
            return "";
        }
        if (stackTrace.length() > maxLength) {
            return stackTrace.substring(0, maxLength) + "\n\n... (truncated)";
        }
        return stackTrace;
    }

    /**
     * 스택 트레이스를 기본 길이(2000자)로 자름
     *
     * @param stackTrace 원본 스택 트레이스
     * @return 잘린 스택 트레이스
     */
    public static String truncateStackTrace(String stackTrace) {
        return truncateStackTrace(stackTrace, 2000);
    }

    /**
     * HTML 이스케이프 처리
     *
     * @param text 원본 텍스트
     * @return HTML 이스케이프된 텍스트
     */
    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br>");
    }
}
