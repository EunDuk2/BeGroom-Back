package com.example.BeGroom.product.dto;

import com.example.BeGroom.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListResDto {

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "상품 번호", example = "5000069")
    private Long productNo;

    @Schema(description = "브랜드", example = "비구름")
    private String brand;

    @Schema(description = "상품명", example = "[바름팜] 친환경 감자 600g")
    private String name;

    @Schema(description = "간단 설명", example = "안심하고 즐기는 파근파근함")
    private String shortDescription;

    @Schema(description = "정가", example = "3990")
    private Integer salesPrice;

    @Schema(description = "판매가", example = "2990")
    private Integer discountedPrice;

    @Schema(description = "할인율", example = "8")
    private Integer discountRate;

    @Schema(description = "메인 이미지 URL")
    private String mainImageUrl;

    @Schema(description = "위시리스트 담긴 수")
    private Integer wishlistCount;

    @Schema(description = "사용자가 찜했는지 여부")
    private Boolean isWishlisted;

    @Schema(description = "품절 여부", example = "true")
    private Boolean isSoldOut;

    @Schema(description = "판매 상태", example = "SALE")
    private String productStatus;

    // Entity -> DTO 변환
    public static ProductListResDto from(Product product, String mainImageUrl, String brandName, Boolean isWishlisted) {
        return ProductListResDto.builder()
                .productId(product.getProductId())
                .productNo(product.getProductNo())
                .brand(brandName)
                .name(product.getName())
                .shortDescription(product.getShortDescription())
                .salesPrice(product.getSalesPrice())
                .discountedPrice(product.getDiscountedPrice())
                .discountRate(product.getDiscountRate())
                .mainImageUrl(mainImageUrl)
                .wishlistCount(product.getWishlistCount())
                .isSoldOut(product.getIsSoldOut())
                .productStatus(product.getProductStatus().name())
                .isWishlisted(isWishlisted)
                .build();
    }
}

