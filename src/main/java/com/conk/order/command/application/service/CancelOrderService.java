package com.conk.order.command.application.service;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.command.domain.aggregate.OrderStatusHistory;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.command.domain.repository.OrderStatusHistoryRepository;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * 셀러 주문 취소 서비스.
 *
 * RECEIVED, ALLOCATED 상태의 주문만 취소 가능하다.
 * 셀러 본인의 주문인지 검증한 뒤 Order.cancelOrder() 를 호출한다.
 * 취소 시 히스토리를 기록한다.
 */
@Service
public class CancelOrderService {

  private final OrderRepository orderRepository;
  private final OrderStatusHistoryRepository historyRepository;

  public CancelOrderService(OrderRepository orderRepository,
      OrderStatusHistoryRepository historyRepository) {
    this.orderRepository = orderRepository;
    this.historyRepository = historyRepository;
  }

  /*
   * 셀러의 주문을 취소한다.
   *
   * @param orderId  취소할 주문번호
   * @param sellerId 요청 셀러 식별자 (X-User-Id 헤더)
   * @throws BusinessException 주문이 없거나, 타 셀러 주문이거나, 취소 불가 상태인 경우
   */
  @Transactional
  public void cancel(String orderId, String sellerId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

    if (!order.getSellerId().equals(sellerId)) {
      throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
    }

    OrderStatus fromStatus = order.getStatus();

    try {
      order.cancelOrder();
    } catch (IllegalStateException e) {
      throw new BusinessException(ErrorCode.ORDER_CANCEL_NOT_ALLOWED);
    }

    historyRepository.save(
        OrderStatusHistory.create(orderId, fromStatus, OrderStatus.CANCELED, sellerId));
  }
}
