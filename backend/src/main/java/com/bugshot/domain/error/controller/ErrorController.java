package com.bugshot.domain.error.controller;

import com.bugshot.domain.error.dto.ErrorResponse;
import com.bugshot.domain.error.entity.Error;
import com.bugshot.domain.error.repository.ErrorRepository;
import com.bugshot.domain.error.service.ErrorService;
import com.bugshot.global.dto.ApiResponse;
import com.bugshot.global.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/errors")
@RequiredArgsConstructor
@Slf4j
public class ErrorController {

    private final ErrorService errorService;
    private final ErrorRepository errorRepository;

    /**
     * 에러 목록 조회
     * GET /api/errors?projectId=xxx&status=unresolved&page=0&size=20&sort=priority
     */
    @GetMapping
    public ResponseEntity<PageResponse<ErrorResponse>> getErrors(
        @RequestParam String projectId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String severity,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "priority") String sort
    ) {
        Sort sortOrder = switch (sort) {
            case "priority" -> Sort.by(Sort.Direction.DESC, "priorityScore", "lastSeenAt");
            case "recent" -> Sort.by(Sort.Direction.DESC, "lastSeenAt");
            case "count" -> Sort.by(Sort.Direction.DESC, "occurrenceCount");
            default -> Sort.by(Sort.Direction.DESC, "priorityScore");
        };

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<Error> errorPage;
        if (status != null) {
            Error.ErrorStatus errorStatus = Error.ErrorStatus.valueOf(status.toUpperCase());
            errorPage = errorRepository.findByProjectIdAndStatus(projectId, errorStatus, pageable);
        } else {
            errorPage = errorRepository.findByProjectId(projectId, pageable);
        }

        Page<ErrorResponse> responsePage = errorPage.map(ErrorResponse::from);
        return ResponseEntity.ok(PageResponse.success(responsePage));
    }

    /**
     * 에러 상세 조회
     * GET /api/errors/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ErrorResponse>> getError(@PathVariable String id) {
        Error error = errorService.getError(id);
        return ResponseEntity.ok(ApiResponse.success(ErrorResponse.from(error)));
    }

    /**
     * 에러 해결 표시
     * PUT /api/errors/{id}/resolve
     */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolveError(
        @PathVariable String id,
        @RequestParam String userId
    ) {
        errorService.resolveError(id, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 에러 무시
     * PUT /api/errors/{id}/ignore
     */
    @PutMapping("/{id}/ignore")
    public ResponseEntity<ApiResponse<Void>> ignoreError(@PathVariable String id) {
        errorService.ignoreError(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 에러 재오픈
     * PUT /api/errors/{id}/reopen
     */
    @PutMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<Void>> reopenError(@PathVariable String id) {
        errorService.reopenError(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
