package com.conk.order.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import com.conk.order.query.dto.response.SellerOrderOptionsResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/*
 * 셀러 주문 등록 옵션 조회 서비스 단위 테스트.
 *
 * 상품 목록은 외부 fetcher 를 stub 으로 대체하고,
 * 서비스가 채널 옵션과 함께 응답 DTO 를 올바르게 조립하는지만 검증한다.
 */
class SellerOrderOptionsQueryServiceTest {

  @Test
  void getOrderOptions_returnsProductsAndSelectableChannels() {
    SellerProductOptionFetcher fetcher = sellerId -> List.of(
        new SellerOrderOptionsResponse.ProductOption("SKU-001", "마스크팩 세트", 10, new BigDecimal("29.99")),
        new SellerOrderOptionsResponse.ProductOption("SKU-002", "앰플", 50, new BigDecimal("15.00"))
    );
    SellerOrderQueryService service = new SellerOrderQueryService(null, null, null, fetcher);

    SellerOrderOptionsResponse response = service.getOrderOptions("SELLER-001");

    assertThat(response.getProducts())
        .extracting(
            SellerOrderOptionsResponse.ProductOption::getSku,
            SellerOrderOptionsResponse.ProductOption::getProductName,
            SellerOrderOptionsResponse.ProductOption::getAvailableStock,
            SellerOrderOptionsResponse.ProductOption::getUnitPrice)
        .containsExactly(
            tuple("SKU-001", "마스크팩 세트", 10, new BigDecimal("29.99")),
            tuple("SKU-002", "앰플", 50, new BigDecimal("15.00"))
        );
    assertThat(response.getChannels())
        .extracting(SellerOrderOptionsResponse.ChannelOption::getValue,
            SellerOrderOptionsResponse.ChannelOption::getLabel)
        .containsExactly(tuple("SHOPIFY", "Shopify"));
  }
}
