package com.bugshot.global.config;

import com.bugshot.global.security.UserIdAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 설정
 *
 * 인증 흐름:
 * 1. Frontend(NextAuth)에서 GitHub/Google OAuth 처리
 * 2. 로그인 성공 시 /api/auth/oauth 호출하여 백엔드에 사용자 등록
 * 3. 이후 API 요청 시 X-User-Id 헤더로 사용자 식별
 * 4. UserIdAuthenticationFilter가 헤더를 읽어 Authentication 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserIdAuthenticationFilter userIdAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/ingest/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/error").permitAll()

                // Swagger UI
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-resources/**").permitAll()

                // Require authentication for all other endpoints
                .anyRequest().authenticated()
            )
            // X-User-Id 헤더 기반 인증 필터 추가
            .addFilterBefore(userIdAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS (Cross-Origin Resource Sharing) 설정
     * - Ingest API: 모든 origin 허용 (SDK가 어디서든 에러 전송 가능)
     * - Dashboard API: 특정 도메인만 허용 (보안)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 1. Ingest API: 모든 origin 허용 (SDK용)
        // Sentry, LogRocket 등과 동일한 방식
        CorsConfiguration ingestConfig = new CorsConfiguration();
        ingestConfig.setAllowedOriginPatterns(List.of("*"));
        ingestConfig.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        ingestConfig.setAllowedHeaders(List.of("*"));
        ingestConfig.setAllowCredentials(false); // allowedOriginPatterns("*") 사용시 false 필요
        ingestConfig.setMaxAge(3600L);
        source.registerCorsConfiguration("/api/ingest/**", ingestConfig);

        // 2. Dashboard API: 특정 도메인만 허용
        CorsConfiguration dashboardConfig = new CorsConfiguration();
        dashboardConfig.setAllowedOrigins(List.of(
            "http://localhost:3000",      // Next.js dev server
            "http://localhost:8081",      // Backend dev server
            "http://localhost:4321",      // Astro dev server
            "https://bugshot.log8.kr",    // Production frontend
            "https://bugshot-api.log8.kr", // Swagger UI
            "https://log8.kr"             // Blog (SDK integration)
        ));
        dashboardConfig.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        dashboardConfig.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-User-Id",
            "X-API-Key",
            "X-Requested-With",
            "Accept",
            "Origin"
        ));
        dashboardConfig.setExposedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-RateLimit-Remaining",
            "X-RateLimit-Limit"
        ));
        dashboardConfig.setAllowCredentials(true);
        dashboardConfig.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", dashboardConfig);

        return source;
    }
}
