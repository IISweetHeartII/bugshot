package com.bugshot.domain.project.dto;

import com.bugshot.domain.project.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {

    private String id;
    private String name;
    private String description;
    private String apiKey;
    private String environment;
    private Boolean sessionReplayEnabled;
    private BigDecimal sessionReplaySampleRate;
    private StatsInfo stats;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatsInfo {
        private Integer totalErrors;
        private Integer totalUsersAffected;
        private Long criticalCount;
        private Long highCount;
        private Long mediumCount;
        private Long lowCount;
        private LocalDateTime lastErrorAt;
    }

    public static ProjectResponse from(Project project) {
        return ProjectResponse.builder()
            .id(project.getId())
            .name(project.getName())
            .description(project.getDescription())
            .apiKey(project.getApiKey())
            .environment(project.getEnvironment().name())
            .sessionReplayEnabled(project.getSessionReplayEnabled())
            .sessionReplaySampleRate(project.getSessionReplaySampleRate())
            .stats(StatsInfo.builder()
                .totalErrors(project.getTotalErrors())
                .totalUsersAffected(project.getTotalUsersAffected())
                .lastErrorAt(project.getLastErrorAt())
                .build())
            .createdAt(project.getCreatedAt())
            .updatedAt(project.getUpdatedAt())
            .build();
    }

    public static ProjectResponse fromWithStats(Project project,
                                                  long criticalCount,
                                                  long highCount,
                                                  long mediumCount,
                                                  long lowCount) {
        ProjectResponse response = from(project);
        response.getStats().setCriticalCount(criticalCount);
        response.getStats().setHighCount(highCount);
        response.getStats().setMediumCount(mediumCount);
        response.getStats().setLowCount(lowCount);
        return response;
    }
}
