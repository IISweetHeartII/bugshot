package com.bugshot.global.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 Rate Limiting 서비스
 * <p>
 * Sliding Window Counter 알고리즘을 사용하여 분산 환경에서도
 * 정확한 요청 제한을 적용합니다.
 * </p>
 *
 * <pre>
 * 이전 구현 (ConcurrentHashMap):
 * - 단일 서버에서만 동작
 * - 서버 재시작 시 초기화
 * - 서버 2대 운영 시 제한이 2배로 적용
 *
 * 현재 구현 (Redis):
 * - 분산 환경에서 동작
 * - 서버 재시작해도 상태 유지
 * - 모든 서버가 동일한 제한 공유
 * </pre>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    private static final int API_KEY_LIMIT = 100;        // 분당 100회
    private static final int IP_LIMIT = 20;              // 분당 20회
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    /**
     * API 키별 Rate Limit 확인
     * - 분당 100회 요청 제한
     *
     * @param apiKey API 키
     * @return 요청 허용 여부
     */
    public boolean allowRequest(String apiKey) {
        String key = RATE_LIMIT_PREFIX + "apikey:" + apiKey;
        boolean allowed = checkAndIncrement(key, API_KEY_LIMIT);

        if (!allowed) {
            log.warn("Rate limit exceeded for API key: {}...", apiKey.substring(0, Math.min(8, apiKey.length())));
        }

        return allowed;
    }

    /**
     * IP별 Rate Limit 확인 (API 키 없는 엔드포인트용)
     * - 분당 20회 요청 제한
     *
     * @param ipAddress IP 주소
     * @return 요청 허용 여부
     */
    public boolean allowRequestByIp(String ipAddress) {
        String key = RATE_LIMIT_PREFIX + "ip:" + ipAddress;
        boolean allowed = checkAndIncrement(key, IP_LIMIT);

        if (!allowed) {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
        }

        return allowed;
    }

    /**
     * Redis를 사용한 Sliding Window Counter 구현
     * <p>
     * INCR 명령으로 카운터를 증가시키고, 첫 요청 시 TTL을 설정합니다.
     * TTL이 만료되면 자동으로 카운터가 초기화됩니다.
     * </p>
     *
     * @param key   Redis 키
     * @param limit 요청 제한 수
     * @return 요청 허용 여부
     */
    private boolean checkAndIncrement(String key, int limit) {
        try {
            // INCR: 키가 없으면 0으로 초기화 후 1 증가
            Long currentCount = redisTemplate.opsForValue().increment(key);

            if (currentCount == null) {
                log.error("Redis increment returned null for key: {}", key);
                return true; // Redis 오류 시 요청 허용 (fail-open)
            }

            // 첫 요청이면 TTL 설정
            if (currentCount == 1) {
                redisTemplate.expire(key, WINDOW_DURATION.toSeconds(), TimeUnit.SECONDS);
                log.debug("Rate limit window started for key: {}", key);
            }

            boolean allowed = currentCount <= limit;

            if (log.isDebugEnabled()) {
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                log.debug("Rate limit check: key={}, count={}/{}, ttl={}s, allowed={}",
                        key, currentCount, limit, ttl, allowed);
            }

            return allowed;

        } catch (Exception e) {
            log.error("Redis rate limit check failed for key: {}", key, e);
            return true; // Redis 오류 시 요청 허용 (fail-open 정책)
        }
    }

    /**
     * 현재 요청 카운트 조회 (모니터링/디버깅용)
     *
     * @param apiKey API 키
     * @return 현재 요청 카운트 (키가 없으면 0)
     */
    public long getCurrentCount(String apiKey) {
        String key = RATE_LIMIT_PREFIX + "apikey:" + apiKey;
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0;
    }

    /**
     * 남은 요청 수 조회
     *
     * @param apiKey API 키
     * @return 남은 요청 수
     */
    public long getRemainingRequests(String apiKey) {
        long currentCount = getCurrentCount(apiKey);
        return Math.max(0, API_KEY_LIMIT - currentCount);
    }

    /**
     * 특정 키의 Rate Limit 초기화 (관리/테스트용)
     *
     * @param key Rate limit 키
     */
    public void reset(String key) {
        String fullKey = key.startsWith(RATE_LIMIT_PREFIX) ? key : RATE_LIMIT_PREFIX + key;
        redisTemplate.delete(fullKey);
        log.info("Rate limit reset for key: {}", fullKey);
    }

    /**
     * API 키의 Rate Limit 초기화
     *
     * @param apiKey API 키
     */
    public void resetApiKey(String apiKey) {
        reset("apikey:" + apiKey);
    }

    /**
     * IP의 Rate Limit 초기화
     *
     * @param ipAddress IP 주소
     */
    public void resetIp(String ipAddress) {
        reset("ip:" + ipAddress);
    }
}
