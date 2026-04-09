package com.conk.order.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/*
 * 셀러 주문 Command 서비스 단위 테스트.
 *
 * create (ORD-002) / cancel 두 명령의 단위 검증을 한 테스트 클래스에 묶는다.
 * 각 명령의 의도를 Nested 클래스로 구분해 가독성을 유지한다.
 */
@ExtendWith(MockitoExtension.class)
class SellerOrderCommandServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private OrderIdGenerator orderIdGenerator;

  @Mock
  private OrderStatusHistoryRepository historyRepository;

  private SellerOrderCommandService service;

  @BeforeEach
  void setUp() {
    service = new SellerOrderCommandService(orderRepository, orderIdGenerator, historyRepository);
  }

  @Nested
  class Create {

    /* 채번된 주문 ID 로 주문이 등록되고 응답에 포함된다. */
    @Test
    void create_usesGeneratedOrderId() {
      when(orderIdGenerator.generate()).thenReturn("ORD-2026-0408-00001");
      CreateOrderRequest request = buildRequest();

      CreateOrderResponse response = service.create(request, "SELLER-001");

      assertThat(response.getOrderId()).isEqualTo("ORD-2026-0408-00001");
      ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
      verify(orderRepository).saveOrder(captor.capture());
      assertThat(captor.getValue().getOrderId()).isEqualTo("ORD-2026-0408-00001");
    }

    /* 저장된 주문의 상태는 RECEIVED 이다. */
    @Test
    void create_savesOrderWithReceivedStatus() {
      when(orderIdGenerator.generate()).thenReturn("ORD-2026-0408-00001");

      service.create(buildRequest(), "SELLER-001");

      ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
      verify(orderRepository).saveOrder(captor.capture());
      assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.RECEIVED);
    }

    /* 저장된 주문의 항목 수가 요청과 일치한다. */
    @Test
    void create_savesCorrectItemCount() {
      when(orderIdGenerator.generate()).thenReturn("ORD-2026-0408-00001");

      service.create(buildRequest(), "SELLER-001");

      ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
      verify(orderRepository).saveOrder(captor.capture());
      assertThat(captor.getValue().getItems()).hasSize(2);
    }
  }

  @Nested
  class Cancel {

    /* RECEIVED 상태 주문을 취소하면 CANCELED 상태가 되고 히스토리가 기록된다. */
    @Test
    void cancel_changesStatusToCanceled() {
      Order order = createOrder("ORD-001", "SELLER-001");
      given(orderRepository.findById("ORD-001")).willReturn(Optional.of(order));

      service.cancel("ORD-001", "SELLER-001");

      assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
      verify(historyRepository).save(any(OrderStatusHistory.class));
    }

    /* 존재하지 않는 주문 → ORDER_NOT_FOUND. */
    @Test
    void cancel_throwsNotFound_whenOrderDoesNotExist() {
      given(orderRepository.findById("NONE")).willReturn(Optional.empty());

      assertThatThrownBy(() -> service.cancel("NONE", "SELLER-001"))
          .isInstanceOf(BusinessException.class)
          .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
              .isEqualTo(ErrorCode.ORDER_NOT_FOUND));
    }

    /* 타 셀러 주문 접근 → ORDER_NOT_FOUND (주문 존재 자체를 노출하지 않음). */
    @Test
    void cancel_throwsNotFound_whenDifferentSeller() {
      Order order = createOrder("ORD-001", "SELLER-001");
      given(orderRepository.findById("ORD-001")).willReturn(Optional.of(order));

      assertThatThrownBy(() -> service.cancel("ORD-001", "SELLER-OTHER"))
          .isInstanceOf(BusinessException.class)
          .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
              .isEqualTo(ErrorCode.ORDER_NOT_FOUND));
    }

    /* 취소 불가 상태(PICKING) → ORDER_CANCEL_NOT_ALLOWED. */
    @Test
    void cancel_throwsConflict_whenStatusNotCancelable() {
      Order order = createOrder("ORD-002", "SELLER-001");
      order.changeStatus(OrderStatus.ALLOCATED);
      order.changeStatus(OrderStatus.OUTBOUND_INSTRUCTED);
      order.changeStatus(OrderStatus.PICKING);
      given(orderRepository.findById("ORD-002")).willReturn(Optional.of(order));

      assertThatThrownBy(() -> service.cancel("ORD-002", "SELLER-001"))
          .isInstanceOf(BusinessException.class)
          .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
              .isEqualTo(ErrorCode.ORDER_CANCEL_NOT_ALLOWED));
    }
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private CreateOrderRequest buildRequest() {
    CreateOrderRequest request = new CreateOrderRequest();
    setField(request, "orderedAt", LocalDateTime.of(2026, 4, 3, 10, 0));
    setField(request, "items", List.of(
        buildItem("SKU-001", 2, "상품 A"),
        buildItem("SKU-002", 1, null)
    ));
    setField(request, "shippingAddress", buildAddress());
    setField(request, "receiverName", "홍길동");
    setField(request, "receiverPhoneNo", "010-1234-5678");
    setField(request, "memo", null);
    return request;
  }

  private CreateOrderItemRequest buildItem(String sku, int quantity, String name) {
    CreateOrderItemRequest item = new CreateOrderItemRequest();
    setField(item, "sku", sku);
    setField(item, "quantity", quantity);
    setField(item, "productNameSnapshot", name);
    return item;
  }

  private CreateShippingAddressRequest buildAddress() {
    CreateShippingAddressRequest addr = new CreateShippingAddressRequest();
    setField(addr, "address1", "서울시 강남구 테헤란로 123");
    setField(addr, "address2", null);
    setField(addr, "city", "Seoul");
    setField(addr, "state", null);
    setField(addr, "zipCode", "06236");
    return addr;
  }

  private Order createOrder(String orderId, String sellerId) {
    return Order.create(
        orderId, LocalDateTime.of(2026, 4, 9, 10, 0), sellerId,
        OrderChannel.MANUAL,
        List.of(OrderItem.create("SKU-001", 1, null)),
        ShippingAddress.create("123 Main St", null, "LA", "CA", "90001"),
        "홍길동", "010-1234-5678", null
    );
  }

  /* 리플렉션으로 DTO 필드를 설정한다 (Lombok @Getter 전용 DTO 는 setter 가 없음). */
  private static void setField(Object target, String fieldName, Object value) {
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
