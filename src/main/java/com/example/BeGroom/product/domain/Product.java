package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.product.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_no")
    private Long productNo;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "sales_price", nullable = false)
    private Integer salesPrice;

    @Column(name = "discounted_price")
    private Integer discountedPrice;

    @Column(name = "discount_rate")
    private Integer discountRate;

    @Column(name = "is_buy_now", nullable = false)
    @Builder.Default
    private Boolean isBuyNow = false;

    @Column(name = "is_purchase_status", nullable = false)
    @Builder.Default
    private Boolean isPurchaseStatus = false;

    @Column(name = "is_only_adult", nullable = false)
    @Builder.Default
    private Boolean isOnlyAdult = false;

    @Column(name = "is_sold_out", nullable = false)
    @Builder.Default
    private Boolean isSoldOut = false;

    @Column(name = "sold_out_title", length = 100)
    @Builder.Default
    private String soldOutTitle = "Coming Soon";

    @Column(name = "sold_out_text", columnDefinition = "TEXT")
    private String soldOutText;

    @Column(name = "can_restock_notify")
    @Builder.Default
    private Boolean canRestockNotify = false;

    @Column(name = "is_low_stock", nullable = false)
    @Builder.Default
    private Boolean isLowStock = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false)
    @Builder.Default
    private ProductStatus productStatus = ProductStatus.WAIT;

    @Column(name = "wishlist_count", nullable = false)
    @Builder.Default
    private Integer wishlistCount = 0;

    @Column(name = "sales_count", nullable = false)
    @Builder.Default
    private Integer salesCount = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum ProductStatus {
        WAIT, SALE, SOLD_OUT, STOP
    }

    public void validateOrderable(int quantity) {
        if(getSalesCount() < quantity) throw new InsufficientStockException(getProductId());
    }
}
