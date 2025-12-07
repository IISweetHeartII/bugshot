package com.bugshot.domain.error.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestRequest {

    @NotBlank(message = "API key is required")
    private String apiKey;

    @NotNull(message = "Error information is required")
    @Valid
    private ErrorInfo error;

    @NotNull(message = "Context information is required")
    @Valid
    private ContextInfo context;

    private SessionReplayData sessionReplay;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorInfo {
        @NotBlank(message = "Error type is required")
        private String type;

        @NotBlank(message = "Error message is required")
        private String message;

        private String stackTrace;
        private String file;
        private Integer line;
        private Integer column;
        private String method;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContextInfo {
        @NotBlank(message = "URL is required")
        private String url;

        private String httpMethod;
        private String userAgent;
        private String ipAddress;
        private String sessionId;
        private String userId;  // App's user ID (anonymized)
        private String timestamp;  // ISO 8601 format from SDK

        // Legacy fields (String) - for backward compatibility
        private String browser;
        private String os;
        private String device;

        // New fields (Object) - from SDK
        private BrowserInfo browserInfo;
        private DeviceInfo deviceInfo;

        private Map<String, Object> headers;
        private Map<String, Object> params;
        private Map<String, Object> customData;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BrowserInfo {
        private String name;
        private String version;
        private String os;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeviceInfo {
        private String type;  // mobile, tablet, desktop
        private ViewportInfo viewport;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ViewportInfo {
        private Integer width;
        private Integer height;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionReplayData {
        private List<Map<String, Object>> events;
        private Integer durationMs;
        private String sessionId;
    }
}
