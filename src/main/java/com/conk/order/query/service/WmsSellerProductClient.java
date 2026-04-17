package com.conk.order.query.service;

import com.conk.order.common.config.FeignConfig;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/*
 * WMS seller 상품 목록 API용 Feign client.
 */
@FeignClient(name = "wmsSellerProductClient", url = "${wms.base-url}", configuration = FeignConfig.class)
public interface WmsSellerProductClient {

  @GetMapping("/wms/products/seller/list")
  WmsApiResponse<List<WmsSellerProductItem>> getSellerProducts(
      @RequestHeader("X-User-Id") String userId,
      @RequestHeader("X-Seller-Id") String sellerId,
      @RequestHeader("X-Tenant-Id") String tenantId
  );

  @Getter
  @Setter
  @NoArgsConstructor
  class WmsApiResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  class WmsSellerProductItem {
    private String sku;
    private String productName;
    private java.math.BigDecimal salePrice;
    private java.math.BigDecimal costPrice;
    private Integer availableStock;
  }
}
