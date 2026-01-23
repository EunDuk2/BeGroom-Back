package com.example.BeGroom.wishlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
public class WishlistToggleRequest {

    @Schema(description = "상품 ID", example = "486")
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;

    public WishlistToggleRequest(Long productId) {
        this.productId = productId;
    }
}
