package com.example.BeGroom.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductReqDto {
    @NotNull
    @Schema(example = "1")
    private Long productId;

    @NotNull
    @Schema(example = "3")
    private Integer orderQuantity;
}
