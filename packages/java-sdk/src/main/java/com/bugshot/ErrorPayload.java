package com.bugshot;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Error Payload - 서버로 전송할 에러 데이터
 */
public class ErrorPayload {

    private String apiKey;
    private ErrorInfo error;
    private ContextInfo context;

    public ErrorPayload(String apiKey, Throwable throwable, Map<String, Object> additionalInfo) {
        this.apiKey = apiKey;
        this.error = new ErrorInfo(throwable);
        this.context = new ContextInfo(additionalInfo);
    }

    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"apiKey\":\"").append(escapeJson(apiKey)).append("\",");
        json.append("\"error\":").append(error.toJson()).append(",");
        json.append("\"context\":").append(context.toJson());
        json.append("}");
        return json.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    /**
     * Error Information
     */
    static class ErrorInfo {
        private String type;
        private String message;
        private String stackTrace;
        private String filePath;
        private Integer lineNumber;
        private String methodName;

        public ErrorInfo(Throwable throwable) {
            this.type = throwable.getClass().getName();
            this.message = throwable.getMessage() != null ? throwable.getMessage() : "No message";
            this.stackTrace = getStackTraceString(throwable);

            // Extract location from first stack trace element
            StackTraceElement[] elements = throwable.getStackTrace();
            if (elements != null && elements.length > 0) {
                StackTraceElement first = elements[0];
                this.filePath = first.getFileName();
                this.lineNumber = first.getLineNumber();
                this.methodName = first.getClassName() + "." + first.getMethodName();
            }
        }

        private String getStackTraceString(Throwable throwable) {
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement element : throwable.getStackTrace()) {
                sb.append("\tat ").append(element.toString()).append("\n");
            }
            return sb.toString();
        }

        public String toJson() {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"type\":\"").append(escapeJson(type)).append("\",");
            json.append("\"message\":\"").append(escapeJson(message)).append("\",");
            json.append("\"stackTrace\":\"").append(escapeJson(stackTrace)).append("\"");
            if (filePath != null) {
                json.append(",\"filePath\":\"").append(escapeJson(filePath)).append("\"");
            }
            if (lineNumber != null) {
                json.append(",\"lineNumber\":").append(lineNumber);
            }
            if (methodName != null) {
                json.append(",\"methodName\":\"").append(escapeJson(methodName)).append("\"");
            }
            json.append("}");
            return json.toString();
        }

        private String escapeJson(String value) {
            if (value == null) return "";
            return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        }
    }

    /**
     * Context Information
     */
    static class ContextInfo {
        private String timestamp;
        private String platform;
        private String javaVersion;
        private String osName;
        private String osVersion;
        private Map<String, Object> additional;

        public ContextInfo(Map<String, Object> additionalInfo) {
            this.timestamp = Instant.now().toString();
            this.platform = "java";
            this.javaVersion = System.getProperty("java.version");
            this.osName = System.getProperty("os.name");
            this.osVersion = System.getProperty("os.version");
            this.additional = additionalInfo != null ? additionalInfo : new HashMap<>();
        }

        public String toJson() {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"timestamp\":\"").append(timestamp).append("\",");
            json.append("\"platform\":\"").append(platform).append("\",");
            json.append("\"javaVersion\":\"").append(escapeJson(javaVersion)).append("\",");
            json.append("\"osName\":\"").append(escapeJson(osName)).append("\",");
            json.append("\"osVersion\":\"").append(escapeJson(osVersion)).append("\"");

            // Add additional info
            for (Map.Entry<String, Object> entry : additional.entrySet()) {
                json.append(",\"").append(escapeJson(entry.getKey())).append("\":");
                Object value = entry.getValue();
                if (value instanceof String) {
                    json.append("\"").append(escapeJson((String) value)).append("\"");
                } else if (value instanceof Number || value instanceof Boolean) {
                    json.append(value);
                } else {
                    json.append("\"").append(escapeJson(String.valueOf(value))).append("\"");
                }
            }

            json.append("}");
            return json.toString();
        }

        private String escapeJson(String value) {
            if (value == null) return "";
            return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        }
    }
}
