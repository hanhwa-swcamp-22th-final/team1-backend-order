package com.conk.order.query.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.query.application.dto.SellerOrderListResponse;
import com.conk.order.query.application.dto.SellerOrderSummary;
import com.conk.order.query.application.service.SellerOrderListQueryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/*
 * ORD-004 셀러 주문 목록 조회 컨트롤러 단위 테스트.
 *
 * @WebMvcTest: Controller, Filter, Jackson 직렬화 등 MVC 레이어만 로드한다.
 *              DB, JPA, MyBatis 는 로드하지 않아 빠르게 실행된다.
 * @MockitoBean: SellerOrderListQueryService 를 Mockito 가짜 객체로 대체한다.
 *               given(...).willReturn(...) 으로 서비스 결과를 고정한다.
 *
 * 이 테스트가 검증하는 것:
 *   - HTTP 응답 상태코드가 맞는가 (200, 400)
 *   - JSON 응답 구조가 success/data 형식인가
 *   - 필수 파라미터(sellerId) 없을 때 400 을 반환하는가
 */
@WebMvcTest(SellerOrderListQueryController.class)
class SellerOrderListQueryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  /* 실제 서비스 대신 Mockito 가짜 객체를 Spring 빈으로 등록한다. */
  @MockitoBean
  private SellerOrderListQueryService sellerOrderListQueryService;

  /* 정상 요청 시 200 OK 와 success/data 형식으로 응답한다. */
  @Test
  void getSellerOrders_returnsOkWithData() throws Exception {
    /* 테스트용 주문 요약 1건을 만들어 서비스가 반환할 결과로 고정한다. */
    SellerOrderSummary summary = new SellerOrderSummary();
    summary.setOrderNo("ORD-001");
    summary.setOrderedAt(LocalDateTime.of(2026, 4, 3, 10, 0));
    summary.setStatus(OrderStatus.RECEIVED);
    summary.setOrderChannel(OrderChannel.MANUAL);
    summary.setReceiverName("홍길동");
    summary.setItemCount(2);

    /*
     * any() : 컨트롤러가 어떤 query 객체를 넘기든 상관없이
     *         항상 아래 SellerOrderListResponse 를 반환하도록 설정한다.
     */
    given(sellerOrderListQueryService.getSellerOrders(any()))
        .willReturn(new SellerOrderListResponse(List.of(summary), 1, 0, 20));

    mockMvc.perform(get("/orders/seller/list")
            .param("sellerId", "SELLER-001"))  /* sellerId 는 필수 파라미터 */
        .andExpect(status().isOk())
        /* $.success → JSON 루트의 success 필드 */
        .andExpect(jsonPath("$.success").value(true))
        /* $.data.orders[0].orderNo → data 안의 첫 번째 주문의 orderNo */
        .andExpect(jsonPath("$.data.orders[0].orderNo").value("ORD-001"))
        .andExpect(jsonPath("$.data.orders[0].status").value("RECEIVED"))
        .andExpect(jsonPath("$.data.totalCount").value(1))
        .andExpect(jsonPath("$.data.page").value(0))
        .andExpect(jsonPath("$.data.size").value(20));
  }

  /* sellerId 없이 요청하면 400 Bad Request 를 반환한다. */
  @Test
  void getSellerOrders_returnsBadRequest_whenSellerIdMissing() throws Exception {
    /*
     * sellerId 파라미터를 보내지 않으면 Spring 이 자동으로 400 반환한다.
     * @RequestParam String sellerId 는 required=true(기본값) 이므로
     * 누락 시 MissingServletRequestParameterException 이 발생한다.
     * 서비스 mock 은 한 번도 호출되지 않는다.
     */
    mockMvc.perform(get("/orders/seller/list"))
        .andExpect(status().isBadRequest());
  }
}
