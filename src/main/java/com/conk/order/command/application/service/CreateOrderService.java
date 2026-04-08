package com.conk.order.command.application.service;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.ShippingAddress;
import com.conk.order.command.application.dto.CreateOrderItemRequest;
import com.conk.order.command.application.dto.CreateOrderRequest;
import com.conk.order.command.application.dto.CreateOrderResponse;
import com.conk.order.command.application.dto.CreateShippingAddressRequest;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* ORD-002 셀러 단건 주문 등록 서비스. */
@Service
public class CreateOrderService {

  private final OrderRepository orderRepository;

  public CreateOrderService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  /**
   * 주문을 등록하고 생성된 주문번호를 반환한다.
   *
   * <p>orderNo 가 null 이면 UUID 로 자동 생성한다.
   * orderNo 가 전달되면 해당 값을 사용하되, 이미 존재하면 예외를 던진다.
   */
  @Transactional
  public CreateOrderResponse create(CreateOrderRequest request) {
    String orderId = resolveOrderId(request.getOrderId());

    Order order = Order.create(
        orderId,
        request.getOrderedAt(),
        request.getSellerId(),
        OrderChannel.MANUAL,
        toOrderItems(request.getItems()),
        toShippingAddress(request.getShippingAddress()),
        request.getReceiverName(),
        request.getReceiverPhoneNo(),
        request.getMemo()
    );

    orderRepository.saveOrder(order);
    return new CreateOrderResponse(orderId);
  }

  /*
   * 요청에 orderNo 가 있으면 중복 여부를 확인하고 반환한다.
   * 없으면 UUID 를 생성해 반환한다.
   */
  private String resolveOrderId(String requested) {
    if (requested == null || requested.isBlank()) {
      return UUID.randomUUID().toString();
    }
    if (orderRepository.existsById(requested)) {
      throw new BusinessException(ErrorCode.ORDER_ALREADY_EXISTS,
          "이미 존재하는 주문번호입니다: " + requested);
    }
    return requested;
  }

  /* 요청 항목 목록을 도메인 OrderItem 으로 변환한다. */
  private List<OrderItem> toOrderItems(List<CreateOrderItemRequest> items) {
    return items.stream()
        .map(i -> OrderItem.create(i.getSku(), i.getQuantity(), i.getProductNameSnapshot()))
        .toList();
  }

  /* 요청 배송지를 도메인 ShippingAddress 로 변환한다. */
  private ShippingAddress toShippingAddress(CreateShippingAddressRequest req) {
    return ShippingAddress.create(
        req.getAddress1(),
        req.getAddress2(),
        req.getCity(),
        req.getState(),
        req.getZipCode()
    );
  }
}
