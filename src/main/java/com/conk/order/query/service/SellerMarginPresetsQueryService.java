package com.conk.order.query.service;

import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import com.conk.order.query.dto.response.SellerMarginPresetsResponse;
import com.conk.order.query.dto.response.SellerMarginPresetsResponse.ChannelPreset;
import com.conk.order.query.dto.response.SellerMarginPresetsResponse.ProductPreset;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

/*
 * 셀러 마진 시뮬레이터 preset 조회 서비스.
 *
 * WMS에서 셀러 상품 목록(가격·원가)과 요금 정보(풀필먼트·포장·보관)를 조합해 반환한다.
 */
@Service
public class SellerMarginPresetsQueryService {

  private final WmsSellerProductClient wmsSellerProductClient;
  private final WmsFeeSettingsClient wmsFeeSettingsClient;

  public SellerMarginPresetsQueryService(
      WmsSellerProductClient wmsSellerProductClient,
      WmsFeeSettingsClient wmsFeeSettingsClient
  ) {
    this.wmsSellerProductClient = wmsSellerProductClient;
    this.wmsFeeSettingsClient = wmsFeeSettingsClient;
  }

  /* 셀러 마진 시뮬레이터 초기값을 반환한다. */
  public SellerMarginPresetsResponse getPresets(String sellerId) {
    WmsFeeSettingsClient.FeeSettingRawItem fees = fetchFeeSettings(sellerId);
    List<ProductPreset> products = fetchProducts(sellerId, fees);
    List<ChannelPreset> channels = buildChannelPresets();
    return new SellerMarginPresetsResponse(products, channels);
  }

  /* WMS에서 셀러 상품 목록을 조회해 ProductPreset 목록으로 변환한다. */
  private List<ProductPreset> fetchProducts(String sellerId, WmsFeeSettingsClient.FeeSettingRawItem fees) {
    WmsSellerProductClient.WmsApiResponse<List<WmsSellerProductClient.WmsSellerProductItem>> response;
    try {
      response = wmsSellerProductClient.getSellerProducts(sellerId, sellerId, sellerId, "true");
    } catch (Exception ex) {
      throw new BusinessException(ErrorCode.ORDER_OPTIONS_UNAVAILABLE);
    }

    if (response == null || !response.isSuccess()) {
      throw new BusinessException(ErrorCode.ORDER_OPTIONS_UNAVAILABLE);
    }

    List<WmsSellerProductClient.WmsSellerProductItem> items = response.getData();
    if (items == null || items.isEmpty()) {
      return List.of();
    }

    return items.stream()
        .filter(item -> item != null && item.getSku() != null && !item.getSku().isBlank())
        .map(item -> new ProductPreset(
            item.getSku().trim(),
            item.getProductName() == null ? item.getSku().trim() : item.getProductName().trim(),
            item.getSalePrice() == null ? BigDecimal.ZERO : item.getSalePrice(),
            item.getCostPrice() == null ? BigDecimal.ZERO : item.getCostPrice(),
            fees.getFulfillmentFee(),
            fees.getPackagingCost(),
            fees.getStorageUnitCost()
        ))
        .toList();
  }

  /* WMS에서 셀러 기준 요금 설정을 조회한다. 실패 시 기본값을 사용한다. */
  private WmsFeeSettingsClient.FeeSettingRawItem fetchFeeSettings(String sellerId) {
    try {
      WmsFeeSettingsClient.WmsApiResponse<WmsFeeSettingsClient.FeeSettingRawItem> response =
          wmsFeeSettingsClient.getFeeSettings(sellerId, "true");

      if (response != null && response.isSuccess() && response.getData() != null) {
        return response.getData();
      }
    } catch (Exception ignored) {
      // 요금 조회 실패 시 기본값으로 fallback
    }
    return defaultFeeItem();
  }

  /* OrderChannel enum을 채널 preset 목록으로 변환한다. */
  private List<ChannelPreset> buildChannelPresets() {
    return Arrays.stream(OrderChannel.values())
        .map(channel -> new ChannelPreset(
            channel.name(),
            toChannelLabel(channel),
            toDefaultFeeRate(channel)
        ))
        .toList();
  }

  private String toChannelLabel(OrderChannel channel) {
    return switch (channel) {
      case MANUAL -> "Manual";
      case EXCEL -> "Excel";
      case SHOPIFY -> "Shopify";
      default -> channel.name();
    };
  }

  private double toDefaultFeeRate(OrderChannel channel) {
    return switch (channel) {
      case SHOPIFY -> 15.0;
      default -> 0.0;
    };
  }

  private WmsFeeSettingsClient.FeeSettingRawItem defaultFeeItem() {
    WmsFeeSettingsClient.FeeSettingRawItem item = new WmsFeeSettingsClient.FeeSettingRawItem();
    item.setFulfillmentFee(new BigDecimal("2.50"));
    item.setPackagingCost(new BigDecimal("0.30"));
    item.setStorageUnitCost(new BigDecimal("28.50"));
    return item;
  }
}
