package com.example.BeGroom.product.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockUpdateRequest {

    @Schema(description = "재고 변경값", example = "-10")
    private Integer quantityChange;
}
