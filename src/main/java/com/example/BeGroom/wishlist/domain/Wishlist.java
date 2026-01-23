package com.example.BeGroom.wishlist.domain;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist",
        uniqueConstraints = {
            @UniqueConstraint(
                name = "uk_wishlist_member_product",
                columnNames = {"member_id", "product_id"}
            )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Wishlist(Member member, Product product) {
        Assert.notNull(member, "회원 정보는 필수입니다.");
        Assert.notNull(product, "상품 정보는 필수입니다.");

        this.member = member;
        this.product = product;
    }

    // 생성 메서드
    public static Wishlist create(Member member, Product product) {
        return Wishlist.builder()
                .member(member)
                .product(product)
                .build();
    }
    // 회원 소유 여부 확인
    public boolean isOwnedBy(Long memberId) {
        return this.member.getId().equals(memberId);
    }
}