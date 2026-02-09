package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.product.exception.InsufficientStockException;
import io.jsonwebtoken.lang.Assert;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_detail")
@Entity
public class ProductDetail extends BaseEntity {

    private static final int LOW_STOCK_THRESHOLD = 10;

//    @Version
//    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true)
    private Long no;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private Boolean isAvailable = true;

    @OneToMany(mappedBy = "productDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductPrice> prices = new ArrayList<>();

    @OneToOne(mappedBy = "productDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private Stock stock;

    @OneToMany(mappedBy = "productDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOptionMapping> optionMappings = new ArrayList<>();

    @Builder
    private ProductDetail(Product product, Long no, String name, Integer initialQuantity) {
        Assert.notNull(product, "상품 정보는 필수입니다.");
        Assert.notNull(no, "상품 번호는 필수입니다.");
        validateName(name);

        this.no = no;
        this.name = name;
        this.isAvailable = true;
        this.stock = Stock.builder()
            .productDetail(this)
            .quantity(initialQuantity != null ? initialQuantity : 0)
            .build();

        setProduct(product);
    }

    // 재고 증가 (입고)
    public void increaseStock(int amount) {
        boolean isSoldOut = this.isSoldOut();
        this.stock.addQuantity(amount);

        // 재입고 시 판매 가능 상태로 변경
        if (isSoldOut && !this.stock.isSoldOut()) {
            this.isAvailable = true;
        }
    }

    // 재고 감소 (주문)
    public void decreaseStock(int amount) {
        this.stock.removeQuantity(amount);

        if (this.stock.isSoldOut()) {
            this.isAvailable = false;
        }
    }

    // 주문 가능 확인
    public void validateOrderable(int quantity) {
        if (!this.isAvailable) {
            throw new IllegalArgumentException("현재 판매 중지된 옵션입니다.");
        }

        if (this.stock.getQuantity() < quantity) {
            throw new InsufficientStockException(this.id);
        }
    }

    public boolean isSoldOut() {
        return this.stock == null || this.stock.isSoldOut();
    }

    public boolean isLowStock() {
        return this.stock != null && this.stock.getQuantity() > 0
            && this.stock.getQuantity() <= LOW_STOCK_THRESHOLD;
    }

    public Integer getOriginalPrice() {
        ProductPrice latestPrice = getLatestPrice();
        return latestPrice != null ? latestPrice.getOriginalPrice().getAmount() : 0;
    }

    public int getSellingPrice() {
        ProductPrice latestPrice = getLatestPrice();
        return latestPrice != null ? latestPrice.getFinalPrice() : 0;
    }

    public int getDiscountRate() {
        ProductPrice latestPrice = getLatestPrice();
        return latestPrice != null ? latestPrice.getDiscountRate() : 0;
    }

    private ProductPrice getLatestPrice() {
        return this.prices.isEmpty() ? null : this.prices.get(this.prices.size() - 1);
    }

    public void addPrice(Integer originalPrice, Integer discountedPrice) {
        ProductPrice newPrice = ProductPrice.builder()
            .productDetail(this)
            .originalPrice(originalPrice)
            .discountedPrice(discountedPrice)
            .build();
        this.prices.add(newPrice);
    }

    // 연관관계 편의 메서드
    protected void setProduct(Product product) {

        if (this.product != null) {
            this.product.getProductDetails().remove(this);
        }

        this.product = product;
        if (product != null && !product.getProductDetails().contains(this)) {
            product.getProductDetails().add(this);
        }
    }

    public void addOption(ProductOption option) {
        ProductOptionMapping.builder()
            .productDetail(this)
            .productOption(option)
            .build();
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상세 상품명은 필수입니다.");
        }
    }
}