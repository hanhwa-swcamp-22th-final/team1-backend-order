package com.conk.order.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.conk.order.query.dto.response.SellerOrderOptionsResponse.ProductOption;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/*
 * WMS 상품 옵션 fetcher 단위 테스트.
 *
 * Feign client 를 Mockito 로 대체하고, 응답 매핑과 헤더 전달만 검증한다.
 */
class WmsSellerProductOptionFetcherTest {

  @Test
  void fetchProducts_mapsWmsProductListResponse() {
    WmsSellerProductClient client = mock(WmsSellerProductClient.class);
    WmsSellerProductClient.WmsSellerProductItem item1 = new WmsSellerProductClient.WmsSellerProductItem();
    item1.setSku("SKU-001");
    item1.setProductName("마스크팩 세트");
    item1.setAvailableStock(10);
    item1.setSalePrice(new BigDecimal("29.99"));
    WmsSellerProductClient.WmsSellerProductItem item2 = new WmsSellerProductClient.WmsSellerProductItem();
    item2.setSku("SKU-002");
    item2.setProductName("앰플");
    item2.setAvailableStock(50);
    item2.setSalePrice(new BigDecimal("15.00"));
    WmsSellerProductClient.WmsApiResponse<List<WmsSellerProductClient.WmsSellerProductItem>> response =
        new WmsSellerProductClient.WmsApiResponse<>();
    response.setSuccess(true);
    response.setData(List.of(item1, item2));

    given(client.getSellerProducts("SELLER-001", "SELLER-001", "SELLER-001", "true"))
        .willReturn(response);

    WmsSellerProductOptionFetcher fetcher = new WmsSellerProductOptionFetcher(client);

    List<ProductOption> result = fetcher.fetchProducts("SELLER-001");

    assertThat(result)
        .extracting(
            ProductOption::getSku,
            ProductOption::getProductName,
            ProductOption::getAvailableStock,
            ProductOption::getUnitPrice)
        .containsExactly(
            org.assertj.core.groups.Tuple.tuple("SKU-001", "마스크팩 세트", 10, new BigDecimal("29.99")),
            org.assertj.core.groups.Tuple.tuple("SKU-002", "앰플", 50, new BigDecimal("15.00"))
        );
    verify(client).getSellerProducts("SELLER-001", "SELLER-001", "SELLER-001", "true");
  }
}
