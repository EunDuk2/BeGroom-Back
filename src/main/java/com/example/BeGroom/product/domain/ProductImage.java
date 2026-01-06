package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_image_id")
    private Long productImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false)
    private ImageType imageType;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 1;


    public enum ImageType {
        MAIN, DETAIL
    }


    // 이미지 수정
    public void updateImageInfo(String imageUrl, ImageType imageType) {
        validateImageUrl(imageUrl);
        if (imageType == null) {
            throw new IllegalArgumentException("이미지 타입은 필수입니다.");
        }
        this.imageUrl = imageUrl;
        this.imageType = imageType;
    }

    // 정렬 순서 변경 (관리자용)
    public void updateSortOrder(Integer sortOrder) {
        if (sortOrder == null || sortOrder < 0) {
            throw new IllegalArgumentException("정렬 순서는 0 이상이어야 합니다.");
        }
        this.sortOrder = sortOrder;
    }

    // 이미지 유효성 검사
    private void validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("이미지 경로는 필수입니다.");
        }
        if (imageUrl.length() > 500) {
            throw new IllegalArgumentException("이미지 경로는 500자를 초과할 수 없습니다.");
        }
    }

    // 연관관계 편의 메서드
    protected void setProduct(Product product) {
        this.product = product;
    }
}