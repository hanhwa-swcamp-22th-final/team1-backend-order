package com.conk.order.query.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.query.dto.SellerOrderStatus;
import com.conk.order.query.dto.request.SellerOrderListQuery;
import com.conk.order.query.dto.response.SellerOrderListResponse;
import com.conk.order.query.dto.response.SellerOrderOptionsResponse;
import com.conk.order.query.dto.response.SellerOrderSummary;
import com.conk.order.query.service.SellerOrderQueryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/*
 * ORD-004 셀러 주문 목록 조회 컨트롤러 단위 테스트.
 *
 * sellerId 는 X-User-Id 헤더에서 추출한다.
 */
@WebMvcTest(SellerOrderQueryController.class)
class SellerOrderListQueryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SellerOrderQueryService sellerOrderQueryService;

  /* 정상 요청 시 200 OK 와 success/data 형식으로 응답한다. */
  @Test
  void getSellerOrders_returnsOkWithData() throws Exception {
    SellerOrderSummary summary = new SellerOrderSummary();
    summary.setOrderId("ORD-001");
    summary.setOrderedAt(LocalDateTime.of(2026, 4, 3, 10, 0));
    summary.setStatus(SellerOrderStatus.RECEIVED);
    summary.setOrderChannel(OrderChannel.MANUAL);
    summary.setChannel("MANUAL");
    summary.setReceiverName("홍길동");
    summary.setRecipient("홍길동");
    summary.setItemCount(2);
    summary.setItemsSummary("상품 2건");
    summary.setStreet1("서울시 강남구 테헤란로 123");
    summary.setStreet2("101동 202호");
    summary.setAddress("서울시 강남구 테헤란로 123 101동 202호");
    summary.setTrackingNo("");
    summary.setCanCancel(true);

    given(sellerOrderQueryService.getSellerOrders(any()))
        .willReturn(new SellerOrderListResponse(List.of(summary), 1, 0, 20));

    mockMvc.perform(get("/orders/seller/list")
            .header("X-User-Id", "SELLER-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.orders[0].orderId").value("ORD-001"))
        .andExpect(jsonPath("$.data.orders[0].orderChannel").value("MANUAL"))
        .andExpect(jsonPath("$.data.orders[0].channel").value("MANUAL"))
        .andExpect(jsonPath("$.data.orders[0].receiverName").value("홍길동"))
        .andExpect(jsonPath("$.data.orders[0].recipient").value("홍길동"))
        .andExpect(jsonPath("$.data.orders[0].street1").value("서울시 강남구 테헤란로 123"))
        .andExpect(jsonPath("$.data.orders[0].street2").value("101동 202호"))
        .andExpect(jsonPath("$.data.orders[0].address").value("서울시 강남구 테헤란로 123 101동 202호"))
        .andExpect(jsonPath("$.data.orders[0].itemCount").value(2))
        .andExpect(jsonPath("$.data.orders[0].itemsSummary").value("상품 2건"))
        .andExpect(jsonPath("$.data.orders[0].status").value("RECEIVED"))
        .andExpect(jsonPath("$.data.orders[0].trackingNo").value(""))
        .andExpect(jsonPath("$.data.orders[0].canCancel").value(true))
        .andExpect(jsonPath("$.data.totalCount").value(1))
        .andExpect(jsonPath("$.data.page").value(0))
        .andExpect(jsonPath("$.data.size").value(20));
  }

  @Test
  void getSellerOrders_acceptsGroupedStatusFilter() throws Exception {
    given(sellerOrderQueryService.getSellerOrders(any()))
        .willReturn(new SellerOrderListResponse(List.of(), 0, 0, 20));

    mockMvc.perform(get("/orders/seller/list")
            .header("X-User-Id", "SELLER-001")
            .param("status", "DISPATCHED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    ArgumentCaptor<SellerOrderListQuery> captor = ArgumentCaptor.forClass(SellerOrderListQuery.class);
    then(sellerOrderQueryService).should().getSellerOrders(captor.capture());
    assertThat(captor.getValue().getSellerStatus()).isEqualTo(SellerOrderStatus.DISPATCHED);
  }

  @Test
  void getSellerOrderOptions_returnsOkWithData() throws Exception {
    SellerOrderOptionsResponse response = new SellerOrderOptionsResponse(
        List.of(new SellerOrderOptionsResponse.ProductOption("SKU-001", "마스크팩 세트", 10, new java.math.BigDecimal("29.99"))),
        List.of(new SellerOrderOptionsResponse.ChannelOption("SHOPIFY", "Shopify"))
    );

    given(sellerOrderQueryService.getOrderOptions("SELLER-001"))
        .willReturn(response);

    mockMvc.perform(get("/orders/seller/options")
            .header("X-User-Id", "SELLER-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.products[0].sku").value("SKU-001"))
        .andExpect(jsonPath("$.data.products[0].productName").value("마스크팩 세트"))
        .andExpect(jsonPath("$.data.channels[0].value").value("SHOPIFY"))
        .andExpect(jsonPath("$.data.channels[0].label").value("Shopify"));
  }

  /*
   * X-User-Id 헤더가 없으면 GlobalExceptionHandler 가 401 Unauthorized 를 반환한다.
   */
  @Test
  void getSellerOrders_returnsUnauthorized_whenUserIdHeaderMissing() throws Exception {
    mockMvc.perform(get("/orders/seller/list"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getSellerOrderOptions_returnsUnauthorized_whenUserIdHeaderMissing() throws Exception {
    mockMvc.perform(get("/orders/seller/options"))
        .andExpect(status().isUnauthorized());
  }
}
