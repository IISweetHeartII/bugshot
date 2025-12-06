package com.bugshot.global.exception;

import com.bugshot.global.dto.ApiResponse;
import com.bugshot.global.response.ValidationError;
import com.bugshot.global.response.code.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 * 모든 컨트롤러에서 발생하는 예외를 일관된 형식으로 처리합니다.
 *
 * 로그 형식: [예외타입] path: {}, traceId: {}, message: {}
 * - path: 요청 URI
 * - traceId: X-Trace-ID 헤더 (프론트엔드에서 전달, 디버깅용)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 유효성 검증 실패 (Bean Validation - @Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        String traceId = request.getHeader("X-Trace-ID");

        List<ValidationError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ValidationError(
                        fieldError.getField(),
                        fieldError.getRejectedValue(),
                        fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        log.warn("[Validation Failed] path: {}, traceId: {}, errors: {}", path, traceId, errors);

        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.VALIDATION_FAILED, errors, path, traceId));
    }

    /**
     * 유효성 검증 실패 (@Validated - PathVariable, RequestParam)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException e,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        String traceId = request.getHeader("X-Trace-ID");

        String errorMessage = e.getConstraintViolations().stream()
                .findFirst()
                .map(jakarta.validation.ConstraintViolation::getMessage)
                .orElse(ErrorCode.VALIDATION_FAILED.getMessage());

        log.warn("[Constraint Violation] path: {}, traceId: {}, message: {}", path, traceId, errorMessage);

        return ResponseEntity
                .status(ErrorCode.VALIDATION_FAILED.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.VALIDATION_FAILED, errorMessage));
    }

    /**
     * 필수 파라미터 누락
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(
            MissingServletRequestParameterException e,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        String traceId = request.getHeader("X-Trace-ID");
        String errorMessage = String.format("필수 파라미터 '%s'가 누락되었습니다", e.getParameterName());

        log.warn("[Missing Parameter] path: {}, traceId: {}, message: {}", path, traceId, errorMessage);

        return ResponseEntity
                .status(ErrorCode.MISSING_PARAMETER.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.MISSING_PARAMETER, errorMessage));
    }

    /**
     * 파라미터 타입 불일치
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        String traceId = request.getHeader("X-Trace-ID");
        String errorMessage = String.format("'%s' 파라미터의 값 '%s'이(가) 올바르지 않습니다",
                e.getName(), e.getValue());

        log.warn("[Type Mismatch] path: {}, traceId: {}, message: {}", path, traceId, errorMessage);

        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.BAD_REQUEST, errorMessage));
    }

    /**
     * 인증 실패 (로그인 필요)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException e,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        String traceId = request.getHeader("X-Trace-ID");

        log.warn("[Authentication Failed] path: {}, traceId: {}, message: {}", path, traceId, e.getMessage());

        return ResponseEntity
                .status(ErrorCode.UNAUTHORIZED.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.UNAUTHORIZED, path, traceId));
    }

    /**
     * 권한 없음 (접근 거부)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException e,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        String traceId = request.getHeader("X-Trace-ID");

        log.warn("[Access Denied] path: {}, traceId: {}, message: {}", path, traceId, e.getMessage());

        return ResponseEntity
                .status(ErrorCode.FORBIDDEN.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.FORBIDDEN, path, traceId));
    }

    /**
     * 엔드포인트를 찾을 수 없음
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(
            NoHandlerFoundException e,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        String traceId = request.getHeader("X-Trace-ID");
        String errorMessage = String.format("'%s %s' 엔드포인트를 찾을 수 없습니다",
                e.getHttpMethod(), e.getRequestURL());

        log.warn("[Not Found] path: {}, traceId: {}, message: {}", path, traceId, errorMessage);

        return ResponseEntity
                .status(ErrorCode.NOT_FOUND.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.NOT_FOUND, errorMessage));
    }

    /**
     * 잘못된 요청 (IllegalArgumentException)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        String traceId = request.getHeader("X-Trace-ID");

        log.warn("[Illegal Argument] path: {}, traceId: {}, message: {}", path, traceId, e.getMessage());

        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.BAD_REQUEST, e.getMessage()));
    }

    /**
     * 상태 오류 (IllegalStateException)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
            IllegalStateException e,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        String traceId = request.getHeader("X-Trace-ID");

        log.warn("[Illegal State] path: {}, traceId: {}, message: {}", path, traceId, e.getMessage());

        return ResponseEntity
                .status(ErrorCode.CONFLICT.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.CONFLICT, e.getMessage()));
    }

    /**
     * 리소스를 찾을 수 없음
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException e,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        String traceId = request.getHeader("X-Trace-ID");

        log.warn("[Resource Not Found] path: {}, traceId: {}, message: {}", path, traceId, e.getMessage());

        return ResponseEntity
                .status(ErrorCode.NOT_FOUND.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.NOT_FOUND, e.getMessage()));
    }

    /**
     * 비즈니스 로직 예외
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException e,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        String traceId = request.getHeader("X-Trace-ID");

        log.warn("[Business Exception] path: {}, traceId: {}, message: {}", path, traceId, e.getMessage());

        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.BAD_REQUEST, e.getMessage()));
    }

    /**
     * 그 외 모든 예외 (서버 내부 오류)
     * 500 에러는 상세 로그를 남기고, 클라이언트에는 일반 메시지만 전달
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception e,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        String traceId = request.getHeader("X-Trace-ID");

        log.error("[Unexpected Exception] path: {}, traceId: {}, type: {}, message: {}",
                path, traceId, e.getClass().getSimpleName(), e.getMessage(), e);

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR, path, traceId));
    }
}
