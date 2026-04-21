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

  public List<InternalOrderSummaryResponse> getPendingOrders(String tenantId) {
    return findPendingOrders(tenantId).stream()
        .map(InternalOrderSummaryResponse::from)
        .toList();
  }

  public InternalOrderSummaryResponse getOrder(String tenantId, String orderId) {
    return InternalOrderSummaryResponse.from(getOrderEntity(tenantId, orderId));
  }

  public InternalOrderShipmentResponse getShipment(String tenantId, String orderId) {
    return InternalOrderShipmentResponse.from(getOrderEntity(tenantId, orderId));
  }

  private List<Order> findPendingOrders(String tenantId) {
    if (tenantId == null || tenantId.isBlank()) {
      return orderRepository.findAllByStatusOrderByOrderedAtDesc(OrderStatus.RECEIVED);
    }
    return orderRepository.findAllByStatusAndTenantIdOrderByOrderedAtDesc(OrderStatus.RECEIVED, tenantId);
  }

  private Order getOrderEntity(String tenantId, String orderId) {
    if (tenantId == null || tenantId.isBlank()) {
      return orderRepository.findById(orderId)
          .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }
    return orderRepository.findByOrderIdAndTenantId(orderId, tenantId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
  }
}
