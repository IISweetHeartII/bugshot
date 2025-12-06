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

import java.util.List;
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

        return projects.stream()
            .map(project -> {
                long criticalCount = errorRepository.countByProjectIdAndSeverity(
                    project.getId(), Error.Severity.CRITICAL);
                long highCount = errorRepository.countByProjectIdAndSeverity(
                    project.getId(), Error.Severity.HIGH);
                long mediumCount = errorRepository.countByProjectIdAndSeverity(
                    project.getId(), Error.Severity.MEDIUM);
                long lowCount = errorRepository.countByProjectIdAndSeverity(
                    project.getId(), Error.Severity.LOW);

                return ProjectResponse.fromWithStats(project, criticalCount, highCount, mediumCount, lowCount);
            })
            .collect(Collectors.toList());
    }

    @Cacheable(value = "project", key = "#userId + ':' + #projectId")
    @Transactional(readOnly = true)
    public ProjectResponse getProject(String userId, String projectId) {
        Project project = projectRepository.findByUserIdAndProjectId(userId, projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found or access denied"));

        long criticalCount = errorRepository.countByProjectIdAndSeverity(
            projectId, Error.Severity.CRITICAL);
        long highCount = errorRepository.countByProjectIdAndSeverity(
            projectId, Error.Severity.HIGH);
        long mediumCount = errorRepository.countByProjectIdAndSeverity(
            projectId, Error.Severity.MEDIUM);
        long lowCount = errorRepository.countByProjectIdAndSeverity(
            projectId, Error.Severity.LOW);

        return ProjectResponse.fromWithStats(project, criticalCount, highCount, mediumCount, lowCount);
    }

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

    @Transactional
    public void deleteProject(String userId, String projectId) {
        Project project = projectRepository.findByUserIdAndProjectId(userId, projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found or access denied"));

        projectRepository.delete(project);
        log.info("Deleted project: id={}, user={}", projectId, userId);
    }

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
