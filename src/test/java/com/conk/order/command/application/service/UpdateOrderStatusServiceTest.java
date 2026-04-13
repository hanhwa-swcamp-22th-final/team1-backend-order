package com.conk.order.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.conk.order.command.application.dto.request.UpdateOrderStatusRequest;
import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.command.domain.aggregate.ShippingAddress;
import com.conk.order.command.domain.aggregate.OrderStatusHistory;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.command.domain.repository.OrderStatusHistoryRepository;
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
 * 주문 상태 변경 서비스 단위 테스트.
 *
 * 검증 대상:
 *   - 정상 전이 시 상태 변경
 *   - 주문 미존재 시 ORDER_NOT_FOUND
 *   - 허용되지 않는 전이 시 ORDER_STATUS_TRANSITION_NOT_ALLOWED
 */
@ExtendWith(MockitoExtension.class)
class UpdateOrderStatusServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private OrderStatusHistoryRepository historyRepository;

  @InjectMocks
  private UpdateOrderStatusService updateOrderStatusService;

  /* RECEIVED → ALLOCATED 정상 전이 검증. */
  @Test
  void updateStatus_changesStatus_whenTransitionIsValid() {
    Order order = createOrder("ORD-001");
    given(orderRepository.findById("ORD-001")).willReturn(Optional.of(order));

    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
    request.setStatus(OrderStatus.ALLOCATED);

    updateOrderStatusService.updateStatus("ORD-001", request);

    assertThat(order.getStatus()).isEqualTo(OrderStatus.ALLOCATED);
    verify(historyRepository).save(any(OrderStatusHistory.class));
  }

  /* 존재하지 않는 주문 → ORDER_NOT_FOUND 예외. */
  @Test
  void updateStatus_throwsNotFound_whenOrderDoesNotExist() {
    given(orderRepository.findById("NONE")).willReturn(Optional.empty());

    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
    request.setStatus(OrderStatus.ALLOCATED);

    assertThatThrownBy(() -> updateOrderStatusService.updateStatus("NONE", request))
        .isInstanceOf(BusinessException.class)
        .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.ORDER_NOT_FOUND));
  }

  /* RECEIVED → PICKING (스킵) → ORDER_STATUS_TRANSITION_NOT_ALLOWED 예외. */
  @Test
  void updateStatus_throwsConflict_whenTransitionIsInvalid() {
    Order order = createOrder("ORD-002");
    given(orderRepository.findById("ORD-002")).willReturn(Optional.of(order));

    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
    request.setStatus(OrderStatus.PICKING);

    assertThatThrownBy(() -> updateOrderStatusService.updateStatus("ORD-002", request))
        .isInstanceOf(BusinessException.class)
        .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
            .isEqualTo(ErrorCode.ORDER_STATUS_TRANSITION_NOT_ALLOWED));
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private Order createOrder(String orderId) {
    return Order.create(
        orderId,
        LocalDateTime.of(2026, 4, 9, 10, 0),
        "SELLER-001",
        OrderChannel.MANUAL,
        List.of(OrderItem.create("SKU-001", 1, null)),
        ShippingAddress.create("123 Main St", null, "LA", "CA", "90001"),
        "홍길동",
        "010-1234-5678",
        null
    );
  }
}
