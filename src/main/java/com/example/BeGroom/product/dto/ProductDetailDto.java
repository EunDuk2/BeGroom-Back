package com.example.BeGroom.product.dto;

import com.example.BeGroom.product.domain.ProductDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
public class ProductDetailDto {

    @Schema(description = "상세상품 ID", example = "1")
    private Long id;

    @Schema(description = "상세상품 번호", example = "10001")
    private Long productNo;

    @Schema(description = "상세상품명", example = "싱그러운 유러피안 샐러드믹스 110g")
    private String name;

    @Schema(description = "원가", example = "6490")
    private Integer originalPrice;

    @Schema(description = "판매가", example = "5490")
    private Integer sellingPrice;

    @Schema(description = "할인율", example = "15")
    private Integer discountRate;

    @Schema(description = "재고", example = "999")
    private Integer stock;

    @Schema(description = "구매 가능 여부", example = "true")
    private Boolean isAvailable;

    @Schema(description = "품절 여부", example = "false")
    private Boolean isSoldOut;

    @Schema(description = "재고 부족 주의 여부", example = "false")
    private Boolean isLowStock;

    @Builder
    private ProductDetailDto(Long id, Long productNo, String name, Integer originalPrice, Integer sellingPrice, Integer discountRate, Integer stock, Boolean isAvailable, Boolean isSoldOut, Boolean isLowStock) {
        this.id = id;
        this.productNo = productNo;
        this.name = name;
        this.originalPrice = originalPrice;
        this.sellingPrice = sellingPrice;
        this.discountRate = discountRate;
        this.stock = stock;
        this.isAvailable = isAvailable;
        this.isSoldOut = isSoldOut;
        this.isLowStock = isLowStock;
    }

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