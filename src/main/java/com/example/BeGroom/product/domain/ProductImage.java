package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_image")
@Entity
public class ProductImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType imageType;

    @Column(nullable = false)
    private Integer sortOrder;

    @Builder
    private ProductImage(Product product, String imageUrl, ImageType imageType, Integer sortOrder) {
        Assert.notNull(product, "상품 정보는 필수입니다.");
        validateImageUrl(imageUrl);
        Assert.notNull(imageType, "이미지 타입은 필수입니다.");

        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.sortOrder = (sortOrder != null) ? sortOrder : 1;

        setProduct(product);
    }

    // 이미지 수정
    public void updateImageInfo(String imageUrl, ImageType imageType) {
        validateImageUrl(imageUrl);
        Assert.notNull(imageType, "이미지 타입은 필수입니다.");

        this.imageUrl = imageUrl;
        this.imageType = imageType;
    }

    // 정렬 순서 변경 (관리자용)
    public void updateSortOrder(Integer sortOrder) {
        Assert.isTrue(sortOrder != null && sortOrder >= 0, "정렬 순서는 0 이상이어야 합니다.");
        this.sortOrder = sortOrder;
    }

    // 연관관계 편의 메서드
    private void setProduct(Product product) {
        if (this.product != null) {
            this.product.getProductImages().remove(this);
        }

        this.product = product;

        if (!product.getProductImages().contains(this)) {
            product.getProductImages().add(this);
        }
    }

    // 이미지 유효성 검사
    private void validateImageUrl(String imageUrl) {
        Assert.hasText(imageUrl, "이미지 경로는 필수입니다.");
        if (imageUrl.length() > 500) {
            throw new IllegalArgumentException("이미지 경로는 500자를 초과할 수 없습니다.");
        }
    }
}