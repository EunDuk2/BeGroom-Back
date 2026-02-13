package com.example.BeGroom.product.dto;

import com.example.BeGroom.product.domain.ProductDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

@Builder
public record ProductDetailDto(
    @Schema(description = "상세상품 ID", example = "1") Long id,
    @Schema(description = "상세상품 번호", example = "10001") Long productNo,
    @Schema(description = "상세상품명", example = "싱그러운 유러피안 샐러드믹스 110g") String name,
    @Schema(description = "원가", example = "6490") Integer originalPrice,
    @Schema(description = "판매가", example = "5490") Integer sellingPrice,
    @Schema(description = "할인율", example = "15") Integer discountRate,
    @Schema(description = "재고", example = "999") Integer stock,
    @Schema(description = "구매 가능 여부", example = "true") Boolean isAvailable,
    @Schema(description = "품절 여부", example = "false") Boolean isSoldOut,
    @Schema(description = "재고 부족 주의 여부", example = "false") Boolean isLowStock
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static ProductDetailDto from(ProductDetail detail) {
        return ProductDetailDto.builder()
            .id(detail.getId())
            .productNo(detail.getNo())
            .name(detail.getName())
            .originalPrice(detail.getOriginalPrice())
            .sellingPrice(detail.getSellingPrice())
            .discountRate(detail.getDiscountRate())
            .stock(detail.getStock() != null ? detail.getStock().getQuantity() : 0)
            .isAvailable(detail.getIsAvailable())
            .isSoldOut(detail.isSoldOut())
            .isLowStock(detail.isLowStock())
            .build();
    }
}