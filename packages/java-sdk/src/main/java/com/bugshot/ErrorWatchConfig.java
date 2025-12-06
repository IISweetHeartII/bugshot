package com.bugshot;

/**
 * Bugshot SDK Configuration
 * SDK 설정을 담는 클래스
 */
public class BugshotConfig {

    private final String apiKey;
    private String endpoint = "http://localhost:8081";
    private String environment = "production";
    private String release;
    private boolean debug = false;
    private boolean enableAutoCapture = true;
    private double sampleRate = 1.0;

    private BugshotConfig(Builder builder) {
        this.apiKey = builder.apiKey;
        this.endpoint = builder.endpoint;
        this.environment = builder.environment;
        this.release = builder.release;
        this.debug = builder.debug;
        this.enableAutoCapture = builder.enableAutoCapture;
        this.sampleRate = builder.sampleRate;
    }

    // Getters
    public String getApiKey() { return apiKey; }
    public String getEndpoint() { return endpoint; }
    public String getEnvironment() { return environment; }
    public String getRelease() { return release; }
    public boolean isDebug() { return debug; }
    public boolean isEnableAutoCapture() { return enableAutoCapture; }
    public double getSampleRate() { return sampleRate; }

    /**
     * Builder Pattern for configuration
     */
    public static class Builder {
        private final String apiKey;
        private String endpoint = "http://localhost:8081";
        private String environment = "production";
        private String release;
        private boolean debug = false;
        private boolean enableAutoCapture = true;
        private double sampleRate = 1.0;

        public Builder(String apiKey) {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("API key is required");
            }
            this.apiKey = apiKey;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder release(String release) {
            this.release = release;
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder enableAutoCapture(boolean enableAutoCapture) {
            this.enableAutoCapture = enableAutoCapture;
            return this;
        }

        public Builder sampleRate(double sampleRate) {
            if (sampleRate < 0 || sampleRate > 1) {
                throw new IllegalArgumentException("Sample rate must be between 0 and 1");
            }
            this.sampleRate = sampleRate;
            return this;
        }

        public BugshotConfig build() {
            return new BugshotConfig(this);
        }
    }
}
