package com.bugshot.domain.project.service;

import com.bugshot.domain.auth.entity.User;
import com.bugshot.domain.auth.repository.UserRepository;
import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.repository.ErrorRepository;
import com.bugshot.domain.project.dto.ProjectRequest;
import com.bugshot.domain.project.dto.ProjectResponse;
import com.bugshot.domain.project.entity.Project;
import com.bugshot.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ErrorRepository errorRepository;

    @CacheEvict(value = "userProjects", key = "#userId")
    @Transactional
    public ProjectResponse createProject(String userId, ProjectRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Check if user can create more projects based on plan
        if (!user.canCreateProject()) {
            throw new IllegalStateException("Project limit reached for current plan");
        }

        Project project = Project.builder()
            .user(user)
            .name(request.getName())
            .description(request.getDescription())
            .environment(request.getEnvironment() != null ?
                request.getEnvironment() : Project.Environment.PRODUCTION)
            .sessionReplayEnabled(request.getSessionReplayEnabled() != null ?
                request.getSessionReplayEnabled() : true)
            .sessionReplaySampleRate(request.getSessionReplaySampleRate())
            .build();

        project = projectRepository.save(project);
        log.info("Created project: id={}, name={}, user={}", project.getId(), project.getName(), userId);

        return ProjectResponse.from(project);
    }

    @Cacheable(value = "userProjects", key = "#userId")
    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjects(String userId) {
        List<Project> projects = projectRepository.findByUserId(userId);

        if (projects.isEmpty()) {
            return List.of();
        }

        // 모든 프로젝트의 심각도별 에러 개수를 단일 쿼리로 조회 (N+1 문제 해결)
        List<String> projectIds = projects.stream()
            .map(Project::getId)
            .collect(Collectors.toList());

        Map<String, Map<Error.Severity, Long>> errorCountsByProject = buildErrorCountsMap(
            errorRepository.countErrorsBySeverityForProjects(projectIds)
        );

        return projects.stream()
            .map(project -> {
                Map<Error.Severity, Long> counts = errorCountsByProject.getOrDefault(
                    project.getId(), Map.of()
                );
                return ProjectResponse.fromWithStats(
                    project,
                    counts.getOrDefault(Error.Severity.CRITICAL, 0L),
                    counts.getOrDefault(Error.Severity.HIGH, 0L),
                    counts.getOrDefault(Error.Severity.MEDIUM, 0L),
                    counts.getOrDefault(Error.Severity.LOW, 0L)
                );
            })
            .collect(Collectors.toList());
    }

    /**
     * 쿼리 결과를 Map<projectId, Map<Severity, Count>> 형태로 변환
     */
    private Map<String, Map<Error.Severity, Long>> buildErrorCountsMap(List<Object[]> queryResults) {
        Map<String, Map<Error.Severity, Long>> result = new HashMap<>();

        for (Object[] row : queryResults) {
            String projectId = (String) row[0];
            Error.Severity severity = (Error.Severity) row[1];
            Long count = (Long) row[2];

            result.computeIfAbsent(projectId, k -> new HashMap<>())
                .put(severity, count);
        }

        return result;
    }

    @Cacheable(value = "project", key = "#userId + ':' + #projectId")
    @Transactional(readOnly = true)
    public ProjectResponse getProject(String userId, String projectId) {
        Project project = projectRepository.findByUserIdAndProjectId(userId, projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found or access denied"));

        // 단일 쿼리로 심각도별 에러 개수 조회 (4개 쿼리 → 1개 쿼리)
        Map<Error.Severity, Long> counts = buildSingleProjectErrorCountsMap(
            errorRepository.countErrorsBySeverityForProject(projectId)
        );

        return ProjectResponse.fromWithStats(
            project,
            counts.getOrDefault(Error.Severity.CRITICAL, 0L),
            counts.getOrDefault(Error.Severity.HIGH, 0L),
            counts.getOrDefault(Error.Severity.MEDIUM, 0L),
            counts.getOrDefault(Error.Severity.LOW, 0L)
        );
    }

    /**
     * 단일 프로젝트용 쿼리 결과를 Map<Severity, Count> 형태로 변환
     */
    private Map<Error.Severity, Long> buildSingleProjectErrorCountsMap(List<Object[]> queryResults) {
        Map<Error.Severity, Long> result = new HashMap<>();

        for (Object[] row : queryResults) {
            Error.Severity severity = (Error.Severity) row[0];
            Long count = (Long) row[1];
            result.put(severity, count);
        }

        return result;
    }

    @CacheEvict(value = {"userProjects", "project"}, allEntries = true)
    @Transactional
    public ProjectResponse updateProject(String userId, String projectId, ProjectRequest request) {
        Project project = projectRepository.findByUserIdAndProjectId(userId, projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found or access denied"));

        project.updateSettings(
            request.getName(),
            request.getDescription(),
            request.getEnvironment()
        );

        project.updateSessionReplaySettings(
            request.getSessionReplayEnabled(),
            request.getSessionReplaySampleRate()
        );

        project = projectRepository.save(project);
        log.info("Updated project: id={}", projectId);

        return ProjectResponse.from(project);
    }

    @CacheEvict(value = {"userProjects", "project"}, allEntries = true)
    @Transactional
    public void deleteProject(String userId, String projectId) {
        Project project = projectRepository.findByUserIdAndProjectId(userId, projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found or access denied"));

        projectRepository.delete(project);
        log.info("Deleted project: id={}, user={}", projectId, userId);
    }

    @CacheEvict(value = {"userProjects", "project"}, allEntries = true)
    @Transactional
    public String regenerateApiKey(String userId, String projectId) {
        Project project = projectRepository.findByUserIdAndProjectId(userId, projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found or access denied"));

        String newApiKey = project.regenerateApiKey();
        projectRepository.save(project);

        log.info("Regenerated API key for project: id={}", projectId);
        return newApiKey;
    }
}
