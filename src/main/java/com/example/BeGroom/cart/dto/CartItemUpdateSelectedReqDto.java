package com.example.BeGroom.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemUpdateSelectedReqDto {

    @Schema(description = "선택 여부", example = "true")
    @NotNull(message = "선택 여부는 필수입니다.")
    private Boolean isSelected;
}
