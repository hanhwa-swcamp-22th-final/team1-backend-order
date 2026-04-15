package com.conk.order.command.application.dto.request;

import com.conk.order.command.domain.aggregate.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/*
 * 주문 상태 변경 요청 DTO.
 *
 * PATCH /orders/{orderId}/status 요청 바디로 사용한다.
 * { "status": "PICKING", "warehouseId": "WH-001" } 형태로 전달받는다.
 */
@Getter
@Setter
public class UpdateOrderStatusRequest {

  /** 변경할 대상 상태. OrderStatus enum 값. */
  @NotNull(message = "변경할 상태는 필수입니다.")
  private OrderStatus status;

  /** 상태 변경과 함께 반영할 창고 ID. */
  private String warehouseId;
}
