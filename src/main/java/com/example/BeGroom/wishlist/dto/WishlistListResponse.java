package com.example.BeGroom.wishlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
public class WishlistListResponse {

    @Schema(description = "위시리스트 상품 목록")
    private List<WishlistResponse> items;

    @Schema(description = "위시리스트 상품 개수", example = "10")
    private Integer totalCount;

    private WishlistListResponse(List<WishlistResponse> items) {
        this.items = items;
        this.totalCount = items.size();
    }

    public static WishlistListResponse from(List<WishlistResponse> items) {
        return new WishlistListResponse(items);
    }
}
