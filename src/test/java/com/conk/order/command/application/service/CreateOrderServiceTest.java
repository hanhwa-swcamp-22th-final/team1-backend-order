package com.conk.order.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.command.application.dto.CreateOrderItemRequest;
import com.conk.order.command.application.dto.CreateOrderRequest;
import com.conk.order.command.application.dto.CreateOrderResponse;
import com.conk.order.command.application.dto.CreateShippingAddressRequest;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.common.exception.BusinessException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/* ORD-002 셀러 단건 주문 등록 서비스 단위 테스트. */
@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

  @Mock
  private OrderRepository orderRepository;

  private CreateOrderService service;

  @BeforeEach
  void setUp() {
    service = new CreateOrderService(orderRepository);
  }

  /* orderId 를 전달하지 않으면 서버가 UUID 를 생성해 반환한다. */
  @Test
  void create_generatesOrderId_whenNotProvided() {
    CreateOrderRequest request = buildRequest(null);

    CreateOrderResponse response = service.create(request);

    assertThat(response.getOrderId()).isNotBlank();
    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).saveOrder(captor.capture());
    assertThat(captor.getValue().getOrderId()).isEqualTo(response.getOrderId());
  }

  /* orderId 를 전달하면 해당 값을 그대로 사용한다. */
  @Test
  void create_usesProvidedOrderId_whenGiven() {
    CreateOrderRequest request = buildRequest("ORD-CUSTOM-001");

    CreateOrderResponse response = service.create(request);

    assertThat(response.getOrderId()).isEqualTo("ORD-CUSTOM-001");
  }

  /* 동일한 orderNo 가 이미 존재하면 예외를 던진다. */
  @Test
  void create_throws_whenOrderIdAlreadyExists() {
    when(orderRepository.existsById("ORD-DUP-001")).thenReturn(true);
    CreateOrderRequest duplicate = buildRequest("ORD-DUP-001");

    assertThatThrownBy(() -> service.create(duplicate))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("ORD-DUP-001");
  }

  /* 저장된 주문의 상태는 RECEIVED 이다. */
  @Test
  void create_savesOrderWithReceivedStatus() {
    service.create(buildRequest(null));

    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).saveOrder(captor.capture());
    assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.RECEIVED);
  }

  /* 저장된 주문의 항목 수가 요청과 일치한다. */
  @Test
  void create_savesCorrectItemCount() {
    service.create(buildRequest(null));

    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).saveOrder(captor.capture());
    assertThat(captor.getValue().getItems()).hasSize(2);
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  /*
   * 기본 주문 요청을 생성한다.
   * orderId 가 null 이면 서버 자동 생성, 값이 있으면 해당 값을 사용한다.
   */
  private CreateOrderRequest buildRequest(String orderId) {
    CreateOrderRequest request = new CreateOrderRequest();
    setField(request, "orderId", orderId);
    setField(request, "sellerId", "SELLER-001");
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
