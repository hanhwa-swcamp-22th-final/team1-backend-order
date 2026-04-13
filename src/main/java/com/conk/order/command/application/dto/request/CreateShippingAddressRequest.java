package com.conk.order.command.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/* 배송지 요청 DTO. */
@Getter
public class CreateShippingAddressRequest {

  /** 기본 주소 (필수). */
  @NotBlank
  private String address1;

  /** 상세 주소 (선택). */
  private String address2;

  /** 도시 (필수). */
  @NotBlank
  private String city;

  /** 주/도 (선택). */
  private String state;

  /** 우편번호 (필수). */
  @NotBlank
  private String zipCode;
}
