package com.example.BeGroom.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class CartResponse {

    @Schema(description = "배송 방식별 그룹화된 장바구니 상품 목록")
    private List<CartGroupResponse> groupItems;

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

    @Schema(description = "배송비", example = "3000")
    private Integer deliveryFee;

    @Builder
    private CartResponse(List<CartGroupResponse> groupItems, Integer totalCount, Integer selectedCount, Integer totalPrice, Integer totalDiscountPrice, Integer finalPrice, Integer deliveryFee) {
        this.groupItems = groupItems;
        this.totalCount = totalCount;
        this.selectedCount = selectedCount;
        this.totalPrice = totalPrice;
        this.totalDiscountPrice = totalDiscountPrice;
        this.finalPrice = finalPrice;
        this.deliveryFee = deliveryFee;
    }

    public static CartResponse from(List<CartItemResponse> items) {
        // 1. 선택된 아이템 필터링
        List<CartItemResponse> selectedItems = items.stream()
            .filter(CartItemResponse::getIsSelected)
            .toList();

        // 2. 가격 계산 로직
        int totalPrice = selectedItems.stream()
            .mapToInt(item -> item.getOriginalPrice() * item.getQuantity())
            .sum();

        int totalProductPrice = selectedItems.stream()
            .mapToInt(item -> item.getDiscountedPrice() * item.getQuantity())
            .sum();

        // 3. 배송비 계산 (구름배송 기준)
        int dawnProductsPrice = selectedItems.stream()
            .filter(item -> "DAWN".equals(item.getDeliveryType()))
            .mapToInt(item -> item.getDiscountedPrice() * item.getQuantity())
            .sum();

        int deliveryFee = (dawnProductsPrice > 0 && dawnProductsPrice < 40000) ? 3000 : 0;
        int finalPrice = totalProductPrice + deliveryFee;
        int totalDiscountPrice = totalPrice - totalProductPrice;

        // 4. 배송 방식 그룹화
        Map<String, List<CartItemResponse>> groupedMap = items.stream()
            .collect(Collectors.groupingBy(item ->
                item.getDeliveryType() != null ? item.getDeliveryType() : "NORMAL_PARCEL"
            ));

        List<CartGroupResponse> groupItems = groupedMap.entrySet().stream()
            .map(entry -> CartGroupResponse.of(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(g -> !"DAWN".equals(g.getDeliveryType())))
            .toList();

        return CartResponse.builder()
            .groupItems(groupItems)
            .totalCount(items.size())
            .selectedCount(selectedItems.size())
            .totalPrice(totalPrice)
            .totalDiscountPrice(totalDiscountPrice)
            .finalPrice(finalPrice)
            .deliveryFee(deliveryFee)
            .build();
    }
}
