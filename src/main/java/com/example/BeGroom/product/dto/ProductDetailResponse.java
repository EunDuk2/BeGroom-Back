package com.example.BeGroom.product.dto;

import com.example.BeGroom.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder // Record에서도 빌더 사용 가능
public record ProductDetailResponse(
    @Schema(description = "상품 ID", example = "1") Long productId,
    @Schema(description = "상품 번호", example = "5000069") Long productNo,
    @Schema(description = "브랜드", example = "비구름") String brand,
    @Schema(description = "상품명", example = "[바름팜] 친환경 감자 600g") String name,
    @Schema(description = "위시리스트 담긴 수") Integer wishlistCount,
    @Schema(description = "사용자가 찜했는지 여부") Boolean isWishlisted,
    @Schema(description = "간단 설명", example = "안심하고 즐기는 파근파근함") String shortDescription,
    @Schema(description = "정가", example = "3990") Integer salesPrice,
    @Schema(description = "판매가", example = "2990") Integer discountedPrice,
    @Schema(description = "할인율", example = "8") Integer discountRate,
    @Schema(description = "상품 상세 설명 (HTML)") String productInfo,
    @Schema(description = "상품 고시 정보 (JSON)") List<Object> productNotice,
    @Schema(description = "메인 이미지 URL") String mainImageUrl,
    @Schema(description = "상세 이미지 URL 목록") List<String> detailImageUrls,
    @Schema(description = "품절 여부", example = "true") Boolean isSoldOut,
    @Schema(description = "판매 상태", example = "SALE") String productStatus,
    @Schema(description = "상세 상품 목록") List<ProductDetailDto> details
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static ProductDetailResponse of(Product product, boolean isWishlisted) {
        return ProductDetailResponse.builder()
            .productId(product.getId())
            .productNo(product.getNo())
            .brand(product.getBrand().getName())
            .name(product.getName())
            .wishlistCount(product.getWishlistCount())
            .isWishlisted(isWishlisted)
            .shortDescription(product.getShortDescription())
            .salesPrice(product.getSalesPrice())
            .discountedPrice(product.getDiscountedPrice())
            .discountRate(product.getDiscountRate())
            .productInfo(product.getProductInfo())
            .productNotice(product.getProductNotice())
            .mainImageUrl(product.getMainImageUrl())
            .detailImageUrls(product.getDetailImageUrls())
            .isSoldOut(product.isSoldOut())
            .productStatus(product.getProductStatus().name())
            .details(product.getProductDetails().stream()
                .map(ProductDetailDto::from)
                .toList())
            .build();
    }

    // Record의 핵심: 데이터 수정을 원할 때 새로운 객체를 생성하는 불변성 유지
    public ProductDetailResponse withWishlist(boolean isWishlisted) {
        return ProductDetailResponse.builder()
            .productId(this.productId)
            .productNo(this.productNo)
            .brand(this.brand)
            .name(this.name)
            .wishlistCount(this.wishlistCount)
            .isWishlisted(isWishlisted)
            .shortDescription(this.shortDescription)
            .salesPrice(this.salesPrice)
            .discountedPrice(this.discountedPrice)
            .discountRate(this.discountRate)
            .productInfo(this.productInfo)
            .productNotice(this.productNotice)
            .mainImageUrl(this.mainImageUrl)
            .detailImageUrls(this.detailImageUrls)
            .isSoldOut(this.isSoldOut)
            .productStatus(this.productStatus)
            .details(this.details)
            .build();
    }
}