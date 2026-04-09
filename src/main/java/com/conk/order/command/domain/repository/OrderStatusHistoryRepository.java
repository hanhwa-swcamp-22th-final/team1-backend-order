package com.conk.order.command.domain.repository;

import com.conk.order.command.domain.aggregate.OrderStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * 주문 상태 변경 히스토리 레포지토리.
 */
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

  /* 주문번호로 히스토리를 시간순으로 조회한다. */
  List<OrderStatusHistory> findByOrderIdOrderByChangedAtAsc(String orderId);
}
