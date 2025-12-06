package com.bugshot.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 설정
 * http://localhost:8081/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
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
                                
                                ## 인증
                                - OAuth2 (GitHub, Google)
                                - API Key (에러 수집 엔드포인트)
                                
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
                                .url("https://api.bugshot.log8.kr")
                                .description("프로덕션 서버")
                ));
    }
}
