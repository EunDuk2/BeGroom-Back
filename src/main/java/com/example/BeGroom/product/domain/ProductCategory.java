package com.example.BeGroom.product.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_category")
@Entity
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private Boolean isPrimary;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private ProductCategory(Product product, Category category, Boolean isPrimary) {
        if (product == null) throw new IllegalArgumentException("상품 정보는 필수입니다.");
        if (category == null) throw new IllegalArgumentException("카테고리 정보는 필수입니다.");

        this.product = product;
        this.category = category;
        this.isPrimary = (isPrimary != null) ? isPrimary : false;

        connect();
    }

    private void connect() {
        if (!product.getProductCategories().contains(this)) {
            product.getProductCategories().add(this);
        }
        if (!category.getProductCategories().contains(this)) {
            category.getProductCategories().add(this);
        }
    }

    public void updatePrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}