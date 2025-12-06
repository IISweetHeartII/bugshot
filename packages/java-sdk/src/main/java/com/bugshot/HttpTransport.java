package com.bugshot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTTP Transport - 에러를 서버로 전송하는 클래스
 */
public class HttpTransport {

    private final HttpClient httpClient;
    private final String endpoint;
    private final boolean debug;
    private final ExecutorService executor;

    public HttpTransport(String endpoint, boolean debug) {
        this.endpoint = endpoint;
        this.debug = debug;
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Bugshot-Transport");
            t.setDaemon(true);
            return t;
        });
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .executor(executor)
            .build();
    }

    /**
     * 에러를 서버로 비동기 전송
     */
    public CompletableFuture<Boolean> sendAsync(ErrorPayload payload) {
        String url = endpoint + "/api/ingest";
        String json = payload.toJson();

        if (debug) {
            System.out.println("[Bugshot] Sending error to: " + url);
            System.out.println("[Bugshot] Payload: " + json);
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .timeout(Duration.ofSeconds(30))
            .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (debug) {
                    System.out.println("[Bugshot] Response status: " + response.statusCode());
                    System.out.println("[Bugshot] Response body: " + response.body());
                }

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    if (debug) {
                        System.out.println("[Bugshot] Error sent successfully");
                    }
                    return true;
                } else {
                    System.err.println("[Bugshot] Failed to send error: HTTP " + response.statusCode());
                    return false;
                }
            })
            .exceptionally(e -> {
                if (debug) {
                    System.err.println("[Bugshot] Failed to send error: " + e.getMessage());
                    e.printStackTrace();
                }
                return false;
            });
    }

    /**
     * 에러를 서버로 동기 전송
     */
    public boolean sendSync(ErrorPayload payload) {
        try {
            return sendAsync(payload).get();
        } catch (Exception e) {
            if (debug) {
                System.err.println("[Bugshot] Sync send failed: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Transport 종료
     */
    public void shutdown() {
        executor.shutdown();
    }
}
