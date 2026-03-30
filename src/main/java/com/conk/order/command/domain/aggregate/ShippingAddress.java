package com.conk.order.command.domain.aggregate;

import lombok.Getter;

/*
 * - 배송지 값 객체
 *
 *   주문 배송에 필요한 주소 정보를 표현한다.
 *   현재 단계에서는 필수 주소 정보 최소한으로 검증한다.
 * */
@Getter
public class ShippingAddress {

  private final String address1;
  private final String address2;
  private final String city;
  private final String state;
  private final String zipCode;

  private ShippingAddress(String address1, String address2, String city, String state, String zipCode) {
    validateAddress1(address1);
    validateCity(city);
    validateZipCode(zipCode);
    this.address1 = address1;
    this.address2 = address2;
    this.city = city;
    this.state = state;
    this.zipCode = zipCode;
  }

  public static ShippingAddress create(String address1, String address2, String city, String state, String zipCode) {
    return new ShippingAddress(address1,address2, city, state, zipCode);
  }

  private void validateAddress1(String address1) {
    if (address1 == null || address1.isBlank()) {
      throw new IllegalArgumentException("Address1 is required.");
    }
  }

  private void validateCity(String city) {
    if (city == null || city.isBlank()) {
      throw new IllegalArgumentException("City is required.");
    }
  }

  private void validateZipCode(String zipCode) {
    if (zipCode == null || zipCode.isBlank()) {
      throw new IllegalArgumentException("Zip code is required.");
    }
  }
}
