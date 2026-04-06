package com.conk.order.command.application.port;

import com.conk.order.command.domain.aggregate.Order;

/*
 * 주문 저장 포트.
 *
 * CreateOrderService 가 의존하는 최소 인터페이스로,
 * 서비스 단위 테스트 시 Stub 구현체로 교체할 수 있다.
 * 프로덕션에서는 OrderRepository 가 이 인터페이스를 구현한다.
 */
public interface OrderSavePort {

  /** 주문번호 중복 여부를 반환한다. */
  boolean existsById(String orderNo);

  /** 주문을 저장한다. */
  void saveOrder(Order order);
}
