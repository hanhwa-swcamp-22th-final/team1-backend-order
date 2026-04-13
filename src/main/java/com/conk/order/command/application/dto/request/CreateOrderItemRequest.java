package com.conk.order.command.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/* 주문 항목 요청 DTO. */
@Getter
public class CreateOrderItemRequest {

  /** SKU 코드 (필수). */
  @NotBlank
  private String sku;

  /** 주문 수량 (필수, 1 이상). */
  @Min(1)
  private int quantity;

  /** 주문 시점 상품명 스냅샷 (선택). */
  private String productNameSnapshot;
}
