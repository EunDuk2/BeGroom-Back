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
public class CartResDto {

    @Schema(description = "장바구니 상품 목록")
    private List<CartItemResDto> items;

    @Schema(description = "전체 상품 개수", example = "5")
    private Integer totalCount;

    @Schema(description = "선택된 상품 개수", example = "3")
    private Integer selectedCount;

    @Schema(description = "선택된 상품 총 금액", example = "50000")
    private Integer totalPrice;

    @Schema(description = "선택된 상품 총 할인 금액", example = "10000")
    private Integer totalDiscountPrice;

    @Schema(description = "선택된 상품 최종 결제 금액", example = "40000")
    private Integer finalPrice;


    public static CartResDto of (
            List<CartItemResDto> items,
            Integer totalCount,
            Integer selectedCount,
            Integer totalPrice,
            Integer totalDiscountPrice,
            Integer finalPrice
    ) {
        return CartResDto.builder()
                .items(items)
                .totalCount(totalCount)
                .selectedCount(selectedCount)
                .totalPrice(totalPrice)
                .totalDiscountPrice(totalDiscountPrice)
                .finalPrice(finalPrice)
                .build();
    }

    // 가격 계산
    public static CartResDto from(List<CartItemResDto> items) {

        int totalCount = items.size();

        List<CartItemResDto> selectedItems = items.stream()
                .filter(CartItemResDto::getIsSelected)
                .toList();
        int selectedCount = selectedItems.size();

        int totalPrice = selectedItems.stream()
                .mapToInt(item -> (item.getBasePrice() != null ? item.getBasePrice() : 0) * item.getQuantity())
                .sum();

        int finalPrice = selectedItems.stream()
                .mapToInt(item -> {
                    int price = item.getDiscountedPrice() != null ? item.getDiscountedPrice() : item.getBasePrice();
                    return price * item.getQuantity();
                })
                .sum();

        int totalDiscountPrice = totalPrice - finalPrice;

        return CartResDto.builder()
                .items(items)
                .totalCount(totalCount)
                .selectedCount(selectedCount)
                .totalPrice(totalPrice)
                .totalDiscountPrice(totalDiscountPrice)
                .finalPrice(finalPrice)
                .build();
    }
}
