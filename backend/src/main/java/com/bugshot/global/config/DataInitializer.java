package com.bugshot.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 데이터 초기화/마이그레이션 실행
 * <p>
 * 기존 데이터의 NULL 값을 기본값으로 업데이트합니다.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Running data initialization...");

        // session_replay_enabled가 NULL인 프로젝트를 TRUE로 업데이트
        int updated = jdbcTemplate.update(
                "UPDATE projects SET session_replay_enabled = true WHERE session_replay_enabled IS NULL"
        );

        if (updated > 0) {
            log.info("Updated {} projects: session_replay_enabled set to TRUE", updated);
        } else {
            log.debug("No projects needed session_replay_enabled update");
        }

        log.info("Data initialization completed");
    }
}
