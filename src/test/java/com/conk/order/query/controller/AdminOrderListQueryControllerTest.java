package com.conk.order.query.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.query.dto.AdminOrderListResponse;
import com.conk.order.query.dto.AdminOrderSummary;
import com.conk.order.query.service.AdminOrderListQueryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/*
 * ORD-005 관리자 주문 목록 조회 컨트롤러 단위 테스트.
 *
 * @WebMvcTest: Controller, Filter, Jackson 직렬화 등 MVC 레이어만 로드한다.
 * @MockitoBean: AdminOrderListQueryService 를 Mockito 가짜 객체로 대체한다.
 *
 * 이 테스트가 검증하는 것:
 *   - 파라미터 없이 요청해도 200 OK 인가 (sellerId 가 선택값이므로)
 *   - JSON 응답 구조가 success/data 형식인가
 *   - status 필터 파라미터를 전달하면 쿼리 객체에 담기는가
 */
@WebMvcTest(AdminOrderListQueryController.class)
class AdminOrderListQueryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AdminOrderListQueryService adminOrderListQueryService;

  /*
   * 파라미터 없이 요청해도 200 OK 와 success/data 형식으로 응답한다.
   * ORD-004 와 달리 sellerId 가 필수가 아니므로 빈 요청도 정상 처리된다.
   */
  @Test
  void getAdminOrders_returnsOkWithData() throws Exception {
    AdminOrderSummary summary = new AdminOrderSummary();
    summary.setOrderNo("ORD-001");
    summary.setOrderedAt(LocalDateTime.of(2026, 4, 5, 10, 0));
    summary.setStatus(OrderStatus.RECEIVED);
    summary.setOrderChannel(OrderChannel.MANUAL);
    summary.setSellerId("SELLER-001");
    summary.setReceiverName("홍길동");
    summary.setItemCount(2);

    given(adminOrderListQueryService.getAdminOrders(any()))
        .willReturn(new AdminOrderListResponse(List.of(summary), 1, 0, 20));

    mockMvc.perform(get("/orders/list"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.orders[0].orderNo").value("ORD-001"))
        .andExpect(jsonPath("$.data.orders[0].sellerId").value("SELLER-001"))
        .andExpect(jsonPath("$.data.totalCount").value(1))
        .andExpect(jsonPath("$.data.page").value(0))
        .andExpect(jsonPath("$.data.size").value(20));
  }

  /*
   * sellerId 파라미터를 전달하면 정상적으로 200 OK 를 반환한다.
   * 관리자는 특정 셀러의 주문만 필터링해서 볼 수 있다.
   */
  @Test
  void getAdminOrders_returnsOk_withSellerIdFilter() throws Exception {
    given(adminOrderListQueryService.getAdminOrders(any()))
        .willReturn(new AdminOrderListResponse(List.of(), 0, 0, 20));

    mockMvc.perform(get("/orders/list")
            .param("sellerId", "SELLER-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }
}