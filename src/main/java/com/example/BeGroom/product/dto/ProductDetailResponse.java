package com.example.BeGroom.product.dto;

import com.example.BeGroom.product.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
public class ProductDetailResponse {

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "상품 번호", example = "5000069")
    private Long productNo;

    @Schema(description = "브랜드", example = "비구름")
    private String brand;

    @Schema(description = "상품명", example = "[바름팜] 친환경 감자 600g")
    private String name;

    @Schema(description = "위시리스트 담긴 수")
    private Integer wishlistCount;

    @Schema(description = "사용자가 찜했는지 여부")
    private Boolean isWishlisted;

    @Schema(description = "간단 설명", example = "안심하고 즐기는 파근파근함")
    private String shortDescription;

    @Schema(description = "정가", example = "3990")
    private Integer salesPrice;

    @Schema(description = "판매가", example = "2990")
    private Integer discountedPrice;

    @Schema(description = "할인율", example = "8")
    private Integer discountRate;

    @Schema(description = "상품 상세 설명 (HTML)", example = "<div>상품 상세 내용...</div>")
    private String productInfo;

    @Schema(description = "상품 고시 정보 (JSON)", example = "[{\"notices\": [{\"title\": \"품목 또는 명칭\", \"description\": \"상품설명 및 상품이미지 참조\"}, ...")
    private List<Object> productNotice;

    @Schema(description = "메인 이미지 URL")
    private String mainImageUrl;

    @Schema(description = "상세 이미지 URL 목록")
    private List<String> detailImageUrls;

    @Schema(description = "품절 여부", example = "true")
    private Boolean isSoldOut;

    @Schema(description = "판매 상태", example = "SALE")
    private String productStatus;

    @Schema(description = "상세 상품 목록")
    private List<ProductDetailDto> details;

    @Builder
    private ProductDetailResponse(Long productId, Long productNo, String brand, String name, Integer wishlistCount, Boolean isWishlisted, String shortDescription, Integer salesPrice, Integer discountedPrice, Integer discountRate, String productInfo, List<Object> productNotice, String mainImageUrl, List<String> detailImageUrls, Boolean isSoldOut, String productStatus, List<ProductDetailDto> details) {
        this.productId = productId;
        this.productNo = productNo;
        this.brand = brand;
        this.name = name;
        this.wishlistCount = wishlistCount;
        this.isWishlisted = isWishlisted;
        this.shortDescription = shortDescription;
        this.salesPrice = salesPrice;
        this.discountedPrice = discountedPrice;
        this.discountRate = discountRate;
        this.productInfo = productInfo;
        this.productNotice = productNotice;
        this.mainImageUrl = mainImageUrl;
        this.detailImageUrls = detailImageUrls;
        this.isSoldOut = isSoldOut;
        this.productStatus = productStatus;
        this.details = details;
    }

    public static ProductDetailResponse of(Product product, boolean isWishlisted) {
        return ProductDetailResponse.builder()
            .productId(product.getId())
            .productNo(product.getNo())
            .brand(product.getBrand().getName())
            .name(product.getName())
            .wishlistCount(product.getWishlistCount())
            .isWishlisted(isWishlisted)
            .shortDescription(product.getShortDescription())
            .salesPrice(product.getSalesPrice())
            .discountedPrice(product.getDiscountedPrice())
            .discountRate(product.getDiscountRate())
            .productInfo(product.getProductInfo())
            .productNotice(product.getProductNotice())
            .mainImageUrl(product.getMainImageUrl())
            .detailImageUrls(product.getDetailImageUrls())
            .isSoldOut(product.isSoldOut())
            .productStatus(product.getProductStatus().name())
            .details(product.getProductDetails().stream()
                .map(ProductDetailDto::from)
                .toList())
            .build();
    }
}