package com.example.BeGroom.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResDto {

    @Schema(description = "배송 방식별 그룹화된 장바구니 상품 목록")
    private List<CartGroupDto> groupItems;

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


    public static CartResDto of (
            List<CartGroupDto> groupItems,
            Integer totalCount,
            Integer selectedCount,
            Integer totalPrice,
            Integer totalDiscountPrice,
            Integer finalPrice,
            Integer deliveryFee
    ) {
        return CartResDto.builder()
                .groupItems(groupItems)
                .totalCount(totalCount)
                .selectedCount(selectedCount)
                .totalPrice(totalPrice)
                .totalDiscountPrice(totalDiscountPrice)
                .finalPrice(finalPrice)
                .deliveryFee(deliveryFee)
                .build();
    }

    // 가격 계산
    public static CartResDto from(List<CartItemResDto> items) {

        int totalCount = items.size();

        List<CartItemResDto> selectedItems = items.stream()
                .filter(CartItemResDto::getIsSelected)
                .toList();
        int selectedCount = selectedItems.size();

        // 정가 기준 총액
        int totalPrice = selectedItems.stream()
                .mapToInt(item -> (item.getBasePrice() != null ? item.getBasePrice() : 0) * item.getQuantity())
                .sum();

        // 할인 적용된 총액 (배송비 제외)
        int totalProductPrice = selectedItems.stream()
                .mapToInt(item -> {
                    int price = item.getDiscountedPrice() != null ? item.getDiscountedPrice() : item.getBasePrice();
                    return price * item.getQuantity();
                })
                .sum();

        // 배송비 계산
        int dawnProductsTotalPrice = selectedItems.stream()
                .filter(item -> "DAWN".equals(item.getDeliveryType()))
                .mapToInt(item -> {
                    int price = item.getDiscountedPrice() != null ? item.getDiscountedPrice() : item.getBasePrice();
                    return price * item.getQuantity();
                })
                .sum();
        int deliveryFee = (dawnProductsTotalPrice > 0 && dawnProductsTotalPrice < 40000) ? 3000 : 0;
        int finalPrice = totalProductPrice + deliveryFee;
        int totalDiscountPrice = totalPrice - totalProductPrice;

        // 배송방식에 따른 그룹화
        Map<String, List<CartItemResDto>> groupedMap = items.stream()
                .collect(Collectors.groupingBy(item -> {
                    return item.getDeliveryType() != null ? item.getDeliveryType() : "NORMAL_PARCEL";
                }));

        List<CartGroupDto> groups = groupedMap.entrySet().stream()
                .map(entry -> CartGroupDto.builder()
                        .deliveryType(entry.getKey())
                        .deliveryTypeName("DAWN".equals(entry.getKey()) ? "구름배송" : "판매자배송")
                        .items(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(g -> !"DAWN".equals(g.getDeliveryType())))
                .toList();

        return CartResDto.builder()
                .groupItems(groups)
                .totalCount(totalCount)
                .selectedCount(selectedCount)
                .totalPrice(totalPrice)
                .totalDiscountPrice(totalDiscountPrice)
                .finalPrice(finalPrice)
                .deliveryFee(deliveryFee)
                .build();
    }
}
