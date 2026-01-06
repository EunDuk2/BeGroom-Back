package com.example.BeGroom.order.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.product.domain.ProductDetail;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "order_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    @JoinColumn(name = "product_detail_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductDetail productDetail;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer price;

    private OrderProduct(Order order, ProductDetail productDetail, Integer quantity, Integer price) {
        this.order = order;
        this.productDetail = productDetail;
        this.quantity = quantity;
        this.price = price;
    }

    public static OrderProduct create(Order order, ProductDetail productDetail, Integer quantity, Integer price) {
        return new OrderProduct(order, productDetail, quantity, price);
    }

    public Long getTotalAmount() {
        return (long) getPrice() * getQuantity();
    }

}
