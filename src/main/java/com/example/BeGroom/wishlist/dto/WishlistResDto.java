package com.example.BeGroom.wishlist.dto;

import com.example.BeGroom.wishlist.domain.Wishlist;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistResDto {

    @Schema(description = "위시리스트 ID", example = "1")
    private Long wishlistId;

    @Schema(description = "상품 ID", example = "486")
    private Long productId;

    @Schema(description = "상품명", example = "남해 보물초 시금치 2종")
    private String productName;

    @Schema(description = "상품 메인 이미지 URL")
    private String mainImageUrl;

    @Schema(description = "정가", example = "3990")
    private Integer salesPrice;

    @Schema(description = "판매가(할인가)", example = "2990")
    private Integer discountedPrice;

    @Schema(description = "할인율", example = "30")
    private Integer discountRate;

    @Schema(description = "찜한 날짜")
    private LocalDateTime createdAt;


    public static WishlistResDto of(
            Wishlist wishlist,
            String productName,
            String mainImageUrl,
            Integer salesPrice,
            Integer discountedPrice,
            Integer discountRate
    ) {
        return WishlistResDto.builder()
                .wishlistId(wishlist.getWishlistId())
                .productId(wishlist.getProduct().getProductId())
                .productName(productName)
                .mainImageUrl(mainImageUrl)
                .salesPrice(salesPrice)
                .discountedPrice(discountedPrice)
                .discountRate(discountRate)
                .createdAt(wishlist.getCreatedAt())
                .build();
    }

    // 상품 정보를 찾지 못했을 경우의 기본 응답
    public static WishlistResDto from(Wishlist wishlist) {
        return WishlistResDto.builder()
                .wishlistId(wishlist.getWishlistId())
                .productId(wishlist.getProduct().getProductId())
                .productName("삭제된 상품입니다.")
                .createdAt(wishlist.getCreatedAt())
                .build();
    }
}
