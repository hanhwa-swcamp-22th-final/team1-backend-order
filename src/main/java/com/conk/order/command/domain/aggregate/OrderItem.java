package com.conk.order.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.Getter;

/*
 * - 주문 항목 도메인 객체.
 *
 *   한 주문 안에 포함되는 개별 상품 단위를 표현한다.
 * 현재 단계에서는 SKU와 수량만 최소 필드로 둔다.
 *   */
@Getter
@Entity
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "order_no")
  private Order order;

  private String sku;
  private int quantity;

  protected OrderItem() {}

  private OrderItem(String sku, int quantity) {
    validateSku(sku);
    validateQuantity(quantity);
    this.sku = sku;
    this.quantity = quantity;
  }

  public static OrderItem create(String sku, int quantity) {
    return new OrderItem(sku, quantity);
  }

  void assignOrder(Order order) {
    this.order = order;
  }

  private void validateSku(String sku) {
    if (sku == null || sku.isBlank()) {
      throw new IllegalArgumentException("SKU is required.");
    }
  }

  private void validateQuantity(int quantity) {
    if (quantity < 1) {
      throw new IllegalArgumentException("Quantity must be greater than zero.");
    }
  }
}
