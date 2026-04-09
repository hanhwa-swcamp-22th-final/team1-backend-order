package com.conk.order.query.application.service;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderStatusHistory;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.command.domain.repository.OrderStatusHistoryRepository;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import com.conk.order.query.application.dto.OrderTrackingResponse;
import com.conk.order.query.application.dto.OrderTrackingResponse.StatusChange;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * 주문 상태 트래킹 조회 서비스.
 *
 * 셀러 본인 주문의 상태 변경 이력을 시간순으로 반환한다.
 */
@Service
public class OrderTrackingQueryService {

  private final OrderRepository orderRepository;
  private final OrderStatusHistoryRepository historyRepository;

  public OrderTrackingQueryService(OrderRepository orderRepository,
      OrderStatusHistoryRepository historyRepository) {
    this.orderRepository = orderRepository;
    this.historyRepository = historyRepository;
  }

  /* 주문 상태 변경 이력을 조회한다. */
  @Transactional(readOnly = true)
  public OrderTrackingResponse getTracking(String orderId, String sellerId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

    if (!order.getSellerId().equals(sellerId)) {
      throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
    }

    List<OrderStatusHistory> histories =
        historyRepository.findByOrderIdOrderByChangedAtAsc(orderId);

    List<StatusChange> changes = histories.stream()
        .map(StatusChange::from)
        .toList();

    return new OrderTrackingResponse(orderId, order.getStatus(), changes);
  }
}
