package com.conk.order.command.application.service;

import com.conk.order.command.application.dto.CreateOrderItemRequest;
import com.conk.order.command.application.dto.CreateOrderRequest;
import com.conk.order.command.application.dto.CreateOrderResponse;
import com.conk.order.command.application.dto.CreateShippingAddressRequest;
import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.command.domain.aggregate.OrderStatusHistory;
import com.conk.order.command.domain.aggregate.ShippingAddress;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.command.domain.repository.OrderStatusHistoryRepository;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * 셀러 주문 Command 서비스.
 *
 * 셀러 Actor 가 자기 주문에 대해 수행하는 명령을 한곳에 묶는다.
 *   - create : 단건 주문 등록 (ORD-002)
 *   - cancel : 주문 취소 (RECEIVED/ALLOCATED 상태에서만, 본인 검증 포함)
 *
 * 두 메서드 모두 동일한 OrderRepository 를 공유하고, 셀러 검증·주문 수명주기라는
 * 같은 도메인 컨텍스트를 다루기 때문에 한 서비스로 묶는다.
 * cancel 은 취소 이력 기록을 위해 OrderStatusHistoryRepository 도 함께 사용한다.
 */
@Service
public class SellerOrderCommandService {

  private final OrderRepository orderRepository;
  private final OrderIdGenerator orderIdGenerator;
  private final OrderStatusHistoryRepository historyRepository;

  public SellerOrderCommandService(
      OrderRepository orderRepository,
      OrderIdGenerator orderIdGenerator,
      OrderStatusHistoryRepository historyRepository) {
    this.orderRepository = orderRepository;
    this.orderIdGenerator = orderIdGenerator;
    this.historyRepository = historyRepository;
  }

  /*
   * 셀러 단건 주문을 등록하고 채번된 주문 ID 를 반환한다.
   *
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

  /*
   * 셀러 본인의 주문을 취소한다.
   *
   * RECEIVED, ALLOCATED 상태의 주문만 취소 가능하다.
   * 타 셀러의 주문을 숨기기 위해 소유자 불일치 시에도 ORDER_NOT_FOUND 로 응답한다.
   * 취소 성공 시 상태 변경 이력을 기록한다.
   *
   * @throws BusinessException 주문이 없거나, 타 셀러 주문이거나, 취소 불가 상태인 경우
   */
  @Transactional
  public void cancel(String orderId, String sellerId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

    if (!order.getSellerId().equals(sellerId)) {
      throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
    }

    OrderStatus fromStatus = order.getStatus();

    try {
      order.cancelOrder();
    } catch (IllegalStateException e) {
      throw new BusinessException(ErrorCode.ORDER_CANCEL_NOT_ALLOWED);
    }

    historyRepository.save(
        OrderStatusHistory.create(orderId, fromStatus, OrderStatus.CANCELED, sellerId));
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
