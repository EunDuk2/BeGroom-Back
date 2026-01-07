package com.example.BeGroom.product.dto;

import com.example.BeGroom.product.domain.Product;
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
public class ProductDetailResDto {

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

    @Schema(description = "소비기한", example = "농산물로 별도의 소비기한은 없으나 가급적 빨리 섭취를 권장합니다.")
    private String expirationDate;

    @Schema(description = "안내사항(JSON 배열)", example = "[\"신선식품의 특성상 상품의 3% 내외의 중량에 차이가 발생할 수 있습니다.\"]")
    private String guides;

    @Schema(description = "상품 상세 설명 (HTML)", example = "<div>상품 상세 내용...</div>")
    private String productDetail;

    @Schema(description = "상품 고시 정보 (JSON)", example = "[{\"notices\": [{\"title\": \"품목 또는 명칭\", \"description\": \"상품설명 및 상품이미지 참조\"}, ...")
    private String productNotice;

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


    public static ProductDetailResDto from(
            Product product,
            String mainImageUrl,
            List<String> detailImageUrls,
            List<ProductDetailDto> details,
            String brandName,
            Integer wishlistCount,
            Boolean isWishlisted
    ) {
        return ProductDetailResDto.builder()
                .productId(product.getProductId())
                .productNo(product.getProductNo())
                .brand(brandName)
                .name(product.getName())
                .wishlistCount(wishlistCount)
                .isWishlisted(isWishlisted)
                .shortDescription(product.getShortDescription())
                .salesPrice(product.getSalesPrice())
                .discountedPrice(product.getDiscountedPrice())
                .discountRate(product.getDiscountRate())
                .isSoldOut(product.getIsSoldOut())
                .productStatus(product.getProductStatus().name())
                .expirationDate(product.getExpirationDate())
                .guides(product.getGuides())
                .productDetail(product.getProductDetail())
                .productNotice(product.getProductNotice())
                .mainImageUrl(mainImageUrl)
                .detailImageUrls(detailImageUrls)
                .details(details)
                .build();
    }
}