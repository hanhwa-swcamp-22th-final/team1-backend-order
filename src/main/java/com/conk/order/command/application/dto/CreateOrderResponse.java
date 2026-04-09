package com.conk.order.command.application.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/* 주문 등록 응답 DTO. */
@Getter
@RequiredArgsConstructor
public class CreateOrderResponse {

  /** 생성된 주문번호. */
  private final String orderId;
}
