package com.bugshot.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson ObjectMapper 설정
 *
 * 모든 LocalDateTime을 ISO-8601 UTC 형식으로 직렬화합니다.
 * 예: "2025-12-07T13:18:28" → "2025-12-07T13:18:28Z"
 *
 * 이렇게 하면 프론트엔드에서 JavaScript Date가 UTC로 정확히 해석합니다.
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter UTC_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // LocalDateTime을 UTC 'Z' 접미사와 함께 직렬화
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(UTC_FORMATTER));

        mapper.registerModule(javaTimeModule);

        // ISO-8601 형식으로 날짜 직렬화 (timestamp 숫자 대신)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
