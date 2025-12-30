package com.example.BeGroom.product.dto.crawling;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "크롤링 결과")
public class CrawlingResultDto {

    @Schema(description = "크롤링된 총 상품 수", example = "200")
    private Integer totalProducts;

    @Schema(description = "크롤링 상태 메시지", example = "완료")
    private String message;
}
