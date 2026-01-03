package com.example.BeGroom.product.dto;

import com.example.BeGroom.product.domain.ProductDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailDto {

    @Schema(description = "상세상품 ID", example = "1")
    private Long productDetailId;

    @Schema(description = "상세상품명", example = "싱그러운 유러피안 샐러드믹스 110g")
    private String name;

    @Schema(description = "원가", example = "6490")
    private Integer basePrice;

    @Schema(description = "판매가", example = "5490")
    private Integer discountedPrice;

    @Schema(description = "재고", example = "999")
    private Integer quantity;

    @Schema(description = "구매 가능 여부", example = "true")
    private Boolean isAvailable;


    public static ProductDetailDto from(ProductDetail productDetail) {
        return ProductDetailDto.builder()
                .productDetailId(productDetail.getProductDetailId())
                .name(productDetail.getName())
                .basePrice(productDetail.getBasePrice())
                .discountedPrice(productDetail.getDiscountedPrice())
                .quantity(productDetail.getQuantity())
                .isAvailable(productDetail.getIsAvailable())
                .build();
    }
}