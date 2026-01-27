package com.example.BeGroom.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BrandFilterResponse {

    @Schema(description = "브랜드 ID", example = "1")
    private Long brandId;

    @Schema(description = "브랜드명", example = "비구름")
    private String brandName;

    @Schema(description = "카테고리/검색 키워드에 해당하는 브랜드의 상품 수", example = "5")
    private Long productCount;

    public BrandFilterResponse(Long brandId, String brandName, Long productCount) {
        this.brandId = brandId;
        this.brandName = brandName;
        this.productCount = productCount;
    }

    public static BrandFilterResponse of(Long brandId, String brandName, Long productCount) {
        return new BrandFilterResponse(brandId, brandName, productCount);
    }
}
