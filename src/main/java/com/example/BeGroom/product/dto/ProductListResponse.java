package com.example.BeGroom.product.dto;

import com.example.BeGroom.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

@Builder
public record ProductListResponse(
    @Schema(description = "상품 ID", example = "1") Long productId,
    @Schema(description = "상품 번호", example = "5000069") Long productNo,
    @Schema(description = "브랜드", example = "비구름") String brand,
    @Schema(description = "상품명", example = "[바름팜] 친환경 감자 600g") String name,
    @Schema(description = "간단 설명", example = "안심하고 즐기는 파근파근함") String shortDescription,
    @Schema(description = "정가", example = "3990") Integer salesPrice,
    @Schema(description = "판매가", example = "2990") Integer discountedPrice,
    @Schema(description = "할인율", example = "8") Integer discountRate,
    @Schema(description = "메인 이미지 URL") String mainImageUrl,
    @Schema(description = "위시리스트 담긴 수") Integer wishlistCount,
    @Schema(description = "사용자가 찜했는지 여부") Boolean isWishlisted,
    @Schema(description = "품절 여부", example = "true") Boolean isSoldOut,
    @Schema(description = "판매 상태", example = "SALE") String productStatus
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static ProductListResponse of(Product product, boolean isWishlisted) {
        return ProductListResponse.builder()
            .productId(product.getId())
            .productNo(product.getNo())
            .brand(product.getBrand().getName())
            .name(product.getName())
            .shortDescription(product.getShortDescription())
            .salesPrice(product.getSalesPrice())
            .discountedPrice(product.getDiscountedPrice())
            .discountRate(product.getDiscountRate())
            .mainImageUrl(product.getMainImageUrl())
            .wishlistCount(product.getWishlistCount())
            .isSoldOut(product.isSoldOut())
            .productStatus(product.getProductStatus().name())
            .isWishlisted(isWishlisted)
            .build();
    }

    public ProductListResponse withWishlist(boolean isWishlisted) {
        return ProductListResponse.builder()
            .productId(this.productId)
            .productNo(this.productNo)
            .brand(this.brand)
            .name(this.name)
            .shortDescription(this.shortDescription)
            .salesPrice(this.salesPrice)
            .discountedPrice(this.discountedPrice)
            .discountRate(this.discountRate)
            .mainImageUrl(this.mainImageUrl)
            .wishlistCount(this.wishlistCount)
            .isSoldOut(this.isSoldOut)
            .productStatus(this.productStatus)
            .isWishlisted(isWishlisted)
            .build();
    }
}

