package com.example.BeGroom.product.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProductUpdateRequest {

    @Schema(description = "상품명", example = "산청 설향 딸기 500g")
    private String name;

    @Schema(description = "상품 간단 설명", example = "탄탄한 과육 속 선명한 과즙")
    private String shortDescription;

    @Schema(description = "상품 정보 (HTML)", example = "<div>상품 상세 내용...</div>")
    private String productInfo;

    @Schema(description = "상품 고시 정보 (JSON)", example = "[{\"notices\": [{\"title\": \"품목 또는 명칭\", \"description\": \"상품설명 및 상품이미지 참조\"}, ...")
    private List<Object> productNotice;

    @Builder
    private ProductUpdateRequest(String name, String shortDescription, String productInfo, List<Object> productNotice) {
        this.name = name;
        this.shortDescription = shortDescription;
        this.productInfo = productInfo;
        this.productNotice = productNotice;
    }
}
