package com.bugshot.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * BFF 패턴을 위한 인증 필터
 *
 * Next.js 서버에서 세션을 검증한 후, X-Internal-Secret 헤더와 함께
 * X-User-Id를 전송합니다. 이 필터는 시크릿이 일치하는 경우에만
 * X-User-Id를 신뢰하여 Authentication을 설정합니다.
 *
 * 보안:
 * - X-Internal-Secret이 없거나 틀리면 X-User-Id를 무시합니다
 * - 시크릿은 서버 간 통신에서만 사용되므로 클라이언트에 노출되지 않습니다
 */
@Component
@Slf4j
public class UserIdAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    @Value("${app.internal-api-secret:}")
    private String internalApiSecret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String userId = request.getHeader(USER_ID_HEADER);
        String providedSecret = request.getHeader(INTERNAL_SECRET_HEADER);

        // 시크릿이 설정되어 있고 일치하는 경우에만 X-User-Id 신뢰
        if (userId != null && !userId.isBlank() && isValidInternalSecret(providedSecret)) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Set authentication for user: {} (via BFF)", userId);
        } else if (userId != null && !userId.isBlank()) {
            // 시크릿 없이 X-User-Id만 보낸 경우 (보안 위반 시도)
            log.warn("Rejected X-User-Id header without valid internal secret from IP: {}",
                    getClientIp(request));
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidInternalSecret(String providedSecret) {
        // 시크릿이 설정되지 않은 경우 (개발 환경) - 모든 요청 허용
        if (internalApiSecret == null || internalApiSecret.isBlank()) {
            log.warn("INTERNAL_API_SECRET is not configured. All X-User-Id headers are trusted. " +
                    "This is only acceptable in development environment!");
            return true;
        }

        return internalApiSecret.equals(providedSecret);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Public endpoints - 필터 스킵
        return path.startsWith("/api/ingest") ||
               path.startsWith("/api/auth") ||
               path.startsWith("/actuator") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/error");
    }
}
