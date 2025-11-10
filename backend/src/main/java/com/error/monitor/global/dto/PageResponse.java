package com.error.monitor.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 페이지네이션이 포함된 API 응답 포맷
 */
@Getter
public class PageResponse<T> {
    private boolean success;
    private List<T> data;
    private Pagination pagination;
    private String message;
    private LocalDateTime timestamp;

    public PageResponse(boolean success, List<T> data, Pagination pagination, String message) {
        this.success = success;
        this.data = data;
        this.pagination = pagination;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Spring Data Page 객체로부터 성공 응답 생성
     */
    public static <T> PageResponse<T> success(Page<T> page) {
        Pagination pagination = new Pagination(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
        return new PageResponse<>(true, page.getContent(), pagination, null);
    }

    /**
     * 커스텀 페이지네이션 정보로 성공 응답 생성
     */
    public static <T> PageResponse<T> success(List<T> data, int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        Pagination pagination = new Pagination(page, size, total, totalPages);
        return new PageResponse<>(true, data, pagination, null);
    }

    /**
     * 에러 응답 생성
     */
    public static <T> PageResponse<T> error(String message) {
        return new PageResponse<>(false, null, null, message);
    }

    /**
     * 페이지네이션 정보
     */
    @Getter
    @AllArgsConstructor
    public static class Pagination {
        /**
         * 현재 페이지 번호 (0부터 시작)
         */
        private int page;

        /**
         * 페이지 크기
         */
        private int size;

        /**
         * 전체 항목 수
         */
        private long total;

        /**
         * 전체 페이지 수
         */
        private int totalPages;
    }
}
