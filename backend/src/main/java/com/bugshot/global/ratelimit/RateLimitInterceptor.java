package com.bugshot.global.ratelimit;

import com.bugshot.global.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Rate Limiting Interceptor
 * 특정 엔드포인트에 대한 요청 제한
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Ingest API에 대한 Rate Limit 적용
        if (request.getRequestURI().startsWith("/api/ingest")) {
            String apiKey = extractApiKey(request);

            if (apiKey != null) {
                // API 키 기반 Rate Limit
                if (!rateLimitService.allowRequest(apiKey)) {
                    sendRateLimitError(response, "API 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
                    return false;
                }
            } else {
                // IP 기반 Rate Limit (API 키 없는 경우)
                String ipAddress = getClientIpAddress(request);
                if (!rateLimitService.allowRequestByIp(ipAddress)) {
                    sendRateLimitError(response, "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Rate Limit 초과 에러 응답 전송
     */
    private void sendRateLimitError(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> errorResponse = ApiResponse.error(message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Request에서 API 키 추출
     */
    private String extractApiKey(HttpServletRequest request) {
        // Header에서 추출
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null) {
            return apiKey;
        }

        // Body에서 추출 시도 (필요시)
        // 실제로는 Body를 읽으면 Controller에서 다시 읽을 수 없으므로
        // Request Body를 캐싱하는 Wrapper를 사용해야 함
        // 여기서는 Header 방식만 지원

        return null;
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
