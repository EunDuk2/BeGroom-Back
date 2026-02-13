package com.example.BeGroom.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
public record BrandFilterResponse(
    @Schema(description = "브랜드 ID", example = "1")
    Long brandId,

    @Schema(description = "브랜드명", example = "비구름")
    String brandName,

    @Schema(description = "카테고리/검색 키워드에 해당하는 브랜드의 상품 수", example = "5")
    Long productCount
) implements Serializable {

    private static final long serialVersionUID = 1L;

    // 정적 팩토리 메서드 유지 (기존 코드와의 호환성)
    public static BrandFilterResponse of(Long brandId, String brandName, Long productCount) {
        return new BrandFilterResponse(brandId, brandName, productCount);
    }
}
