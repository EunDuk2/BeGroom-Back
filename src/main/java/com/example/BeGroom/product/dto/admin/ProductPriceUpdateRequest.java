package com.example.BeGroom.product.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductPriceUpdateRequest {

    @Schema(description = "원가", example = "7990")
    private Integer originalPrice;

    @Schema(description = "할인가", example = "5990")
    private Integer sellingPrice;
}
