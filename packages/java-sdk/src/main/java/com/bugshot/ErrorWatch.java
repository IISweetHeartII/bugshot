package com.bugshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Bugshot Java SDK
 *
 * Java 애플리케이션의 에러를 자동으로 캡처하고 서버로 전송합니다.
 *
 * 사용법:
 * <pre>
 * // 초기화
 * Bugshot.init(new BugshotConfig.Builder("your-api-key")
 *     .endpoint("https://your-server.com")
 *     .environment("production")
 *     .debug(true)
 *     .build());
 *
 * // 수동 에러 캡처
 * try {
 *     riskyOperation();
 * } catch (Exception e) {
 *     Bugshot.captureException(e);
 * }
 * </pre>
 *
 * @author Bugshot Team
 * @version 1.0.0
 */
public final class Bugshot {

    private static Bugshot instance;
    private final BugshotConfig config;
    private final HttpTransport transport;
    private final Random random = new Random();
    private Map<String, String> userInfo;
    private Map<String, Object> globalContext;

    private Bugshot(BugshotConfig config) {
        this.config = config;
        this.transport = new HttpTransport(config.getEndpoint(), config.isDebug());
        this.userInfo = new HashMap<>();
        this.globalContext = new HashMap<>();
    }

    /**
     * SDK 초기화
     *
     * @param config SDK 설정
     */
    public static synchronized void init(BugshotConfig config) {
        if (instance != null) {
            log(config.isDebug(), "Bugshot already initialized, reinitializing...");
            instance.shutdown();
        }

        instance = new Bugshot(config);

        // 자동 에러 캡처 설정
        if (config.isEnableAutoCapture()) {
            setupUncaughtExceptionHandler();
        }

        log(config.isDebug(), "Bugshot SDK initialized");
        log(config.isDebug(), "  Endpoint: " + config.getEndpoint());
        log(config.isDebug(), "  Environment: " + config.getEnvironment());
        log(config.isDebug(), "  Auto Capture: " + config.isEnableAutoCapture());
    }

    /**
     * 예외 캡처
     *
     * @param throwable 캡처할 예외
     */
    public static void captureException(Throwable throwable) {
        captureException(throwable, null);
    }

    /**
     * 예외 캡처 (추가 정보 포함)
     *
     * @param throwable 캡처할 예외
     * @param additionalInfo 추가 컨텍스트 정보
     */
    public static void captureException(Throwable throwable, Map<String, Object> additionalInfo) {
        if (instance == null) {
            System.err.println("[Bugshot] SDK not initialized. Call Bugshot.init() first.");
            return;
        }

        instance.doCapture(throwable, additionalInfo);
    }

    /**
     * 메시지 캡처
     *
     * @param message 메시지
     */
    public static void captureMessage(String message) {
        captureException(new Exception(message));
    }

    /**
     * 사용자 정보 설정
     *
     * @param userId 사용자 ID
     * @param email 이메일
     * @param username 사용자 이름
     */
    public static void setUser(String userId, String email, String username) {
        if (instance == null) return;

        instance.userInfo.put("userId", userId);
        if (email != null) instance.userInfo.put("email", email);
        if (username != null) instance.userInfo.put("username", username);

        log(instance.config.isDebug(), "User set: " + userId);
    }

    /**
     * 사용자 정보 초기화
     */
    public static void clearUser() {
        if (instance == null) return;
        instance.userInfo.clear();
    }

    /**
     * 전역 컨텍스트 추가
     *
     * @param key 키
     * @param value 값
     */
    public static void setContext(String key, Object value) {
        if (instance == null) return;
        instance.globalContext.put(key, value);
    }

    /**
     * 전역 컨텍스트 초기화
     */
    public static void clearContext() {
        if (instance == null) return;
        instance.globalContext.clear();
    }

    /**
     * SDK 종료
     */
    public static synchronized void shutdown() {
        if (instance != null) {
            instance.transport.shutdown();
            instance = null;
            System.out.println("[Bugshot] SDK shutdown complete");
        }
    }

    // ============ Private Methods ============

    private void doCapture(Throwable throwable, Map<String, Object> additionalInfo) {
        // 샘플링 체크
        if (!shouldSample()) {
            log(config.isDebug(), "Error not sampled (rate: " + config.getSampleRate() + ")");
            return;
        }

        // 컨텍스트 병합
        Map<String, Object> context = new HashMap<>();
        context.putAll(globalContext);
        context.put("environment", config.getEnvironment());
        if (config.getRelease() != null) {
            context.put("release", config.getRelease());
        }
        if (!userInfo.isEmpty()) {
            context.put("user", new HashMap<>(userInfo));
        }
        if (additionalInfo != null) {
            context.putAll(additionalInfo);
        }

        // Payload 생성 및 전송
        ErrorPayload payload = new ErrorPayload(config.getApiKey(), throwable, context);

        CompletableFuture<Boolean> future = transport.sendAsync(payload);

        if (config.isDebug()) {
            future.thenAccept(success -> {
                if (success) {
                    log(true, "Error captured: " + throwable.getClass().getSimpleName());
                } else {
                    log(true, "Failed to capture error");
                }
            });
        }
    }

    private boolean shouldSample() {
        return random.nextDouble() < config.getSampleRate();
    }

    private static void setupUncaughtExceptionHandler() {
        Thread.UncaughtExceptionHandler existingHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            log(instance.config.isDebug(), "Uncaught exception captured from thread: " + thread.getName());

            // Bugshot로 에러 캡처
            Map<String, Object> context = new HashMap<>();
            context.put("threadName", thread.getName());
            context.put("threadId", thread.getId());
            context.put("uncaught", true);

            instance.doCapture(throwable, context);

            // 기존 핸들러 호출
            if (existingHandler != null) {
                existingHandler.uncaughtException(thread, throwable);
            }
        });

        log(instance.config.isDebug(), "Uncaught exception handler installed");
    }

    private static void log(boolean debug, String message) {
        if (debug) {
            System.out.println("[Bugshot] " + message);
        }
    }
}
