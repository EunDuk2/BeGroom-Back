package com.example.BeGroom.product.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_option_mapping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductOptionMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long mappingId;

    // 연관 관계 반영: ProductDetail과 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id", nullable = false)
    private ProductDetail productDetail;

    // 연관 관계 반영: ProductOption과 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private ProductOption productOption;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    public static ProductOptionMapping create(ProductDetail detail, ProductOption option) {
        return ProductOptionMapping.builder()
                .productDetail(detail)
                .productOption(option)
                .build();
    }

    // 연관관계 편의 메서드
    protected void setProductDetail(ProductDetail productDetail) {
        this.productDetail = productDetail;
    }

    protected void setProductOption(ProductOption productOption) {
        this.productOption = productOption;
    }
}