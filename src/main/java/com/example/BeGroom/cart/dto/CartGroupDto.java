package com.example.BeGroom.cart.dto;

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
public class CartGroupDto {

    @Schema(description = "배송 타입", example = "DAWN")
    private String deliveryType;

    @Schema(description = "배송 타입 이름", example = "구름배송")
    private String deliveryTypeName;

    @Schema(description = "해당 그룹의 상품들")
    private List<CartItemResDto> items;
}
