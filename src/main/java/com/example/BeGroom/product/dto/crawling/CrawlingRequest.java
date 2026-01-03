package com.example.BeGroom.product.dto.crawling;

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
public class CrawlingRequest {

    @Schema(description = "크롤링할 카테고리 ID 목록(null이면 전체 중분류 크롤링)", example = "[1, 2, 3]")
    private List<Long> categoryIds;

    @Schema(description = "카테고리당 최대 크롤링 상품 개수", example = "200", defaultValue = "200")
    @Builder.Default
    private Integer maxProductsPerCategory = 200;

}