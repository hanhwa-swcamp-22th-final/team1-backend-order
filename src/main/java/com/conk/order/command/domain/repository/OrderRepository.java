package com.conk.order.command.domain.repository;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
 * 주문 저장소.
 * JpaRepository 가 existsById, save 를 제공한다.
 * saveOrder() 는 JpaRepository.save() 를 위임해 서비스 코드에서 이름을 통일한다.
 */
public interface OrderRepository extends JpaRepository<Order, String> {

  Long countByStatus(OrderStatus status);

  List<Order> findAllByStatusOrderByOrderedAtDesc(OrderStatus status);

  List<Order> findAllByStatusAndSellerIdOrderByOrderedAtDesc(OrderStatus status, String sellerId);

  java.util.Optional<Order> findByOrderIdAndSellerId(String orderId, String sellerId);

  default void saveOrder(Order order) {
    save(order);
  }
}
