package com.error.monitor.api.ingest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestResponse {

    private boolean success;

    private String errorId;

    private String message;

    public static IngestResponse success(String errorId) {
        return IngestResponse.builder()
            .success(true)
            .errorId(errorId)
            .message("Error recorded successfully")
            .build();
    }

    public static IngestResponse error(String message) {
        return IngestResponse.builder()
            .success(false)
            .message(message)
            .build();
    }
}
