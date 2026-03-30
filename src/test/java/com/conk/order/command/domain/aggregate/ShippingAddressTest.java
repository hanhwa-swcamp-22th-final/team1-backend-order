package com.conk.order.command.domain.aggregate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/* ShippingAddress 도메인 규칙 테스트
*
* 배송지는 주문 배송을 위한 기본주소 정보이며,
* 핵심 필수값이 비어 있으면 생성될 수 없어야 한다.
* */
public class ShippingAddressTest {

  /* 필수 주소 정보로 배송지가 정상 생성되는지 확인한다. */
  @Test
  void createCreatesShippingAddressWithRequiredFields() {
    ShippingAddress shippingAddress = ShippingAddress.create(
        "서울시 강남구 테헤란로 123",
        "101동 202호",
        "Seoul",
        "Seoul",
        "06236"
    );

  assertThat(shippingAddress.getAddress1()).isEqualTo("서울시 강남구 테헤란로 123");
  assertThat(shippingAddress.getAddress2()).isEqualTo("101동 202호");
  assertThat(shippingAddress.getCity()).isEqualTo("Seoul");
  assertThat(shippingAddress.getState()).isEqualTo("Seoul");
  assertThat(shippingAddress.getZipCode()).isEqualTo("06236");
  }

  /* 기본 주소가 비어 있으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenAddress1IsBlank() {
    assertThatThrownBy(() -> ShippingAddress.create(" ", "101동 202호", "Seoul", "Seoul", "06236"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Address1 is required.");
  }

  /* 도시 정보가 비어 있으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenCityIsBlank() {
    assertThatThrownBy(() -> ShippingAddress.create("서울시 강남구 태헤란로 123", "101동 200호", " ", "Seoul", "06236"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("City is required.");
  }

  /* 우편번호가 비어 있으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenZipCodeIsBlank() {
    assertThatThrownBy(() -> ShippingAddress.create("서울시 강남구 테헤란로 123", "101동 202호", "Seoul", "Seoul", " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Zip code is required.");
  }
}
