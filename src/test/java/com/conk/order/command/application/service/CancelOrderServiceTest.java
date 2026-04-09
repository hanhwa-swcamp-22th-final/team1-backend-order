package com.conk.order.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.command.domain.aggregate.ShippingAddress;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/*
 * 셀러 주문 취소 서비스 단위 테스트.
 *
 * 검증 대상:
 *   - RECEIVED 상태 주문 취소 성공
 *   - 주문 미존재 → ORDER_NOT_FOUND
 *   - 타 셀러 주문 접근 → ORDER_NOT_FOUND
 *   - 취소 불가 상태 → ORDER_CANCEL_NOT_ALLOWED
 */
@ExtendWith(MockitoExtension.class)
class CancelOrderServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @InjectMocks
  private CancelOrderService cancelOrderService;

  /* RECEIVED 상태 주문을 취소하면 CANCELED 상태가 된다. */
  @Test
  void cancel_changesStatusToCanceled() {
    Order order = createOrder("ORD-001", "SELLER-001");
    given(orderRepository.findById("ORD-001")).willReturn(Optional.of(order));

    cancelOrderService.cancel("ORD-001", "SELLER-001");

    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
  }

  /* 존재하지 않는 주문 → ORDER_NOT_FOUND. */
  @Test
  void cancel_throwsNotFound_whenOrderDoesNotExist() {
    given(orderRepository.findById("NONE")).willReturn(Optional.empty());

    assertThatThrownBy(() -> cancelOrderService.cancel("NONE", "SELLER-001"))
        .isInstanceOf(BusinessException.class)
        .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.ORDER_NOT_FOUND));
  }

  /* 타 셀러 주문 접근 → ORDER_NOT_FOUND (주문 존재 자체를 노출하지 않음). */
  @Test
  void cancel_throwsNotFound_whenDifferentSeller() {
    Order order = createOrder("ORD-001", "SELLER-001");
    given(orderRepository.findById("ORD-001")).willReturn(Optional.of(order));

    assertThatThrownBy(() -> cancelOrderService.cancel("ORD-001", "SELLER-OTHER"))
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

    assertThatThrownBy(() -> cancelOrderService.cancel("ORD-002", "SELLER-001"))
        .isInstanceOf(BusinessException.class)
        .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.ORDER_CANCEL_NOT_ALLOWED));
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private Order createOrder(String orderId, String sellerId) {
    return Order.create(
        orderId, LocalDateTime.of(2026, 4, 9, 10, 0), sellerId,
        OrderChannel.MANUAL,
        List.of(OrderItem.create("SKU-001", 1, null)),
        ShippingAddress.create("123 Main St", null, "LA", "CA", "90001"),
        "홍길동", "010-1234-5678", null
    );
  }
}
