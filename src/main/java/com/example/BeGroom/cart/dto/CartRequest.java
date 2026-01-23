package com.example.BeGroom.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CartRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartAddList {

        @Schema(description = "장바구니에 추가할 상품 목록")
        @NotNull(message = "상품 목록은 필수입니다.")
        private List<CartItemAdd> items;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemAdd {

        @Schema(description = "상품 상세 ID", example = "486")
        @NotNull(message = "상품 상세 ID는 필수입니다.")
        private Long productDetailId;

        @Schema(description = "수량", example = "2")
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        private Integer quantity;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemUpdateQuantity {

        @Schema(description = "수량", example = "5")
        @NotNull(message = "수량은 필수입니다")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        private Integer quantity;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemUpdateSelected {

        @Schema(description = "선택 여부", example = "true")
        @NotNull(message = "선택 여부는 필수입니다.")
        private Boolean isSelected;
    }
}
