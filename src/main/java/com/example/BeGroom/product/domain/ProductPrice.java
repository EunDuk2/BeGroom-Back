package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.product.domain.vo.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_price")
@Entity
public class ProductPrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id", nullable = false)
    private ProductDetail productDetail;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "original_price", nullable = false))
    private Money originalPrice;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "discounted_price"))
    private Money discountedPrice;

    @Builder
    private ProductPrice(ProductDetail productDetail, Integer originalPrice, Integer discountedPrice) {
        Assert.notNull(productDetail, "상세 상품은 필수입니다.");

        this.originalPrice = Money.of(originalPrice);
        this.discountedPrice = (discountedPrice != null) ? Money.of(discountedPrice) : null;

        if (this.discountedPrice != null && this.discountedPrice.isGreaterThan(this.originalPrice)) {
            throw new IllegalArgumentException("할인가는 원가보다 클 수 없습니다.");
        }

        this.productDetail = productDetail;
    }

    public int getFinalPrice() {
        return (discountedPrice != null) ? discountedPrice.getAmount() : originalPrice.getAmount();
    }

    public int getDiscountRate() {
        return originalPrice.calculateDiscountRate(discountedPrice);
    }
}
