package com.conk.order.query.dto.response;

import com.conk.order.command.domain.aggregate.Order;
import lombok.Getter;

/**
 * WMS 송장 발급용 배송지 DTO다.
 */
@Getter
public class InternalOrderShipmentResponse {

  private final String orderId;
  private final String sellerId;
  private final String warehouseId;
  private final String recipientName;
  private final String street1;
  private final String street2;
  private final String city;
  private final String state;
  private final String zip;
  private final String country;
  private final String phone;
  private final String email;

  private InternalOrderShipmentResponse(Order order) {
    this.orderId = order.getOrderId();
    this.sellerId = order.getSellerId();
    this.warehouseId = order.getWarehouseId();
    this.recipientName = order.getReceiverName();
    this.street1 = order.getShippingAddress() == null ? null : order.getShippingAddress().getAddress1();
    this.street2 = order.getShippingAddress() == null ? null : order.getShippingAddress().getAddress2();
    this.city = order.getShippingAddress() == null ? null : order.getShippingAddress().getCity();
    this.state = order.getShippingAddress() == null ? null : order.getShippingAddress().getState();
    this.zip = order.getShippingAddress() == null ? null : order.getShippingAddress().getZipCode();
    this.country = order.getShippingAddress() == null ? null : order.getShippingAddress().getCountry();
    this.phone = order.getReceiverPhoneNo();
    this.email = null;
  }

  public static InternalOrderShipmentResponse from(Order order) {
    return new InternalOrderShipmentResponse(order);
  }
}
