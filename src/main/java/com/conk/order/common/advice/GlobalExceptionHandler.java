package com.conk.order.common.advice;

import com.conk.order.common.dto.ApiResponse;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * 애플리케이션 전역 예외를 API 응답 형식으로 변환하는 핸들러다.
 */
/* 컨트롤러마다 흩어져 있던 예외 응답 포맷을 한 곳에서 통일한다. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /* X-User-Id 헤더 누락 → 401 Unauthorized. 그 외 헤더 누락 → 400. */
  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
    if ("X-User-Id".equals(ex.getHeaderName())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.failure("UNAUTHORIZED", "인증 정보가 없습니다. X-User-Id 헤더가 필요합니다."));
    }
    return ResponseEntity.badRequest()
        .body(ApiResponse.failure(ErrorCode.INVALID_INPUT.getCode(),
            "필수 헤더가 누락되었습니다: " + ex.getHeaderName()));
  }

  /* 비즈니스 예외 — ErrorCode 에 정의된 status/code/message 로 응답한다. */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
    ErrorCode errorCode = ex.getErrorCode();
    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiResponse.failure(errorCode.getCode(), ex.getMessage()));
  }

  /* @Valid 검증 실패 — 필드별 에러 메시지를 하나의 문자열로 합쳐서 반환한다. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(
      MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage())
        .collect(Collectors.joining(", "));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.failure(ErrorCode.INVALID_INPUT.getCode(), message));
  }

  /* @RequestParam 필수값 누락 — 파라미터 이름을 포함해 400 으로 응답한다. */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingParam(
      MissingServletRequestParameterException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.failure(ErrorCode.INVALID_INPUT.getCode(),
            ex.getParameterName() + " 파라미터는 필수입니다."));
  }

  /* multipart 파일 파트 누락 — 파트명을 포함해 400 으로 응답한다. */
  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingPart(
      MissingServletRequestPartException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.failure(ErrorCode.INVALID_INPUT.getCode(),
            ex.getRequestPartName() + " 파일은 필수입니다."));
  }

  /* 그 외 모든 예외 — 상세 내용을 숨기고 500 으로 응답한다. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.failure("SERVER-001", "서버 오류가 발생했습니다."));
  }
}
