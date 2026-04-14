package com.conk.order.query.service;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderStatus;
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
import com.conk.order.query.dto.response.SellerOrderOptionsResponse;
import com.conk.order.query.dto.response.SellerOrderOptionsResponse.ChannelOption;
import com.conk.order.query.dto.response.SellerOrderSummary;
import com.conk.order.query.mapper.SellerOrderListQueryMapper;
import java.util.Arrays;
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
  private final SellerProductOptionFetcher sellerProductOptionFetcher;

  public SellerOrderQueryService(
      SellerOrderListQueryMapper sellerOrderListQueryMapper,
      OrderRepository orderRepository,
      OrderStatusHistoryRepository historyRepository,
      SellerProductOptionFetcher sellerProductOptionFetcher) {
    this.sellerOrderListQueryMapper = sellerOrderListQueryMapper;
    this.orderRepository = orderRepository;
    this.historyRepository = historyRepository;
    this.sellerProductOptionFetcher = sellerProductOptionFetcher;
  }

  /* 셀러 주문 목록을 조회해 페이징 응답으로 조립한다. */
  public SellerOrderListResponse getSellerOrders(SellerOrderListQuery query) {
    List<SellerOrderSummary> orders = sellerOrderListQueryMapper.findOrders(query);
    orders.forEach(this::populateSellerOrderListDisplayFields);
    int totalCount = sellerOrderListQueryMapper.countOrders(query);
    return new SellerOrderListResponse(orders, totalCount, query.getPage(), query.getSize());
  }

  /* 셀러 주문 등록 화면 옵션을 반환한다. */
  @Transactional(readOnly = true)
  public SellerOrderOptionsResponse getOrderOptions(String sellerId) {
    return new SellerOrderOptionsResponse(
        sellerProductOptionFetcher.fetchProducts(sellerId),
        Arrays.stream(OrderChannel.values())
            .filter(this::isSelectableSalesChannel)
            .map(channel -> new ChannelOption(channel.name(), toChannelLabel(channel)))
            .toList()
    );
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

  private boolean isSelectableSalesChannel(OrderChannel channel) {
    return channel != OrderChannel.MANUAL && channel != OrderChannel.EXCEL;
  }

  private void populateSellerOrderListDisplayFields(SellerOrderSummary summary) {
    summary.setChannel(toChannelLabel(summary.getOrderChannel()));
    summary.setRecipient(summary.getReceiverName());
    summary.setAddress(buildListAddress(summary));
    summary.setItemsSummary("상품 " + summary.getItemCount() + "건");
    summary.setTrackingNo(summary.getTrackingNo() == null ? "" : summary.getTrackingNo());
    summary.setCanCancel(
        summary.getStatus() == OrderStatus.RECEIVED || summary.getStatus() == OrderStatus.ALLOCATED);
  }

  private String buildListAddress(SellerOrderSummary summary) {
    String street1 = summary.getStreet1() == null ? "" : summary.getStreet1().trim();
    String street2 = summary.getStreet2() == null ? "" : summary.getStreet2().trim();

    if (street1.isEmpty()) {
      return street2;
    }
    if (street2.isEmpty()) {
      return street1;
    }
    return street1 + " " + street2;
  }

  private String toChannelLabel(OrderChannel channel) {
    if (channel == null) {
      return "";
    }
    return switch (channel) {
      case MANUAL -> "Manual";
      case EXCEL -> "Excel";
      case SHOPIFY -> "Shopify";
      default -> channel.name();
    };
  }
}
