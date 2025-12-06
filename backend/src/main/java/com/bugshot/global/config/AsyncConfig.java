package com.bugshot.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 비동기 및 스케줄링 설정
 *
 * @EnableAsync: @Async 어노테이션 활성화
 * - Discord/Slack 웹훅 전송 등 시간이 걸리는 작업을 비동기로 처리
 * - API 응답 시간에 영향을 주지 않도록 함
 *
 * @EnableScheduling: @Scheduled 어노테이션 활성화
 * - 대시보드 통계 캐시 갱신 등 주기적인 작업 처리
 *
 * 필요시 ThreadPoolTaskExecutor를 커스터마이징할 수 있습니다.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
}
