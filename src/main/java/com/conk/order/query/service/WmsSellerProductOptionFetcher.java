package com.conk.order.query.service;

import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import com.conk.order.query.dto.response.SellerOrderOptionsResponse.ProductOption;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.stereotype.Component;

/*
 * WMS seller 상품 목록 API를 Feign으로 호출해 주문 등록용 상품 옵션으로 변환한다.
 */
@Component
public class WmsSellerProductOptionFetcher implements SellerProductOptionFetcher {

  private final WmsSellerProductClient wmsSellerProductClient;

  public WmsSellerProductOptionFetcher(WmsSellerProductClient wmsSellerProductClient) {
    this.wmsSellerProductClient = wmsSellerProductClient;
  }

  @Override
  public List<ProductOption> fetchProducts(String sellerId) {
    WmsSellerProductClient.WmsApiResponse<List<WmsSellerProductClient.WmsSellerProductItem>> response;

    try {
      response = wmsSellerProductClient.getSellerProducts(sellerId, sellerId, sellerId);
    } catch (Exception ex) {
      throw new BusinessException(ErrorCode.ORDER_OPTIONS_UNAVAILABLE);
    }

    if (response == null || !response.isSuccess()) {
      throw new BusinessException(ErrorCode.ORDER_OPTIONS_UNAVAILABLE);
    }

    List<WmsSellerProductClient.WmsSellerProductItem> products = response.getData();
    if (products == null || products.isEmpty()) {
      return List.of();
    }

    LinkedHashMap<String, ProductOption> deduplicated = new LinkedHashMap<>();
    for (WmsSellerProductClient.WmsSellerProductItem product : products) {
      if (product == null) {
        continue;
      }
      String sku = normalizeText(product.getSku());
      if (sku.isBlank()) {
        continue;
      }
      String productName = normalizeText(product.getProductName());
      deduplicated.putIfAbsent(
          sku,
          new ProductOption(
              sku,
              productName.isBlank() ? sku : productName,
              product.getAvailableStock() == null ? 0 : product.getAvailableStock(),
              product.getSalePrice() == null ? java.math.BigDecimal.ZERO : product.getSalePrice()
          )
      );
    }

    return List.copyOf(deduplicated.values());
  }

  private String normalizeText(Object value) {
    return value == null ? "" : String.valueOf(value).trim();
  }
}
