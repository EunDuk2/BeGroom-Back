package com.example.BeGroom.wishlist.dto;

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
public class WishlistListResDto {

    @Schema(description = "위시리스트 상품 목록")
    private List<WishlistResDto> items;

    @Schema(description = "위시리스트 상품 개수", example = "10")
    private Integer totalCount;


    public static WishlistListResDto from(List<WishlistResDto> items) {
        return WishlistListResDto.builder()
                .items(items)
                .totalCount(items.size())
                .build();
    }
}
