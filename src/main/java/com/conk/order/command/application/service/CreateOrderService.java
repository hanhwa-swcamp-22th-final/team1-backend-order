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
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* ORD-002 셀러 단건 주문 등록 서비스. */
@Service
public class CreateOrderService {

  private final OrderRepository orderRepository;
  private final OrderIdGenerator orderIdGenerator;

  public CreateOrderService(OrderRepository orderRepository, OrderIdGenerator orderIdGenerator) {
    this.orderRepository = orderRepository;
    this.orderIdGenerator = orderIdGenerator;
  }

  /**
   * 주문을 등록하고 채번된 주문 ID 를 반환한다.
   * 주문 ID 는 OrderIdGenerator 가 날짜 기반 시퀀스로 생성한다.
   * sellerId 는 NGINX 가 주입한 X-User-Id 헤더 값을 컨트롤러가 전달한다.
   */
  @Transactional
  public CreateOrderResponse create(CreateOrderRequest request, String sellerId) {
    String orderId = orderIdGenerator.generate();

    Order order = Order.create(
        orderId,
        request.getOrderedAt(),
        sellerId,
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
