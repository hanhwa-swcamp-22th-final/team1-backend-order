package com.conk.order.command.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.dto.CreateOrderResponse;
import com.conk.order.command.service.CreateOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/* ORD-002 셀러 단건 주문 등록 컨트롤러 단위 테스트. */
@WebMvcTest(CreateOrderController.class)
class CreateOrderControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private CreateOrderService createOrderService;

  /* 정상 요청 시 201 Created 와 success/message/data 형식으로 응답한다. */
  @Test
  void createOrder_returnsCreatedWithOrderNo() throws Exception {
    given(createOrderService.create(org.mockito.ArgumentMatchers.any()))
        .willReturn(new CreateOrderResponse("ORD-UUID-001"));

    String requestBody = objectMapper.writeValueAsString(buildRequestBody());

    mockMvc.perform(post("/orders/seller/manual")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("주문이 등록되었습니다."))
        .andExpect(jsonPath("$.data.orderNo").value("ORD-UUID-001"));
  }

  /* sellerId 가 없으면 400 Bad Request 를 반환한다. */
  @Test
  void createOrder_returnsBadRequest_whenSellerIdMissing() throws Exception {
    Map<String, Object> body = buildRequestBody();
    body.remove("sellerId");

    mockMvc.perform(post("/orders/seller/manual")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isBadRequest());
  }

  /* items 가 없으면 400 Bad Request 를 반환한다. */
  @Test
  void createOrder_returnsBadRequest_whenItemsMissing() throws Exception {
    Map<String, Object> body = buildRequestBody();
    body.remove("items");

    mockMvc.perform(post("/orders/seller/manual")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isBadRequest());
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  /* 기본 요청 바디를 Map 으로 구성한다. Map 이므로 필드 제거 테스트에 편리하다. */
  private Map<String, Object> buildRequestBody() {
    return new java.util.HashMap<>(Map.of(
        "sellerId", "SELLER-001",
        "orderedAt", LocalDateTime.of(2026, 4, 3, 10, 0).toString(),
        "items", List.of(Map.of("sku", "SKU-001", "quantity", 2)),
        "shippingAddress", Map.of(
            "address1", "서울시 강남구 테헤란로 123",
            "city", "Seoul",
            "zipCode", "06236"
        ),
        "receiverName", "홍길동",
        "receiverPhoneNo", "010-1234-5678"
    ));
  }
}
