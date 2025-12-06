package com.bugshot.global.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting 서비스
 * Bucket4j를 사용한 Token Bucket 알고리즘 구현
 */
@Service
@Slf4j
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * API 키별 Rate Limit 확인
     * - 분당 100회 요청 제한
     */
    public boolean allowRequest(String apiKey) {
        Bucket bucket = resolveBucket(apiKey);
        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            log.warn("Rate limit exceeded for API key: {}", apiKey.substring(0, 8) + "...");
        }

        return allowed;
    }

    /**
     * IP별 Rate Limit 확인 (API 키 없는 엔드포인트용)
     * - 분당 20회 요청 제한
     */
    public boolean allowRequestByIp(String ipAddress) {
        Bucket bucket = resolveIpBucket(ipAddress);
        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
        }

        return allowed;
    }

    /**
     * API 키별 Bucket 생성 또는 조회
     * - 용량: 100 토큰
     * - 리필: 분당 100 토큰
     */
    private Bucket resolveBucket(String apiKey) {
        return cache.computeIfAbsent(apiKey, k -> createNewBucket(100, Duration.ofMinutes(1)));
    }

    /**
     * IP별 Bucket 생성 또는 조회
     * - 용량: 20 토큰
     * - 리필: 분당 20 토큰
     */
    private Bucket resolveIpBucket(String ipAddress) {
        return cache.computeIfAbsent("ip:" + ipAddress, k -> createNewBucket(20, Duration.ofMinutes(1)));
    }

    /**
     * 새로운 Bucket 생성
     */
    private Bucket createNewBucket(int capacity, Duration refillDuration) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, refillDuration));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * 특정 키의 Rate Limit 초기화 (테스트용)
     */
    public void reset(String key) {
        cache.remove(key);
    }

    /**
     * 모든 Rate Limit 초기화
     */
    public void resetAll() {
        cache.clear();
        log.info("All rate limits have been reset");
    }
}
