package com.conk.order.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.conk.order.query.dto.response.SellerMarginPresetsResponse;
import com.conk.order.query.dto.response.SellerMarginPresetsResponse.ChannelPreset;
import com.conk.order.query.dto.response.SellerMarginPresetsResponse.ProductPreset;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/*
 * SellerMarginPresetsQueryService 단위 테스트.
 * WMS 클라이언트를 stub 처리해 Spring 컨텍스트 없이 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class SellerMarginPresetsQueryServiceTest {

  @Mock
  private WmsSellerProductClient wmsSellerProductClient;

  @Mock
  private WmsFeeSettingsClient wmsFeeSettingsClient;

  @InjectMocks
  private SellerMarginPresetsQueryService service;

  @Test
  @DisplayName("WMS 상품 목록과 요금 정보가 정상 반환되면 products와 channels를 조립해 반환한다")
  void getPresets_whenWmsReturnsData_thenAssemblePresetsCorrectly() {
    // 상품 stub
    WmsSellerProductClient.WmsSellerProductItem item = new WmsSellerProductClient.WmsSellerProductItem();
    item.setSku("SKU-001");
    item.setProductName("테스트 상품");
    item.setSalePrice(new BigDecimal("29.99"));
    item.setCostPrice(new BigDecimal("12.00"));
    item.setAvailableStock(50);

    WmsSellerProductClient.WmsApiResponse<List<WmsSellerProductClient.WmsSellerProductItem>> productResponse =
        new WmsSellerProductClient.WmsApiResponse<>();
    productResponse.setSuccess(true);
    productResponse.setData(List.of(item));

    given(wmsSellerProductClient.getSellerProducts(anyString(), anyString(), anyString(), eq("true")))
        .willReturn(productResponse);

    // 요금 stub
    WmsFeeSettingsClient.FeeSettingRawItem feeItem = new WmsFeeSettingsClient.FeeSettingRawItem();
    feeItem.setFulfillmentFee(new BigDecimal("3.00"));
    feeItem.setPackagingCost(new BigDecimal("0.40"));
    feeItem.setStorageUnitCost(new BigDecimal("30.00"));

    WmsFeeSettingsClient.WmsApiResponse<WmsFeeSettingsClient.FeeSettingRawItem> feeResponse =
        new WmsFeeSettingsClient.WmsApiResponse<>();
    feeResponse.setSuccess(true);
    feeResponse.setData(feeItem);

    given(wmsFeeSettingsClient.getFeeSettings(anyString(), eq("true"))).willReturn(feeResponse);

    // 실행
    SellerMarginPresetsResponse result = service.getPresets("seller-1");

    // product 검증
    assertThat(result.getProducts()).hasSize(1);
    ProductPreset product = result.getProducts().get(0);
    assertThat(product.getSku()).isEqualTo("SKU-001");
    assertThat(product.getProductName()).isEqualTo("테스트 상품");
    assertThat(product.getDefaultSalePrice()).isEqualByComparingTo(new BigDecimal("29.99"));
    assertThat(product.getProductCost()).isEqualByComparingTo(new BigDecimal("12.00"));
    assertThat(product.getFulfillmentFee()).isEqualByComparingTo(new BigDecimal("3.00"));
    assertThat(product.getPackagingCost()).isEqualByComparingTo(new BigDecimal("0.40"));
    assertThat(product.getStorageUnitCost()).isEqualByComparingTo(new BigDecimal("30.00"));

    // channel 검증
    assertThat(result.getChannels()).isNotEmpty();
    ChannelPreset shopify = result.getChannels().stream()
        .filter(c -> c.getKey().equals("SHOPIFY"))
        .findFirst()
        .orElseThrow();
    assertThat(shopify.getLabel()).isEqualTo("Shopify");
    assertThat(shopify.getDefaultFeeRate()).isEqualTo(15.0);

    ChannelPreset manual = result.getChannels().stream()
        .filter(c -> c.getKey().equals("MANUAL"))
        .findFirst()
        .orElseThrow();
    assertThat(manual.getDefaultFeeRate()).isEqualTo(0.0);
  }

  @Test
  @DisplayName("WMS 요금 조회가 실패하면 기본값(fulfillmentFee=2.50)으로 fallback한다")
  void getPresets_whenFeeSettingsFails_thenUseFallbackValues() {
    WmsSellerProductClient.WmsSellerProductItem item = new WmsSellerProductClient.WmsSellerProductItem();
    item.setSku("SKU-001");
    item.setProductName("상품");
    item.setSalePrice(new BigDecimal("20.00"));

    WmsSellerProductClient.WmsApiResponse<List<WmsSellerProductClient.WmsSellerProductItem>> productResponse =
        new WmsSellerProductClient.WmsApiResponse<>();
    productResponse.setSuccess(true);
    productResponse.setData(List.of(item));

    given(wmsSellerProductClient.getSellerProducts(anyString(), anyString(), anyString(), eq("true")))
        .willReturn(productResponse);

    given(wmsFeeSettingsClient.getFeeSettings(anyString(), eq("true")))
        .willThrow(new RuntimeException("WMS 연결 실패"));

    SellerMarginPresetsResponse result = service.getPresets("seller-1");

    assertThat(result.getProducts()).hasSize(1);
    ProductPreset product = result.getProducts().get(0);
    assertThat(product.getFulfillmentFee()).isEqualByComparingTo(new BigDecimal("2.50"));
    assertThat(product.getPackagingCost()).isEqualByComparingTo(new BigDecimal("0.30"));
    assertThat(product.getStorageUnitCost()).isEqualByComparingTo(new BigDecimal("28.50"));
  }

  @Test
  @DisplayName("WMS 상품 목록이 비어 있으면 빈 products를 반환한다")
  void getPresets_whenNoProducts_thenReturnEmptyProductList() {
    WmsSellerProductClient.WmsApiResponse<List<WmsSellerProductClient.WmsSellerProductItem>> productResponse =
        new WmsSellerProductClient.WmsApiResponse<>();
    productResponse.setSuccess(true);
    productResponse.setData(List.of());

    given(wmsSellerProductClient.getSellerProducts(anyString(), anyString(), anyString(), eq("true")))
        .willReturn(productResponse);

    WmsFeeSettingsClient.WmsApiResponse<WmsFeeSettingsClient.FeeSettingRawItem> feeResponse =
        new WmsFeeSettingsClient.WmsApiResponse<>();
    feeResponse.setSuccess(true);
    feeResponse.setData(new WmsFeeSettingsClient.FeeSettingRawItem());

    given(wmsFeeSettingsClient.getFeeSettings(anyString(), eq("true"))).willReturn(feeResponse);

    SellerMarginPresetsResponse result = service.getPresets("seller-1");

    assertThat(result.getProducts()).isEmpty();
    assertThat(result.getChannels()).isNotEmpty();
  }
}
