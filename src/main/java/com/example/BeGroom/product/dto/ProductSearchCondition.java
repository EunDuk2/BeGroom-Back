package com.example.BeGroom.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Builder
public record ProductSearchCondition(
    @Schema(description = "검색 키워드(상품명)", example = "감자")
    String keyword,

    @Schema(description = "카테고리 ID 목록", example = "[1, 2, 3]")
    List<Long> categoryIds,

    @Schema(description = "브랜드 ID 목록", example = "[1, 2, 3]")
    List<Long> brandIds,

    @Schema(description = "품절 상품 제외 여부", example = "true")
    Boolean excludeSoldOut,

    @Schema(description = "배송 타입", example = "DAWN")
    List<String> deliveryTypes,

    @Schema(description = "포장 타입", example = "COLD")
    List<String> packagingTypes
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Compact Constructor:
     * 리스트가 null로 들어올 경우를 대비해 빈 리스트로 초기화해주는 방어 로직을 넣을 수 있습니다.
     */
    public ProductSearchCondition {
        if (categoryIds == null) categoryIds = List.of();
        if (brandIds == null) brandIds = List.of();
        if (deliveryTypes == null) deliveryTypes = List.of();
        if (packagingTypes == null) packagingTypes = List.of();
    }

    public ProductSearchCacheKey toCacheKey() {
        return ProductSearchCacheKey.from(this);
    }
}