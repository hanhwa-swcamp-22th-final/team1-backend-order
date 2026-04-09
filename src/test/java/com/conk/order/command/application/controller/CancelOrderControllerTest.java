package com.conk.order.command.application.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.application.service.CancelOrderService;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/*
 * 셀러 주문 취소 컨트롤러 단위 테스트.
 *
 * 검증 대상:
 *   - 정상 취소 → 200 OK + message
 *   - 주문 미존재 → 404
 *   - 취소 불가 → 409
 *   - X-User-Id 누락 → 401
 */
@WebMvcTest(CancelOrderController.class)
class CancelOrderControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CancelOrderService cancelOrderService;

  /* 정상 취소 시 200 OK 와 메시지를 반환한다. */
  @Test
  void cancel_returnsOk() throws Exception {
    doNothing().when(cancelOrderService).cancel(eq("ORD-001"), eq("SELLER-001"));

    mockMvc.perform(patch("/orders/seller/ORD-001/cancel")
            .header("X-User-Id", "SELLER-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("주문이 취소되었습니다."));
  }

  /* 주문이 없으면 404 를 반환한다. */
  @Test
  void cancel_returns404_whenNotFound() throws Exception {
    doThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND))
        .when(cancelOrderService).cancel(eq("NONE"), eq("SELLER-001"));

    mockMvc.perform(patch("/orders/seller/NONE/cancel")
            .header("X-User-Id", "SELLER-001"))
        .andExpect(status().isNotFound());
  }

  /* 취소 불가 상태면 409 를 반환한다. */
  @Test
  void cancel_returns409_whenNotCancelable() throws Exception {
    doThrow(new BusinessException(ErrorCode.ORDER_CANCEL_NOT_ALLOWED))
        .when(cancelOrderService).cancel(eq("ORD-001"), eq("SELLER-001"));

    mockMvc.perform(patch("/orders/seller/ORD-001/cancel")
            .header("X-User-Id", "SELLER-001"))
        .andExpect(status().isConflict());
  }

  /* X-User-Id 헤더 누락 시 401 을 반환한다. */
  @Test
  void cancel_returns401_whenHeaderMissing() throws Exception {
    mockMvc.perform(patch("/orders/seller/ORD-001/cancel"))
        .andExpect(status().isUnauthorized());
  }
}
