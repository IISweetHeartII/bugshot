package com.error.monitor.api.ingest;

import com.error.monitor.api.ingest.dto.IngestRequest;
import com.error.monitor.api.ingest.dto.IngestResponse;
import com.error.monitor.global.dto.ApiResponse;
import com.error.monitor.service.ErrorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingest")
@RequiredArgsConstructor
@Slf4j
public class IngestController {

    private final ErrorService errorService;

    /**
     * 에러 수집 API - SDK에서 호출
     *
     * POST /api/ingest
     */
    @PostMapping
    public ResponseEntity<ApiResponse<IngestResponse>> ingestError(@Valid @RequestBody IngestRequest request) {
        IngestResponse response = errorService.ingestError(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 헬스 체크 엔드포인트
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }
}
