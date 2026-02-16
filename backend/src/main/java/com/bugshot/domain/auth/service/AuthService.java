package com.bugshot.domain.auth.service;

import com.bugshot.domain.auth.dto.OAuthLoginRequest;
import com.bugshot.domain.auth.dto.OAuthLoginResponse;
import com.bugshot.domain.auth.dto.UsageStatsResponse;
import com.bugshot.domain.auth.entity.User;
import com.bugshot.domain.auth.repository.UserRepository;
import com.bugshot.domain.error.repository.ErrorOccurrenceRepository;
import com.bugshot.domain.project.entity.Project;
import com.bugshot.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ErrorOccurrenceRepository errorOccurrenceRepository;

    @Transactional
    public OAuthLoginResponse processOAuthLogin(OAuthLoginRequest request) {
        log.info("Processing OAuth login: provider={}, providerId={}, email={}",
            request.getProvider(), request.getProviderId(), request.getEmail());

        User user;

        // Check if user exists by provider ID
        if (request.getProvider().equalsIgnoreCase("github")) {
            user = userRepository.findByGithubId(request.getProviderId())
                .orElse(null);
        } else if (request.getProvider().equalsIgnoreCase("google")) {
            user = userRepository.findByGoogleId(request.getProviderId())
                .orElse(null);
        } else {
            throw new IllegalArgumentException("Unsupported provider: " + request.getProvider());
        }

        // If user doesn't exist, check by email
        if (user == null) {
            user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        }

        // Create new user or update existing
        if (user == null) {
            user = createNewUser(request);
            log.info("Created new user: id={}, email={}", user.getId(), user.getEmail());
        } else {
            user = updateUserInfo(user, request);
            log.info("Updated existing user: id={}, email={}", user.getId(), user.getEmail());
        }

        return OAuthLoginResponse.success(user);
    }

    @Transactional(readOnly = true)
    public OAuthLoginResponse getUserInfo(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        return OAuthLoginResponse.success(user);
    }

    private User createNewUser(OAuthLoginRequest request) {
        User.UserBuilder builder = User.builder()
            .email(request.getEmail())
            .name(request.getName())
            .avatarUrl(request.getImage())
            .planType(User.PlanType.FREE);

        if (request.getProvider().equalsIgnoreCase("github")) {
            builder.githubId(request.getProviderId());
        } else if (request.getProvider().equalsIgnoreCase("google")) {
            builder.googleId(request.getProviderId());
        }

        return userRepository.save(builder.build());
    }

    private User updateUserInfo(User user, OAuthLoginRequest request) {
        // Update provider IDs if not set
        boolean needsSave = false;
        User.UserBuilder builder = User.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(request.getName() != null ? request.getName() : user.getName())
            .avatarUrl(request.getImage() != null ? request.getImage() : user.getAvatarUrl())
            .planType(user.getPlanType());

        if (request.getProvider().equalsIgnoreCase("github") && user.getGithubId() == null) {
            builder.githubId(request.getProviderId()).googleId(user.getGoogleId());
            needsSave = true;
        } else if (request.getProvider().equalsIgnoreCase("google") && user.getGoogleId() == null) {
            builder.githubId(user.getGithubId()).googleId(request.getProviderId());
            needsSave = true;
        } else {
            builder.githubId(user.getGithubId()).googleId(user.getGoogleId());
        }

        if (needsSave || !user.getName().equals(request.getName()) ||
            !user.getAvatarUrl().equals(request.getImage())) {
            user = userRepository.save(builder.build());
        }

        return user;
    }

    /**
     * 사용량 통계 조회
     */
    @Transactional(readOnly = true)
    public UsageStatsResponse getUsageStats(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 프로젝트 수 조회
        List<Project> projects = projectRepository.findByUserId(userId);
        int projectCount = projects.size();

        // 월간 이벤트 수 조회 (이번 달 1일부터)
        long monthlyEvents = 0;
        if (!projects.isEmpty()) {
            List<String> projectIds = projects.stream()
                    .map(Project::getId)
                    .toList();

            LocalDateTime monthStart = LocalDateTime.now()
                    .with(TemporalAdjusters.firstDayOfMonth())
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);

            monthlyEvents = errorOccurrenceRepository.countByProjectIdsAndSince(projectIds, monthStart);
        }

        return UsageStatsResponse.of(user, projectCount, monthlyEvents);
    }
}
