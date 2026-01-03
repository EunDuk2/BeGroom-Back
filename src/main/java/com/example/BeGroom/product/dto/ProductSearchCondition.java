package com.example.BeGroom.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchCondition {

    @Schema(description = "검색 키워드(상품명)", example = "감자")
    private String keyword;

    @Schema(description = "카테고리 ID 목록", example = "[1, 2, 3]")
    private List<Long> categoryIds;

    @Schema(description = "브랜드 ID 목록", example = "[1, 2, 3]")
    private List<Long> brandIds;

    @Schema(description = "품절 상품 제외 여부", example = "true")
    private Boolean excludeSoldOut;

    @Schema(description = "배송 타입", example = "DAWN")
    private List<String> deliveryTypes;

    @Schema(description = "포장 타입", example = "COLD")
    private List<String> packagingTypes;

}