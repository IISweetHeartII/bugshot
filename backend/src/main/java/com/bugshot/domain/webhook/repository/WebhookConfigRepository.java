package com.bugshot.domain.webhook.repository;

import com.bugshot.domain.webhook.entity.WebhookConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 웹훅 설정 저장소
 */
@Repository
public interface WebhookConfigRepository extends JpaRepository<WebhookConfig, String> {

    /**
     * 프로젝트의 웹훅 목록 조회
     */
    List<WebhookConfig> findByProjectId(String projectId);

    /**
     * 프로젝트의 활성화된 웹훅 조회
     */
    List<WebhookConfig> findByProjectIdAndEnabledTrue(String projectId);
}
