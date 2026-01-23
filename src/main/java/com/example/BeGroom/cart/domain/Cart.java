package com.example.BeGroom.cart.domain;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.product.domain.ProductDetail;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cart")
@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<CartItem> cartItems = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Cart(Member member) {
        Assert.notNull(member, "회원 정보는 필수입니다.");
        this.member = member;
    }

    public static Cart create(Member member) {
        Cart cart = new Cart(member);
        member.assignCart(cart);
        return cart;
    }

    public void addProduct(ProductDetail productDetail, int quantity) {
        CartItem existingItem = findCartItem(productDetail);

        if (existingItem != null) {
            existingItem.increaseQuantity(quantity);
        } else {
            CartItem.create(this, productDetail, quantity);
        }
    }

    private CartItem findCartItem(ProductDetail productDetail) {
        return this.cartItems.stream()
            .filter(item -> item.getProductDetail().equals(productDetail))
            .findFirst()
            .orElse(null);
    }

    public int calculateTotalPrice() {
        return cartItems.stream()
            .filter(CartItem::getIsSelected)
            .mapToInt(CartItem::calculateItemPrice)
            .sum();
    }

    protected void addCartItem(CartItem cartItem) {
        this.cartItems.add(cartItem);
    }
}
