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
     * 프론트엔드와 백엔드가 다른 도메인일 때 필요
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin (도메인) 설정
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",      // Next.js dev server
            "https://bugshot.log8.kr"     // Production frontend
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // 허용할 헤더 (보안을 위해 구체적으로 지정)
        configuration.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-User-Id",         // bugshot 인증 헤더
            "X-API-Key",         // SDK API 키
            "X-Requested-With",
            "Accept",
            "Origin"
        ));

        // 클라이언트가 접근 가능한 응답 헤더
        configuration.setExposedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-RateLimit-Remaining",
            "X-RateLimit-Limit"
        ));

        // 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);

        // Preflight 요청 캐시 시간 (1시간)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
