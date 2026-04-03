package com.conk.order.query.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/*
 * API 공통 응답 래퍼.
 *
 * - 조회 API: success(data) — message 는 null 이므로 JSON 에 포함되지 않는다.
 * - 생성 API: created(message, data) — { success, message, data } 형태로 직렬화된다.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // null 인 필드는 JSON 응답에 넣지 않겠다는 어노테이션
public class ApiResponse<T> {

  private final boolean success;
  private final String message;
  private final T data;

  private ApiResponse(boolean success, String message, T data) {
    this.success = success;
    this.message = message;
    this.data = data;
  }

  /* 조회 API 응답. { success: true, data: {...} } */
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, null, data);
  }

  /* 생성 API 응답. { success: true, message: "...", data: {...} } */
  public static <T> ApiResponse<T> created(String message, T data) {
    return new ApiResponse<>(true, message, data);
  }
}
