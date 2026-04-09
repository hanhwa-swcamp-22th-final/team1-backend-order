package com.conk.order.query.application.service;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import com.conk.order.query.application.dto.OrderDetailResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * 주문 단건 상세 조회 서비스.
 *
 * JPA findById 로 주문을 조회한 뒤 OrderDetailResponse 로 변환한다.
 * 주문이 없으면 ORDER_NOT_FOUND 예외를 던진다.
 */
@Service
public class OrderDetailQueryService {

  private final OrderRepository orderRepository;

  public OrderDetailQueryService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  /* 주문 상세를 조회한다. */
  @Transactional(readOnly = true)
  public OrderDetailResponse getOrderDetail(String orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    return OrderDetailResponse.from(order);
  }
}
