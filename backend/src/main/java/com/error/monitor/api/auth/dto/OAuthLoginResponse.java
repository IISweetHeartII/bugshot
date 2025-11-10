package com.error.monitor.api.auth.dto;

import com.error.monitor.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthLoginResponse {

    private boolean success;
    private String message;
    private UserData user;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserData {
        private String id;
        private String email;
        private String name;
        private String profileImage;
        private String planType;
        private Integer projectLimit;
        private Integer eventLimit;
        private Integer replayRetentionDays;
    }

    public static OAuthLoginResponse success(User user) {
        // Get limits based on plan type
        int projectLimit = switch (user.getPlanType()) {
            case FREE -> 3;
            case PRO -> 10;
            case TEAM -> 50;
        };

        int eventLimit = switch (user.getPlanType()) {
            case FREE -> 10000;
            case PRO -> 100000;
            case TEAM -> 1000000;
        };

        int replayRetentionDays = switch (user.getPlanType()) {
            case FREE -> 7;
            case PRO -> 30;
            case TEAM -> 90;
        };

        return OAuthLoginResponse.builder()
            .success(true)
            .message("Login successful")
            .user(UserData.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImage(user.getAvatarUrl())
                .planType(user.getPlanType().name())
                .projectLimit(projectLimit)
                .eventLimit(eventLimit)
                .replayRetentionDays(replayRetentionDays)
                .build())
            .build();
    }

    public static OAuthLoginResponse error(String message) {
        return OAuthLoginResponse.builder()
            .success(false)
            .message(message)
            .build();
    }
}
