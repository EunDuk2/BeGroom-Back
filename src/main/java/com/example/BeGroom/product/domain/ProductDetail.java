package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.product.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_detail_id")
    private Long productDetailId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "base_price", nullable = false)
    private Integer basePrice;

    @Column(name = "discounted_price")
    private Integer discountedPrice;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    public int getSellingPrice() {
        return discountedPrice != null ? discountedPrice : basePrice;
    }

    public void validateOrderable(int quantity) {
        if(this.quantity < quantity) throw new InsufficientStockException(this.productDetailId);
    }

    public void decreaseStock(int quantity) {
        if (this.quantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다. productDetailId=" + this.productDetailId);
        }
        this.quantity -= quantity;
    }

    public void increaseStock(int quantity) {
        this.quantity += quantity;
    }

}