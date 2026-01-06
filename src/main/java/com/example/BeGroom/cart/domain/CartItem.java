package com.example.BeGroom.cart.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.product.domain.ProductDetail;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id", nullable = false)
    private ProductDetail productDetail;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "is_selected", nullable = false)
    @Builder.Default
    private Boolean isSelected = true;


    // 선택 상태 변경
    public void updateSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    // 수량 변경
    public void updateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        if (quantity > this.productDetail.getQuantity()) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.quantity = quantity;
    }

    // 수량 증가
    public void increaseQuantity(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("증가량은 1개 이상이어야 합니다.");
        }
        if (this.quantity + amount > this.productDetail.getQuantity()) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.quantity += amount;
    }

    // 수량 감소
    public void decreaseQuantity(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("감소량은 1개 이상이어야 합니다.");
        }
        if (this.quantity - amount <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        this.quantity -= amount;
    }

    // 연관관계
    protected void setCart(Cart cart) {
        this.cart = cart;
    }
}
