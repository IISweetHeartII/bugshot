package com.bugshot.domain.replay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 세션 리플레이 응답
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionReplayResponse {
    /**
     * 에러 ID
     */
    private String errorId;

    /**
     * 리플레이 데이터 URL (Cloudflare R2)
     */
    private String replayUrl;

    /**
     * 리플레이 크기 (bytes)
     */
    private Long size;

    /**
     * 녹화 시작 시간
     */
    private LocalDateTime recordedAt;

    /**
     * 리플레이 길이 (초)
     */
    private Integer duration;

    /**
     * 사용자 정보
     */
    private UserInfo userInfo;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String userId;
        private String ip;
        private String userAgent;
        private String browser;
        private String os;
    }
}
