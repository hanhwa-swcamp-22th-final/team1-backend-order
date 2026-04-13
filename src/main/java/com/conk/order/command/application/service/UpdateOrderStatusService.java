package com.conk.order.command.application.service;

import com.conk.order.command.application.dto.request.UpdateOrderStatusRequest;
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
 * 주문 상태 변경 서비스.
 *
 * 주문을 조회한 뒤 도메인 규칙(OrderStatus.canTransitionTo) 에 따라
 * 상태를 변경한다. 변경 시 히스토리를 기록한다.
 */
@Service
public class UpdateOrderStatusService {

  private final OrderRepository orderRepository;
  private final OrderStatusHistoryRepository historyRepository;

  public UpdateOrderStatusService(OrderRepository orderRepository,
      OrderStatusHistoryRepository historyRepository) {
    this.orderRepository = orderRepository;
    this.historyRepository = historyRepository;
  }

  /*
   * 주문 상태를 변경한다.
   *
   * @param orderId 변경 대상 주문번호
   * @param request 변경할 상태 정보
   * @throws BusinessException 주문이 없거나 전이가 허용되지 않는 경우
   */
  @Transactional
  public void updateStatus(String orderId, UpdateOrderStatusRequest request) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

    OrderStatus fromStatus = order.getStatus();

    try {
      order.changeStatus(request.getStatus());
    } catch (IllegalStateException e) {
      throw new BusinessException(ErrorCode.ORDER_STATUS_TRANSITION_NOT_ALLOWED);
    }

    historyRepository.save(
        OrderStatusHistory.create(orderId, fromStatus, request.getStatus(), null));
  }
}
