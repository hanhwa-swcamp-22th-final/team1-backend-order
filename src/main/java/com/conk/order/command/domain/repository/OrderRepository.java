package com.conk.order.command.domain.repository;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.command.application.port.OrderSavePort;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * JpaRepository 가 existsById 를 제공하므로 OrderSavePort 의 중복 체크는 별도 구현이 필요 없다.
 * saveOrder() 는 JpaRepository.save() 를 위임해 이름 충돌을 피한다.
 */
public interface OrderRepository extends JpaRepository<Order, String>, OrderSavePort {

  Long countByStatus(OrderStatus status);

  @Override
  default void saveOrder(Order order) {
    save(order);
  }
}
