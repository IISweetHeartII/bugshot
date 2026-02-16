package com.bugshot.domain.error.controller;

import com.bugshot.domain.error.dto.IngestRequest;
import com.bugshot.domain.error.dto.IngestResponse;
import com.bugshot.domain.error.service.ErrorService;
import com.bugshot.global.dto.ApiResponse;
import com.bugshot.global.ratelimit.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingest")
@RequiredArgsConstructor
@Slf4j
public class IngestController {

    private final ErrorService errorService;
    private final RateLimitService rateLimitService;

    /**
     * 에러 수집 API - SDK에서 호출
     *
     * POST /api/ingest
     */
    @PostMapping
    public ResponseEntity<ApiResponse<IngestResponse>> ingestError(
            @Valid @RequestBody IngestRequest request,
            HttpServletRequest httpRequest) {

        // Rate Limiting: API 키 기반 체크
        if (!rateLimitService.allowRequest(request.getApiKey())) {
            log.warn("Rate limit exceeded for API key: {}",
                    request.getApiKey().substring(0, Math.min(8, request.getApiKey().length())) + "...");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Rate limit exceeded. Please try again later."));
        }

        // Rate Limiting: IP 주소 기반 체크 (추가 보안)
        String ipAddress = getClientIpAddress(httpRequest);
        if (!rateLimitService.allowRequestByIp(ipAddress)) {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Rate limit exceeded. Please try again later."));
        }

        IngestResponse response = errorService.ingestError(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 헬스 체크 엔드포인트
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    /**
     * 클라이언트 IP 주소 추출 (프록시/로드밸런서 고려)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, get the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
