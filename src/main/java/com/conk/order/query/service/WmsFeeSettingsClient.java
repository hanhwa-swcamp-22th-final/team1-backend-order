package com.conk.order.query.service;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/*
 * WMS 내부 요금 조회 API용 Feign client.
 */
@FeignClient(name = "wmsFeeSettingsClient", url = "${wms.base-url}")
public interface WmsFeeSettingsClient {

  @GetMapping("/wms/fee-settings/internal")
  WmsApiResponse<FeeSettingRawItem> getFeeSettings(
      @RequestHeader("X-Seller-Id") String sellerId
  );

  @Getter
  @Setter
  @NoArgsConstructor
  class WmsApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  class FeeSettingRawItem {
    private BigDecimal fulfillmentFee;
    private BigDecimal packagingCost;
    private BigDecimal storageUnitCost;
  }
}
