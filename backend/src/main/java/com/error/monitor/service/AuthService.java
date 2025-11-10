package com.error.monitor.service;

import com.error.monitor.api.auth.dto.OAuthLoginRequest;
import com.error.monitor.api.auth.dto.OAuthLoginResponse;
import com.error.monitor.domain.user.User;
import com.error.monitor.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

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
}
