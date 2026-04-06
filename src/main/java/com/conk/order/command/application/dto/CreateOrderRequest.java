package com.conk.order.command.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

/*
 * 셀러 단건 주문 등록 요청 DTO.
 *
 * orderNo 는 선택값이다.
 * - null 이면 서버가 UUID 로 자동 생성한다.
 * - 값이 있으면 해당 값을 사용하며 중복 여부를 검증한다.
 * orderChannel 은 이 엔드포인트 전용으로 MANUAL 로 고정된다.
 */
@Getter
public class CreateOrderRequest {

  /** 주문번호 (선택). null 이면 서버 자동 생성. */
  private String orderNo;

  /** 셀러 식별자 (필수). */
  @NotBlank
  private String sellerId;

  /** 주문 일시 (필수). */
  @NotNull
  private LocalDateTime orderedAt;

  /** 주문 항목 목록 (필수, 1개 이상). */
  @NotEmpty
  @Valid
  private List<CreateOrderItemRequest> items;

  /** 배송지 정보 (필수). */
  @NotNull
  @Valid
  private CreateShippingAddressRequest shippingAddress;

  /** 수령인 이름 (필수). */
  @NotBlank
  private String receiverName;

  /** 수령인 연락처 (필수). */
  @NotBlank
  private String receiverPhoneNo;

  /** 메모 (선택). */
  private String memo;
}
