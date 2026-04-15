package com.conk.order.query.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;

/*
 * 셀러 마진 시뮬레이터 preset 응답 DTO.
 *
 * FE SellerMarginSimulatorView.vue 가 화면 진입 시 필요한 초기값을 반환한다.
 *   - products: 셀러 보유 상품 목록 + 단가 정보
 *   - channels: 판매 채널 목록 + 기본 수수료율
 */
@Getter
public class SellerMarginPresetsResponse {

  private final List<ProductPreset> products;
  private final List<ChannelPreset> channels;

  public SellerMarginPresetsResponse(List<ProductPreset> products, List<ChannelPreset> channels) {
    this.products = products == null ? List.of() : List.copyOf(products);
    this.channels = channels == null ? List.of() : List.copyOf(channels);
  }

  @Getter
  public static class ProductPreset {

    private final String sku;
    private final String productName;
    /** WMS product.sale_price_amt */
    private final BigDecimal defaultSalePrice;
    /** WMS product.cost_price_amt */
    private final BigDecimal productCost;
    /** fee_setting.pick_base_rate_amt */
    private final BigDecimal fulfillmentFee;
    /** fee_setting.packing_material_rate_amt */
    private final BigDecimal packagingCost;
    /** fee_setting.storage_pallet_rate_amt */
    private final BigDecimal storageUnitCost;

    public ProductPreset(
        String sku,
        String productName,
        BigDecimal defaultSalePrice,
        BigDecimal productCost,
        BigDecimal fulfillmentFee,
        BigDecimal packagingCost,
        BigDecimal storageUnitCost
    ) {
      this.sku = sku;
      this.productName = productName;
      this.defaultSalePrice = defaultSalePrice == null ? BigDecimal.ZERO : defaultSalePrice;
      this.productCost = productCost == null ? BigDecimal.ZERO : productCost;
      this.fulfillmentFee = fulfillmentFee == null ? BigDecimal.ZERO : fulfillmentFee;
      this.packagingCost = packagingCost == null ? BigDecimal.ZERO : packagingCost;
      this.storageUnitCost = storageUnitCost == null ? BigDecimal.ZERO : storageUnitCost;
    }
  }

  @Getter
  public static class ChannelPreset {

    private final String key;
    private final String label;
    private final double defaultFeeRate;

    public ChannelPreset(String key, String label, double defaultFeeRate) {
      this.key = key;
      this.label = label;
      this.defaultFeeRate = defaultFeeRate;
    }
  }
}
