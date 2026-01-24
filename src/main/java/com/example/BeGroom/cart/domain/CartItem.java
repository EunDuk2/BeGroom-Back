package com.example.BeGroom.cart.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.product.domain.ProductDetail;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cart_item")
@Entity
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id", nullable = false)
    private ProductDetail productDetail;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private Boolean isSelected;

    @Builder
    private CartItem(Cart cart, ProductDetail productDetail, Integer quantity, Boolean isSelected) {
        this.cart = cart;
        this.productDetail = productDetail;
        this.quantity = (quantity != null) ? quantity : 1;
        this.isSelected = (isSelected != null) ? isSelected : true;

        validateStock(this.quantity);
    }

    public static CartItem create(Cart cart, ProductDetail productDetail, int quantity) {
        CartItem cartItem = CartItem.builder()
            .cart(cart)
            .productDetail(productDetail)
            .quantity(quantity)
            .isSelected(true)
            .build();

        cart.addCartItem(cartItem);
        return cartItem;
    }

    // 선택 상태 변경
    public void updateSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    // 수량 변경 시 재고 검증 로직 포함
    public void updateQuantity(int quantity) {
        validateStock(quantity);
        this.quantity = quantity;
    }

    public void increaseQuantity(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("증가량은 1개 이상이어야 합니다.");
        updateQuantity(this.quantity + amount);
    }

    public void decreaseQuantity(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("감소량은 1개 이상이어야 합니다.");
        updateQuantity(this.quantity - amount);
    }

    public int calculateItemPrice() {
        return this.productDetail.getSellingPrice() * this.quantity;
    }

    private void validateStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }

        this.productDetail.validateOrderable(quantity);
    }

    // 연관관계
    protected void setCart(Cart cart) {
        this.cart = cart;
    }
}
