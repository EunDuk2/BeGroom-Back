package com.example.BeGroom.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartAddListReqDto {

    @Schema(description = "장바구니에 추가할 상품 목록")
    @NotNull(message = "상품 목록은 필수입니다.")
    private List<CartItemAddReqDto> items;
}
