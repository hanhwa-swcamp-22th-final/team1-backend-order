package com.conk.order.query.service;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderStatusHistory;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.command.domain.repository.OrderStatusHistoryRepository;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import com.conk.order.query.dto.request.SellerOrderListQuery;
import com.conk.order.query.dto.response.OrderTrackingResponse;
import com.conk.order.query.dto.response.OrderTrackingResponse.StatusChange;
import com.conk.order.query.dto.response.SellerOrderDetailResponse;
import com.conk.order.query.dto.response.SellerOrderListResponse;
import com.conk.order.query.dto.response.SellerOrderSummary;
import com.conk.order.query.mapper.SellerOrderListQueryMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * 셀러 주문 조회 서비스.
 *
 * 셀러 Actor 가 자신의 주문에 대해 수행하는 조회를 한곳에 묶는다.
 *   - 목록 조회
 *   - 상세 조회
 *   - 상태 이력 조회
 */
@Service
public class SellerOrderQueryService {

  private final SellerOrderListQueryMapper sellerOrderListQueryMapper;
  private final OrderRepository orderRepository;
  private final OrderStatusHistoryRepository historyRepository;

  public SellerOrderQueryService(
      SellerOrderListQueryMapper sellerOrderListQueryMapper,
      OrderRepository orderRepository,
      OrderStatusHistoryRepository historyRepository) {
    this.sellerOrderListQueryMapper = sellerOrderListQueryMapper;
    this.orderRepository = orderRepository;
    this.historyRepository = historyRepository;
  }

  /* 셀러 주문 목록을 조회해 페이징 응답으로 조립한다. */
  public SellerOrderListResponse getSellerOrders(SellerOrderListQuery query) {
    List<SellerOrderSummary> orders = sellerOrderListQueryMapper.findOrders(query);
    int totalCount = sellerOrderListQueryMapper.countOrders(query);
    return new SellerOrderListResponse(orders, totalCount, query.getPage(), query.getSize());
  }

  /* 셀러 본인의 주문 상세를 조회한다. */
  @Transactional(readOnly = true)
  public SellerOrderDetailResponse getDetail(String orderId, String sellerId) {
    Order order = getSellerOwnedOrder(orderId, sellerId);
    return SellerOrderDetailResponse.from(order);
  }

  /* 셀러 본인의 주문 상태 변경 이력을 조회한다. */
  @Transactional(readOnly = true)
  public OrderTrackingResponse getTracking(String orderId, String sellerId) {
    Order order = getSellerOwnedOrder(orderId, sellerId);
    List<OrderStatusHistory> histories =
        historyRepository.findByOrderIdOrderByChangedAtAsc(orderId);

    List<StatusChange> changes = histories.stream()
        .map(StatusChange::from)
        .toList();

    return new OrderTrackingResponse(orderId, order.getStatus(), changes);
  }

  /* 셀러 소유 주문을 조회한다. 소유자가 다르면 존재하지 않는 주문처럼 처리한다. */
  private Order getSellerOwnedOrder(String orderId, String sellerId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

    if (!order.getSellerId().equals(sellerId)) {
      throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
    }
    return order;
  }
}
