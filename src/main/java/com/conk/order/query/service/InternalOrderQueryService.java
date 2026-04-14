package com.conk.order.query.service;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import com.conk.order.query.dto.response.InternalOrderShipmentResponse;
import com.conk.order.query.dto.response.InternalOrderSummaryResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * WMS 내부 연동용 주문 조회 서비스다.
 */
@Service
@Transactional(readOnly = true)
public class InternalOrderQueryService {

  private final OrderRepository orderRepository;

  public InternalOrderQueryService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  public List<InternalOrderSummaryResponse> getPendingOrders() {
    return orderRepository.findAllByStatusOrderByOrderedAtDesc(OrderStatus.RECEIVED).stream()
        .map(InternalOrderSummaryResponse::from)
        .toList();
  }

  public InternalOrderSummaryResponse getOrder(String orderId) {
    return InternalOrderSummaryResponse.from(getOrderEntity(orderId));
  }

  public InternalOrderShipmentResponse getShipment(String orderId) {
    return InternalOrderShipmentResponse.from(getOrderEntity(orderId));
  }

  private Order getOrderEntity(String orderId) {
    return orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
  }
}
