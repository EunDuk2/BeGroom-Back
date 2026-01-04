package com.example.BeGroom.cart.dto;

import com.example.BeGroom.cart.domain.CartItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResDto {

    @Schema(description = "장바구니 상품 ID", example = "1")
    private Long cartItemId;

    @Schema(description = "상품 상세 ID", example = "486")
    private Long productDetailId;

    @Schema(description = "상품명", example = "남해 보물초 시금치 2종")
    private String productName;

    @Schema(description = "상품 메인 이미지 URL")
    private String mainImageUrl;

    @Schema(description = "상세 상품명", example = "남해 보물초 시금치 250g")
    private String productDetailName;

    @Schema(description = "정가", example = "3990")
    private Integer basePrice;

    @Schema(description = "판매가(할인가)", example = "2990")
    private Integer discountedPrice;

    @Schema(description = "수량", example = "2")
    private Integer quantity;

    @Schema(description = "선택 여부", example = "true")
    private Boolean isSelected;

    @Schema(description = "품절 여부", example = "false")
    private Boolean isSoldOut;

    @Schema(description = "재고 수량", example = "123")
    private Integer stockQuantity;


    // Entity -> DTO (기본 정보)
    public static CartItemResDto from(CartItem cartItem) {
        return CartItemResDto.builder()
                .cartItemId(cartItem.getCartItemId())
                .productDetailId(cartItem.getProductDetailId())
                .quantity(cartItem.getQuantity())
                .isSelected(cartItem.getIsSelected())
                .build();
    }

    // Entity + 상품 정보 -> DTO (전체 정보)
    public static CartItemResDto of(
            CartItem cartItem,
            String productName,
            String mainImageUrl,
            String productDetailName,
            Integer basePrice,
            Integer discountedPrice,
            Boolean isSoldOut,
            Integer stockQuantity
    ) {
        return CartItemResDto.builder()
                .cartItemId(cartItem.getCartItemId())
                .productDetailId(cartItem.getProductDetailId())
                .productName(productName)
                .mainImageUrl(mainImageUrl)
                .productDetailName(productDetailName)
                .basePrice(basePrice)
                .discountedPrice(discountedPrice)
                .quantity(cartItem.getQuantity())
                .isSelected(cartItem.getIsSelected())
                .isSoldOut(isSoldOut)
                .stockQuantity(stockQuantity)
                .build();
    }
}
