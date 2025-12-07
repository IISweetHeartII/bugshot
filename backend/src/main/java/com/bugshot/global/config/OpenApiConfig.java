package com.bugshot.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 설정
 * http://localhost:8081/swagger-ui.html
 *
 * 인증 방식:
 * - X-User-Id: 대시보드 API용 (NextAuth에서 전달)
 * - X-API-Key: SDK 에러 수집용
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // X-User-Id 헤더 인증 설정 (대시보드 API)
        String userIdScheme = "X-User-Id";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(userIdScheme);

        Components components = new Components()
                .addSecuritySchemes(userIdScheme, new SecurityScheme()
                        .name("X-User-Id")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .description("NextAuth에서 전달받은 사용자 ID (대시보드 API용)"))
                .addSecuritySchemes("X-API-Key", new SecurityScheme()
                        .name("X-API-Key")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .description("프로젝트 API 키 (SDK 에러 수집용)"));

        return new OpenAPI()
                .info(new Info()
                        .title("Bugshot API")
                        .version("1.0.0")
                        .description("""
                                Bugshot - 실시간 에러 모니터링 및 알림 서비스 🎯

                                ## 주요 기능
                                - 실시간 에러 수집 및 추적
                                - 세션 리플레이 녹화
                                - 대시보드 통계 및 트렌드 분석
                                - Discord/Slack/카카오톡 웹훅 알림
                                - 프로젝트별 API 키 관리

                                ## 인증 방식
                                - **X-User-Id**: 대시보드 API (NextAuth OAuth 인증 후 전달)
                                - **X-API-Key**: SDK 에러 수집 엔드포인트 (/api/ingest/*)

                                ## Rate Limiting
                                - API 키당: 100 requests/minute
                                - IP당: 20 requests/minute
                                """)
                        .contact(new Contact()
                                .name("Bugshot Team")
                                .email("contact@bugshot.log8.kr")
                                .url("https://github.com/bugshot"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("https://bugshot-api.log8.kr")
                                .description("프로덕션 서버")
                ))
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
