package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.product.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductDetail extends BaseEntity {

    private static final int LOW_STOCK_THRESHOLD = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_detail_id")
    private Long productDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

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

    @OneToMany(mappedBy = "productDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductOptionMapping> optionMappings = new ArrayList<>();


    // 기본 정보 및 가격 수정
    public void updateName(String name) {
        validateName(name);
        this.name = name;
    }

    public void updatePrice(Integer basePrice, Integer discountedPrice) {
        validatePrice(basePrice, discountedPrice);
        this.basePrice = basePrice;
        this.discountedPrice = discountedPrice;
    }

    // 재고 수량 직접 수정 (관리자용)
    public void updateQuantity(Integer quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("재고 수량은 0개 이상이어야 합니다.");
        }
        this.quantity = quantity;
        autoUpdateAvailability();
        updateParentProductStatus();
    }

    // 재고 증가 (입고)
    public void increaseStock(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("입고 수량은 1개 이상이어야 합니다.");
        }
        this.quantity += amount;

        // 재입고 시 판매 가능 상태로 변경
        if (this.quantity > 0) {
            this.isAvailable = true;
        }

        updateParentProductStatus();
    }

    // 재고 감소 (주문)
    public void decreaseStock(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("차감 수량은 1개 이상이어야 합니다.");
        }
        if (this.quantity < amount) {
            throw new IllegalStateException("재고가 부족합니다. (현재 재고: " + this.quantity + ")");
        }
        this.quantity -= amount;
        autoUpdateAvailability();
        updateParentProductStatus();
    }

    // 재고에 따른 상태 자동 변경 (내부 로직)
    private void autoUpdateAvailability() {
        if (this.quantity <= 0) {
            this.isAvailable = false;
        }
    }

    // Product 상태 업데이트 트리거 - 모든 ProductDetail이 품절이면 Product도 품절 처리
    private void updateParentProductStatus() {
        if(this.product != null) {
            this.product.updateStockStatusFromDetails();
        }
    }

    // 품절 임박 확인
    public boolean isLowStock() {
        return this.quantity > 0  && this.quantity <= LOW_STOCK_THRESHOLD;
    }

    // 주문 가능 확인
    public void validateOrderable(int quantity) {
        if (!this.isAvailable) {
            throw new IllegalStateException("현재 판매 중지된 옵션입니다.");
        }
        if (this.quantity < quantity) {
            throw new InsufficientStockException(this.productDetailId);
        }
    }

    // 판매가
    public int getSellingPrice() {
        return discountedPrice != null ? discountedPrice : basePrice;
    }

    // 유효성 검사 로직
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("옵션명은 필수입니다.");
        }

        if (name.length() > 200) {
            throw new IllegalArgumentException("옵션명은 200자를 초과할 수 없습니다.");
        }
    }

    private void validatePrice(Integer basePrice, Integer discountedPrice) {
        if (basePrice == null || basePrice < 0) {
            throw new IllegalArgumentException("기본 가격은 0 이상이어야 합니다.");
        }

        if (discountedPrice != null) {
            if (discountedPrice < 0) {
                throw new IllegalArgumentException("할인 가격은 0 이상이어야 합니다.");
            }
            if (discountedPrice > basePrice) {
                throw new IllegalArgumentException("할인 가격은 기본 가격보다 클 수 없습니다.");
            }
        }
    }

    // 연관관계 편의 메서드
    protected void setProduct(Product product) {
        this.product = product;
    }

    public void addOption(ProductOption option) {
        ProductOptionMapping mapping = ProductOptionMapping.builder()
                .productDetail(this)
                .productOption(option)
                .build();
        this.optionMappings.add(mapping);
    }
}