package com.bugshot.domain.project.controller;

import com.bugshot.domain.project.dto.ProjectRequest;
import com.bugshot.domain.project.dto.ProjectResponse;
import com.bugshot.domain.project.service.ProjectService;
import com.bugshot.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 프로젝트 목록 조회
     * GET /api/projects
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjects(Authentication authentication) {
        String userId = authentication.getName();
        List<ProjectResponse> projects = projectService.getUserProjects(userId);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    /**
     * 프로젝트 생성
     * POST /api/projects
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
        @Valid @RequestBody ProjectRequest request,
        Authentication authentication
    ) {
        String userId = authentication.getName();
        ProjectResponse project = projectService.createProject(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(project));
    }

    /**
     * 프로젝트 상세 조회
     * GET /api/projects/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
        @PathVariable String id,
        Authentication authentication
    ) {
        String userId = authentication.getName();
        ProjectResponse project = projectService.getProject(userId, id);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    /**
     * 프로젝트 수정
     * PUT /api/projects/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
        @PathVariable String id,
        @Valid @RequestBody ProjectRequest request,
        Authentication authentication
    ) {
        String userId = authentication.getName();
        ProjectResponse project = projectService.updateProject(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    /**
     * 프로젝트 삭제
     * DELETE /api/projects/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
        @PathVariable String id,
        Authentication authentication
    ) {
        String userId = authentication.getName();
        projectService.deleteProject(userId, id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * API 키 재생성
     * POST /api/projects/{id}/regenerate-key
     */
    @PostMapping("/{id}/regenerate-key")
    public ResponseEntity<ApiResponse<Map<String, String>>> regenerateApiKey(
        @PathVariable String id,
        Authentication authentication
    ) {
        String userId = authentication.getName();
        String newApiKey = projectService.regenerateApiKey(userId, id);
        return ResponseEntity.ok(ApiResponse.success(Map.of("apiKey", newApiKey)));
    }
}
