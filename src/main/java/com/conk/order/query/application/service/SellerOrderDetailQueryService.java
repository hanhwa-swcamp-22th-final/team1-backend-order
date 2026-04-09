package com.conk.order.query.application.service;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import com.conk.order.query.application.dto.SellerOrderDetailResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * 셀러 주문 상세 조회 서비스.
 *
 * 셀러 본인의 주문인지 검증한 뒤 상세 정보를 반환한다.
 * 타 셀러 주문 접근 시 ORDER_NOT_FOUND 로 처리하여 존재 자체를 노출하지 않는다.
 */
@Service
public class SellerOrderDetailQueryService {

  private final OrderRepository orderRepository;

  public SellerOrderDetailQueryService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  /* 셀러 주문 상세를 조회한다. */
  @Transactional(readOnly = true)
  public SellerOrderDetailResponse getDetail(String orderId, String sellerId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

    if (!order.getSellerId().equals(sellerId)) {
      throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
    }

    return SellerOrderDetailResponse.from(order);
  }
}
