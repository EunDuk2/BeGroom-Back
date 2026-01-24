package com.example.BeGroom.product.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_option_mapping")
@Entity
public class ProductOptionMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id", nullable = false)
    private ProductDetail productDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private ProductOption productOption;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private ProductOptionMapping(ProductDetail productDetail, ProductOption productOption) {
        if (productDetail == null) throw new IllegalArgumentException("상세 상품 정보는 필수입니다.");
        if (productOption == null) throw new IllegalArgumentException("옵션 정보는 필수입니다.");

        this.productDetail = productDetail;
        this.productOption = productOption;

        connect(productDetail);
    }

    private void connect(ProductDetail detail) {
        if (!detail.getOptionMappings().contains(this)) {
            detail.getOptionMappings().add(this);
        }
    }
}