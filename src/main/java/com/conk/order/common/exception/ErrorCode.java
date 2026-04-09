package com.conk.order.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 서비스 전반에서 사용하는 비즈니스 에러 코드와 메시지를 모아둔 enum이다.
 */
/* 공통 비즈니스 에러 코드 모음.
 * 현재는 Order 흐름부터 적용했고, 이후 다른 서비스도 같은 패턴으로 확장할 수 있다. */
@Getter
public enum ErrorCode {

  /* 공통 */
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON-001", "잘못된 입력값입니다."),

  /* 주문 (Order) */
  ORDER_NO_REQUIRED(HttpStatus.BAD_REQUEST, "ORD-001", "주문번호는 필수입니다."),
  ORDER_ALREADY_EXISTS(HttpStatus.CONFLICT, "ORD-002", "이미 존재하는 주문번호입니다."),
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORD-003", "주문을 찾을 수 없습니다."),
  ORDER_CANCEL_NOT_ALLOWED(HttpStatus.CONFLICT, "ORD-004", "현재 상태에서는 주문을 취소할 수 없습니다."),
  ORDER_STATUS_TRANSITION_NOT_ALLOWED(HttpStatus.CONFLICT, "ORD-005", "허용되지 않는 상태 전이입니다."),

  /* 주문 항목 (OrderItem) */
  ORDER_ITEM_SKU_REQUIRED(HttpStatus.BAD_REQUEST, "ORD-101", "SKU는 필수입니다."),
  ORDER_ITEM_QUANTITY_INVALID(HttpStatus.BAD_REQUEST, "ORD-102", "주문 수량은 1 이상이어야 합니다."),

  /* 엑셀 일괄 등록 (Bulk) */
  BULK_FILE_UNREADABLE(HttpStatus.BAD_REQUEST, "ORD-201", "엑셀 파일을 읽을 수 없습니다."),
  BULK_FILE_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "ORD-202", "xlsx 형식의 파일만 업로드할 수 있습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;

  ErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }

}
