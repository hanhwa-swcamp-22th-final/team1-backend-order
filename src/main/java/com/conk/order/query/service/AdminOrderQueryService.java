package com.conk.order.query.service;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import com.conk.order.query.dto.request.AdminOrderListQuery;
import com.conk.order.query.dto.response.AdminOrderListResponse;
import com.conk.order.query.dto.response.AdminOrderSummary;
import com.conk.order.query.dto.response.OrderDetailResponse;
import com.conk.order.query.mapper.AdminOrderListQueryMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * 관리자 주문 조회 서비스.
 *
 * 관리자 Actor 가 전체 셀러 주문을 조회하는 기능을 한곳에 묶는다.
 *   - 목록 조회
 *   - 단건 상세 조회
 */
@Service
public class AdminOrderQueryService {

  private final AdminOrderListQueryMapper adminOrderListQueryMapper;
  private final OrderRepository orderRepository;

  public AdminOrderQueryService(
      AdminOrderListQueryMapper adminOrderListQueryMapper,
      OrderRepository orderRepository) {
    this.adminOrderListQueryMapper = adminOrderListQueryMapper;
    this.orderRepository = orderRepository;
  }

  /* 관리자 주문 목록을 조회해 페이징 응답으로 조립한다. */
  public AdminOrderListResponse getAdminOrders(AdminOrderListQuery query) {
    List<AdminOrderSummary> orders = adminOrderListQueryMapper.findOrders(query);
    int totalCount = adminOrderListQueryMapper.countOrders(query);
    return new AdminOrderListResponse(orders, totalCount, query.getPage(), query.getSize());
  }

  /* 주문 상세를 조회한다. */
  @Transactional(readOnly = true)
  public OrderDetailResponse getOrderDetail(String orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    return OrderDetailResponse.from(order);
  }
}
