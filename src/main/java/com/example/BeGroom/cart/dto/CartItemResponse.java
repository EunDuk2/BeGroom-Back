package com.example.BeGroom.cart.dto;

import com.example.BeGroom.cart.domain.CartItem;
import com.example.BeGroom.product.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
public class CartItemResponse {

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
    private Integer originalPrice;

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

    @Schema(description = "배송 방식", example = "DAWN")
    private String deliveryType;

    @Builder
    private CartItemResponse(Long cartItemId, Long productDetailId, String productName, String mainImageUrl, String productDetailName, Integer originalPrice, Integer discountedPrice, Integer quantity, Boolean isSelected, Boolean isSoldOut, Integer stockQuantity, String deliveryType) {
        this.cartItemId = cartItemId;
        this.productDetailId = productDetailId;
        this.productName = productName;
        this.mainImageUrl = mainImageUrl;
        this.productDetailName = productDetailName;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.quantity = quantity;
        this.isSelected = isSelected;
        this.isSoldOut = isSoldOut;
        this.stockQuantity = stockQuantity;
        this.deliveryType = deliveryType;
    }

    public static CartItemResponse from(CartItem cartItem) {
        ProductDetail detail = cartItem.getProductDetail();
        Product product = detail.getProduct();

        String imageUrl = product.getProductImages().stream()
            .filter(img -> img.getImageType() == ImageType.MAIN)
            .findFirst()
            .map(ProductImage::getImageUrl)
            .orElse(null);

        String deliveryType = detail.getOptionMappings().stream()
            .map(mapping -> mapping.getProductOption())
            .filter(opt -> "delivery".equals(opt.getOptionType()))
            .map(ProductOption::getOptionValue)
            .findFirst()
            .orElse("NORMAL_PARCEL");

        return CartItemResponse.builder()
                .cartItemId(cartItem.getId())
                .productDetailId(detail.getId())
                .productName(product.getName())
                .mainImageUrl(imageUrl)
                .productDetailName(detail.getName())
                .originalPrice(detail.getOriginalPrice())
                .discountedPrice(detail.getSellingPrice())
                .quantity(cartItem.getQuantity())
                .isSelected(cartItem.getIsSelected())
                .isSoldOut(!detail.getIsAvailable())
                .stockQuantity(detail.getStock().getQuantity())
                .deliveryType(deliveryType)
                .build();
    }
}
