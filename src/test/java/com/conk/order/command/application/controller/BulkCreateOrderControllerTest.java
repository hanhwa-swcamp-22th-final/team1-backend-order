package com.conk.order.command.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.application.dto.response.BulkCreateOrderResponse;
import com.conk.order.command.application.dto.response.BulkValidateResponse;
import com.conk.order.command.application.dto.response.FailedRow;
import com.conk.order.command.application.service.BulkOrderCommandService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/*
 * ORD-003 엑셀 일괄 주문 등록 컨트롤러 단위 테스트.
 *
 * sellerId 는 X-User-Id 헤더에서 추출한다.
 */
@WebMvcTest(BulkOrderCommandController.class)
class BulkCreateOrderControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BulkOrderCommandService bulkOrderCommandService;

  /* 정상 요청 시 200 OK 와 success/message/data 형식으로 응답한다. */
  @Test
  void bulkCreate_returnsOkWithResult() throws Exception {
    BulkCreateOrderResponse mockResponse =
        new BulkCreateOrderResponse(2, List.of());

    given(bulkOrderCommandService.create(any(), eq("SELLER-001"), eq("TENANT-001")))
        .willReturn(mockResponse);

    MockMultipartFile file = new MockMultipartFile(
        "file", "orders.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "dummy".getBytes()
    );

    mockMvc.perform(multipart("/orders/seller/bulk")
            .file(file)
            .header("X-User-Id", "SELLER-001")
            .header("X-Tenant-Id", "TENANT-001")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("일괄 주문이 등록되었습니다."))
        .andExpect(jsonPath("$.data.successCount").value(2))
        .andExpect(jsonPath("$.data.failedRows").isEmpty());
  }

  /* 일부 실패 행이 있어도 200 OK 와 함께 failedRows 를 반환한다. */
  @Test
  void bulkCreate_returnsOkWithFailedRows_whenSomeRowsFail() throws Exception {
    BulkCreateOrderResponse mockResponse =
        new BulkCreateOrderResponse(1, List.of(new FailedRow(3, "SKU는 필수입니다.")));

    given(bulkOrderCommandService.create(any(), eq("SELLER-001"), eq("TENANT-001")))
        .willReturn(mockResponse);

    MockMultipartFile file = new MockMultipartFile(
        "file", "orders.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "dummy".getBytes()
    );

    mockMvc.perform(multipart("/orders/seller/bulk")
            .file(file)
            .header("X-User-Id", "SELLER-001")
            .header("X-Tenant-Id", "TENANT-001")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.successCount").value(1))
        .andExpect(jsonPath("$.data.failedRows[0].rowNumber").value(3))
        .andExpect(jsonPath("$.data.failedRows[0].reason").value("SKU는 필수입니다."));
  }

  /*
   * X-User-Id 헤더가 없으면 GlobalExceptionHandler 가 401 Unauthorized 를 반환한다.
   */
  @Test
  void bulkCreate_returnsUnauthorized_whenUserIdHeaderMissing() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file", "orders.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "dummy".getBytes()
    );

    mockMvc.perform(multipart("/orders/seller/bulk")
            .file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isUnauthorized());
  }

  /* file 파트가 없으면 400 Bad Request 를 반환한다. */
  @Test
  void bulkCreate_returnsBadRequest_whenFileMissing() throws Exception {
    mockMvc.perform(multipart("/orders/seller/bulk")
            .header("X-User-Id", "SELLER-001")
            .header("X-Tenant-Id", "TENANT-001")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
  }

  /* validate 엔드포인트는 검증 결과를 success/data 형식으로 반환한다. */
  @Test
  void validate_returnsOkWithSummary() throws Exception {
    given(bulkOrderCommandService.validate(any()))
        .willReturn(new BulkValidateResponse(2, 1,
            List.of(new BulkValidateResponse.RowError(3, "SKU가 비어있습니다."))));

    MockMultipartFile file = new MockMultipartFile(
        "file", "orders.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "dummy".getBytes()
    );

    mockMvc.perform(multipart("/orders/seller/bulk/validate")
            .file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.totalRows").value(2))
        .andExpect(jsonPath("$.data.validRows").value(1))
        .andExpect(jsonPath("$.data.errors[0].row").value(3))
        .andExpect(jsonPath("$.data.errors[0].message").value("SKU가 비어있습니다."));
  }
}
