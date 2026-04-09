package com.conk.order.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.command.application.dto.CreateOrderItemRequest;
import com.conk.order.command.application.dto.CreateOrderRequest;
import com.conk.order.command.application.dto.CreateOrderResponse;
import com.conk.order.command.application.dto.CreateShippingAddressRequest;
import com.conk.order.command.domain.repository.OrderRepository;
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

  @Mock
  private OrderIdGenerator orderIdGenerator;

  private CreateOrderService service;

  @BeforeEach
  void setUp() {
    service = new CreateOrderService(orderRepository, orderIdGenerator);
  }

  /* 채번된 주문 ID 로 주문이 등록되고 응답에 포함된다. */
  @Test
  void create_usesGeneratedOrderId() {
    when(orderIdGenerator.generate()).thenReturn("ORD-2026-0408-00001");
    CreateOrderRequest request = buildRequest();

    CreateOrderResponse response = service.create(request);

    assertThat(response.getOrderId()).isEqualTo("ORD-2026-0408-00001");
    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).saveOrder(captor.capture());
    assertThat(captor.getValue().getOrderId()).isEqualTo("ORD-2026-0408-00001");
  }

  /* 저장된 주문의 상태는 RECEIVED 이다. */
  @Test
  void create_savesOrderWithReceivedStatus() {
    when(orderIdGenerator.generate()).thenReturn("ORD-2026-0408-00001");

    service.create(buildRequest());

    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).saveOrder(captor.capture());
    assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.RECEIVED);
  }

  /* 저장된 주문의 항목 수가 요청과 일치한다. */
  @Test
  void create_savesCorrectItemCount() {
    when(orderIdGenerator.generate()).thenReturn("ORD-2026-0408-00001");

    service.create(buildRequest());

    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).saveOrder(captor.capture());
    assertThat(captor.getValue().getItems()).hasSize(2);
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private CreateOrderRequest buildRequest() {
    CreateOrderRequest request = new CreateOrderRequest();
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
