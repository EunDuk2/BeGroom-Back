package com.example.BeGroom.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
public class CartGroupResponse {

    @Schema(description = "배송 타입", example = "DAWN")
    private String deliveryType;

    @Schema(description = "배송 타입 이름", example = "구름배송")
    private String deliveryTypeName;

    @Schema(description = "해당 그룹의 상품들")
    private List<CartItemResponse> items;

    @Builder
    private CartGroupResponse(String deliveryType, String deliveryTypeName, List<CartItemResponse> items) {
        this.deliveryType = deliveryType;
        this.deliveryTypeName = deliveryTypeName;
        this.items = items;
    }

    public static CartGroupResponse of(String deliveryType, List<CartItemResponse> items) {
        return CartGroupResponse.builder()
            .deliveryType(deliveryType)
            .deliveryTypeName("DAWN".equals(deliveryType) ? "구름배송" : "판매자배송")
            .items(items)
            .build();
    }
}
