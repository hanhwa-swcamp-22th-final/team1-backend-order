package com.conk.order.command.application.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.application.dto.response.CreateOrderResponse;
import com.conk.order.command.application.service.SellerOrderCommandService;
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
@WebMvcTest(SellerOrderCommandController.class)
class CreateOrderControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private SellerOrderCommandService sellerOrderCommandService;

  /* 정상 요청 시 201 Created 와 success/message/data 형식으로 응답한다. */
  @Test
  void createOrder_returnsCreatedWithOrderNo() throws Exception {
    given(sellerOrderCommandService.create(any(), eq("SELLER-001"), eq("TENANT-001")))
        .willReturn(new CreateOrderResponse("ORD-UUID-001"));

    String requestBody = objectMapper.writeValueAsString(buildRequestBody());

    mockMvc.perform(post("/orders/seller/manual")
            .header("X-User-Id", "SELLER-001")
            .header("X-Tenant-Id", "TENANT-001")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("주문이 등록되었습니다."))
        .andExpect(jsonPath("$.data.orderId").value("ORD-UUID-001"));
  }

  /*
   * X-User-Id 헤더가 없으면 GlobalExceptionHandler 가 401 Unauthorized 를 반환한다.
   * 운영에서는 NGINX 가 항상 헤더를 주입하므로 발생하지 않는 케이스.
   */
  @Test
  void createOrder_returnsUnauthorized_whenUserIdHeaderMissing() throws Exception {
    mockMvc.perform(post("/orders/seller/manual")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(buildRequestBody())))
        .andExpect(status().isUnauthorized());
  }

  /* items 가 없으면 400 Bad Request 를 반환한다. */
  @Test
  void createOrder_returnsBadRequest_whenItemsMissing() throws Exception {
    Map<String, Object> body = buildRequestBody();
    body.remove("items");

    mockMvc.perform(post("/orders/seller/manual")
            .header("X-User-Id", "SELLER-001")
            .header("X-Tenant-Id", "TENANT-001")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isBadRequest());
  }

  /* shippingAddress.state 가 없으면 400 Bad Request 를 반환한다. */
  @Test
  void createOrder_returnsBadRequest_whenStateMissing() throws Exception {
    Map<String, Object> body = buildRequestBody();
    @SuppressWarnings("unchecked")
    Map<String, Object> shippingAddress = new java.util.HashMap<>(
        (Map<String, Object>) body.get("shippingAddress"));
    shippingAddress.remove("state");
    body.put("shippingAddress", shippingAddress);

    mockMvc.perform(post("/orders/seller/manual")
            .header("X-User-Id", "SELLER-001")
            .header("X-Tenant-Id", "TENANT-001")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isBadRequest());
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  /* 기본 요청 바디를 Map 으로 구성한다. sellerId 는 body 에 없고 헤더에서 온다. */
  private Map<String, Object> buildRequestBody() {
    return new java.util.HashMap<>(Map.of(
        "orderedAt", LocalDateTime.of(2026, 4, 3, 10, 0).toString(),
        "items", List.of(Map.of("sku", "SKU-001", "quantity", 2)),
        "shippingAddress", Map.of(
            "address1", "서울시 강남구 테헤란로 123",
            "city", "Seoul",
            "state", "CA",
            "zipCode", "06236"
        ),
        "receiverName", "홍길동",
        "receiverPhoneNo", "010-1234-5678"
    ));
  }
}
