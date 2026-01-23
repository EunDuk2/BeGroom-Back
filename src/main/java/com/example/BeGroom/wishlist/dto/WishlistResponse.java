package com.example.BeGroom.wishlist.dto;

import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.wishlist.domain.Wishlist;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
public class WishlistResponse {

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

    @Builder
    private WishlistResponse(Long wishlistId, Long productId, String productName, String mainImageUrl, Integer salesPrice, Integer discountedPrice, Integer discountRate, LocalDateTime createdAt) {
        this.wishlistId = wishlistId;
        this.productId = productId;
        this.productName = productName;
        this.mainImageUrl = mainImageUrl;
        this.salesPrice = salesPrice;
        this.discountedPrice = discountedPrice;
        this.discountRate = discountRate;
        this.createdAt = createdAt;
    }

    public static WishlistResponse from(Wishlist wishlist) {
        Product product = wishlist.getProduct();

        return WishlistResponse.builder()
                .wishlistId(wishlist.getId())
                .productId(product.getId())
                .productName(product.getName())
                .mainImageUrl(product.getMainImageUrl())
                .salesPrice(product.getSalesPrice())
                .discountedPrice(product.getDiscountedPrice())
                .discountRate(product.getDiscountRate())
                .createdAt(wishlist.getCreatedAt())
                .build();
    }

}
