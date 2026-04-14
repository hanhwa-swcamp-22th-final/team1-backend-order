package com.conk.order.query.dto.response;

import java.util.List;
import lombok.Getter;

/*
 * 셀러 주문 등록 화면 옵션 응답 DTO.
 *
 * FE 가 수동 주문 등록 화면에서 사용하는 상품 SKU 목록과 판매 채널 목록을 함께 반환한다.
 */
@Getter
public class SellerOrderOptionsResponse {

  private final List<ProductOption> products;
  private final List<ChannelOption> channels;

  public SellerOrderOptionsResponse(List<ProductOption> products, List<ChannelOption> channels) {
    this.products = products == null ? List.of() : List.copyOf(products);
    this.channels = channels == null ? List.of() : List.copyOf(channels);
  }

  @Getter
  public static class ProductOption {
    private final String sku;
    private final String productName;

    public ProductOption(String sku, String productName) {
      this.sku = sku;
      this.productName = productName;
    }
  }

  @Getter
  public static class ChannelOption {
    private final String value;
    private final String label;

    public ChannelOption(String value, String label) {
      this.value = value;
      this.label = label;
    }
  }
}
