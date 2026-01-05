package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.BeGroom.product.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "product_no", nullable = false, unique = true)
    private Long productNo;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

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

    @Column(name = "expiration_date", columnDefinition = "TEXT")
    private String expirationDate;

    @Column(name = "guides", columnDefinition = "TEXT")
    private String guides;

    @Column(name = "product_detail", columnDefinition = "LONGTEXT")
    private String productDetail;

    @Column(name = "product_notice", columnDefinition = "JSON")
    private String productNotice;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "productId", fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    public enum ProductStatus {
        WAIT, SALE, SOLD_OUT, STOP
    }

    public void updateProductOption(String expirationDate,
                                    List<String> guides,
                                    String productDetail,
                                    List<?> productNotice) {
        this.expirationDate = expirationDate;

        // guides JSON 문자열로 변환
        if (guides != null && !guides.isEmpty()) {
            try {
                this.guides = new ObjectMapper().writeValueAsString(guides);
            } catch (Exception e) {
                this.guides = null;
            }
        }

        this.productDetail = productDetail;

        // productNotice JSON 문자열로 변환
        if (productNotice != null && !productNotice.isEmpty()) {
            try {
                this.productNotice = new ObjectMapper().writeValueAsString(productNotice);
            } catch (Exception e) {
                this.productNotice = null;
            }
        }
    }

    public void updateBrandId(Long brandId) {
        this.brandId = brandId;
    }
}