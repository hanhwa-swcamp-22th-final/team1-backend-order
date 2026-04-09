package com.conk.order.command.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.application.service.UpdateOrderStatusService;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/*
 * 주문 상태 변경 컨트롤러 단위 테스트.
 *
 * 검증 대상:
 *   - 정상 요청 시 200 OK + success/message 응답
 *   - 주문 미존재 시 404
 *   - 잘못된 전이 시 409
 *   - status 누락 시 400
 */
@WebMvcTest(OrderStatusCommandController.class)
class UpdateOrderStatusControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UpdateOrderStatusService updateOrderStatusService;

  /* 정상 요청 시 200 OK 와 메시지를 반환한다. */
  @Test
  void updateStatus_returnsOk() throws Exception {
    doNothing().when(updateOrderStatusService).updateStatus(eq("ORD-001"), any());

    mockMvc.perform(patch("/orders/ORD-001/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\": \"ALLOCATED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("주문 상태가 변경되었습니다."));
  }

  /* 주문이 존재하지 않으면 404 를 반환한다. */
  @Test
  void updateStatus_returns404_whenOrderNotFound() throws Exception {
    doThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND))
        .when(updateOrderStatusService).updateStatus(eq("NONE"), any());

    mockMvc.perform(patch("/orders/NONE/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\": \"ALLOCATED\"}"))
        .andExpect(status().isNotFound());
  }

  /* 허용되지 않는 전이 시 409 를 반환한다. */
  @Test
  void updateStatus_returns409_whenTransitionNotAllowed() throws Exception {
    doThrow(new BusinessException(ErrorCode.ORDER_STATUS_TRANSITION_NOT_ALLOWED))
        .when(updateOrderStatusService).updateStatus(eq("ORD-001"), any());

    mockMvc.perform(patch("/orders/ORD-001/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\": \"PICKING\"}"))
        .andExpect(status().isConflict());
  }

  /* status 필드가 없으면 400 을 반환한다. */
  @Test
  void updateStatus_returns400_whenStatusMissing() throws Exception {
    mockMvc.perform(patch("/orders/ORD-001/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());
  }
}
