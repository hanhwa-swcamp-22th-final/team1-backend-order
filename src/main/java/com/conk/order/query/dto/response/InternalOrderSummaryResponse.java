package com.conk.order.query.dto.response;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderItem;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

/**
 * WMS 내부 연동용 주문 요약 DTO다.
 */
@Getter
public class InternalOrderSummaryResponse {

  private final String orderId;
  private final String sellerId;
  private final String sellerName;
  private final String warehouseId;
  private final String channel;
  private final String orderStatus;
  private final String recipientName;
  private final String street1;
  private final String street2;
  private final String cityName;
  private final String state;
  private final String zip;
  private final String country;
  private final String phone;
  private final String email;
  private final LocalDateTime orderedAt;
  private final List<Item> items;

  private InternalOrderSummaryResponse(Order order) {
    this.orderId = order.getOrderId();
    this.sellerId = order.getSellerId();
    this.sellerName = order.getSellerId();
    this.warehouseId = order.getWarehouseId();
    this.channel = order.getOrderChannel() == null ? null : order.getOrderChannel().name();
    this.orderStatus = order.getStatus() == null ? null : order.getStatus().name();
    this.recipientName = order.getReceiverName();
    this.street1 = order.getShippingAddress() == null ? null : order.getShippingAddress().getAddress1();
    this.street2 = order.getShippingAddress() == null ? null : order.getShippingAddress().getAddress2();
    this.cityName = order.getShippingAddress() == null ? null : order.getShippingAddress().getCity();
    this.state = order.getShippingAddress() == null ? null : order.getShippingAddress().getState();
    this.zip = order.getShippingAddress() == null ? null : order.getShippingAddress().getZipCode();
    this.country = order.getShippingAddress() == null ? null : order.getShippingAddress().getCountry();
    this.phone = order.getReceiverPhoneNo();
    this.email = null;
    this.orderedAt = order.getOrderedAt();
    this.items = order.getItems().stream()
        .map(Item::new)
        .toList();
  }

  public static InternalOrderSummaryResponse from(Order order) {
    return new InternalOrderSummaryResponse(order);
  }

  @Getter
  public static class Item {
    private final String skuId;
    private final String productName;
    private final int quantity;

    private Item(OrderItem item) {
      this.skuId = item.getSku();
      this.productName = item.getProductNameSnapshot();
      this.quantity = item.getQuantity();
    }
  }
}
