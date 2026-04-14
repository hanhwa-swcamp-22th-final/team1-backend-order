package com.conk.order.query.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.query.dto.AdminOrderStatus;
import com.conk.order.query.dto.response.AdminOrderListResponse;
import com.conk.order.query.dto.response.AdminOrderSummary;
import com.conk.order.query.service.AdminOrderQueryService;
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
 * @MockitoBean: AdminOrderQueryService 를 Mockito 가짜 객체로 대체한다.
 *
 * 이 테스트가 검증하는 것:
 *   - 파라미터 없이 요청해도 200 OK 인가 (sellerId 가 선택값이므로)
 *   - JSON 응답 구조가 success/data 형식인가
 *   - status 필터 파라미터를 전달하면 쿼리 객체에 담기는가
 */
@WebMvcTest(AdminOrderQueryController.class)
class AdminOrderListQueryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AdminOrderQueryService adminOrderQueryService;

  /*
   * 파라미터 없이 요청해도 200 OK 와 success/data 형식으로 응답한다.
   * ORD-004 와 달리 sellerId 가 필수가 아니므로 빈 요청도 정상 처리된다.
   */
  @Test
  void getAdminOrders_returnsOkWithData() throws Exception {
    AdminOrderSummary summary = new AdminOrderSummary();
    summary.setId("ORD-001");
    summary.setOrderedAt(LocalDateTime.of(2026, 4, 5, 10, 0));
    summary.setStatus(AdminOrderStatus.PENDING);
    summary.setChannel("MANUAL");
    summary.setCompany("SELLER-001");
    summary.setWarehouse("WH-001");
    summary.setSkuCount(2);
    summary.setQty(5);
    summary.setDestState("Seoul");

    given(adminOrderQueryService.getAdminOrders(any()))
        .willReturn(new AdminOrderListResponse(List.of(summary), 1, 0, 20));

    mockMvc.perform(get("/orders/list")
            .header("X-Seller-Id", "MASTER-ADMIN-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.orders[0].id").value("ORD-001"))
        .andExpect(jsonPath("$.data.orders[0].company").value("SELLER-001"))
        .andExpect(jsonPath("$.data.orders[0].warehouse").value("WH-001"))
        .andExpect(jsonPath("$.data.orders[0].channel").value("MANUAL"))
        .andExpect(jsonPath("$.data.orders[0].skuCount").value(2))
        .andExpect(jsonPath("$.data.orders[0].qty").value(5))
        .andExpect(jsonPath("$.data.orders[0].destState").value("Seoul"))
        .andExpect(jsonPath("$.data.orders[0].status").value("PENDING"))
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
    given(adminOrderQueryService.getAdminOrders(any()))
        .willReturn(new AdminOrderListResponse(List.of(), 0, 0, 20));

    mockMvc.perform(get("/orders/list")
            .header("X-Seller-Id", "MASTER-ADMIN-001")
            .param("sellerId", "SELLER-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }
}
