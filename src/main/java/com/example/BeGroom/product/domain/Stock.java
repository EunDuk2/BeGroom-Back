package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import io.jsonwebtoken.lang.Assert;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stock")
@Entity
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id", nullable = false, unique = true)
    private ProductDetail productDetail;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    private Stock(ProductDetail productDetail, Integer quantity) {
        Assert.notNull(productDetail, "상세 상품은 필수입니다.");
        Assert.isTrue(quantity >= 0, "재고 수량은 0 이상이어야 합니다.");

        this.productDetail = productDetail;
        this.quantity = quantity;
    }

    public void addQuantity(int amount) {
        this.quantity += amount;
    }

    public void removeQuantity(int amount) {
        int restQuantity = this.quantity - amount;
        if (restQuantity < 0) {
            throw new IllegalArgumentException("재고가 부족합니다. (현재 재고: " + this.quantity + ")");
        }
        this.quantity = restQuantity;
    }

    public boolean isSoldOut() {
        return this.quantity <= 0;
    }
}
