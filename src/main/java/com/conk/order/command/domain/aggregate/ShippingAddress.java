package com.conk.order.command.domain.aggregate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

/*
 * 배송지 값 객체.
 *
 * 주문 배송에 필요한 주소 정보를 표현한다.
 * sales_order 테이블의 ship_to_* 컬럼에 매핑된다.
 * address1, city, zipCode 는 필수값이며, address2 와 state 는 선택값이다.
 * country 는 현재 미국 전용 서비스로 "US" 고정이다.
 */
@Getter
@Embeddable
public class ShippingAddress {

  /** 기본 주소. sales_order.ship_to_address1 */
  @Column(name = "ship_to_address1")
  private String address1;

  /** 상세 주소. sales_order.ship_to_address2 */
  @Column(name = "ship_to_address2")
  private String address2;

  /** 도시. sales_order.ship_to_city */
  @Column(name = "ship_to_city")
  private String city;

  /** 주/도. sales_order.ship_to_state */
  @Column(name = "ship_to_state")
  private String state;

  /** 우편번호. sales_order.ship_to_zip_code */
  @Column(name = "ship_to_zip_code")
  private String zipCode;

  /** 국가. sales_order.ship_to_country — 현재 미국 전용 서비스로 "US" 고정. */
  @Column(name = "ship_to_country", nullable = false)
  private String country;

  protected ShippingAddress() {}

  private ShippingAddress(String address1, String address2, String city, String state, String zipCode) {
    validateAddress1(address1);
    validateCity(city);
    validateZipCode(zipCode);
    this.address1 = address1;
    this.address2 = address2;
    this.city = city;
    this.state = state;
    this.zipCode = zipCode;
    this.country = "US";
  }

  /**
   * 배송지 생성 팩토리 메서드.
   *
   * @param address1 기본 주소 (필수)
   * @param address2 상세 주소 (선택)
   * @param city     도시 (필수)
   * @param state    주/도 (선택)
   * @param zipCode  우편번호 (필수)
   * @return ShippingAddress
   */
  public static ShippingAddress create(
      String address1, String address2, String city, String state, String zipCode
  ) {
    return new ShippingAddress(address1, address2, city, state, zipCode);
  }

  /**
   * 기본 주소 필수값 검증.
   *
   * @param address1 기본 주소
   */
  private void validateAddress1(String address1) {
    if (address1 == null || address1.isBlank()) {
      throw new IllegalArgumentException("Address1 is required.");
    }
  }

  /**
   * 도시 필수값 검증.
   *
   * @param city 도시
   */
  private void validateCity(String city) {
    if (city == null || city.isBlank()) {
      throw new IllegalArgumentException("City is required.");
    }
  }

  /**
   * 우편번호 필수값 검증.
   *
   * @param zipCode 우편번호
   */
  private void validateZipCode(String zipCode) {
    if (zipCode == null || zipCode.isBlank()) {
      throw new IllegalArgumentException("Zip code is required.");
    }
  }
}
