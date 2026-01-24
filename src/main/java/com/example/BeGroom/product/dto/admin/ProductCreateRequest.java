package com.example.BeGroom.product.dto.admin;

import com.example.BeGroom.product.domain.Brand;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProductCreateRequest {

    @Schema(description = "브랜드 ID", example = "1")
    private Long brandId;

    @Schema(description = "상품 번호", example = "1000568239")
    private Long productNo;

    @Schema(description = "상품명", example = "[오봉집] 낙지볶음 2종 (냉동)")
    private String name;

    @Schema(description = "상품 간단 설명", example = "매장의 맛을 그대로 구현한")
    private String shortDescription;

    @Schema(description = "상품 정보 (HTML)", example = "<div>상품 상세 내용...</div>")
    private String productInfo;

    @Schema(description = "상품 고시 정보 (JSON)", example = "[{\"notices\": [{\"title\": \"품목 또는 명칭\", \"description\": \"상품설명 및 상품이미지 참조\"}]}]")
    private List<Object> productNotice;

    @Schema(description = "판매 상태", example = "SALE")
    private ProductStatus status = ProductStatus.WAIT;

    @Schema(description = "상세 상품 리스트")
    private List<ProductDetailRequest> details;

    @Getter
    @NoArgsConstructor
    public static class ProductDetailRequest {
        @Schema(description = "옵션명", example = "[오봉집] 낙지볶음 300g (냉동)")
        private String name;

        @Schema(description = "옵션 상품 번호", example = "1000568238")
        private Long productNo;

        @Schema(description = "정가(base_price)", example = "7990")
        private Integer originalPrice;

        @Schema(description = "판매가(discounted_price)", example = "6990")
        private Integer sellingPrice;

        @Schema(description = "초기 재고", example = "100")
        private Integer stock;
    }

    @Builder
    private ProductCreateRequest(Long brandId, Long productNo, String name, String shortDescription, String productInfo, List<Object> productNotice, ProductStatus status, List<ProductDetailRequest> details) {
        this.brandId = brandId;
        this.productNo = productNo;
        this.name = name;
        this.shortDescription = shortDescription;
        this.productInfo = productInfo;
        this.productNotice = productNotice;
        this.status = status;
        this.details = details;
    }

    public Product toEntity(Brand brand) {

        return Product.builder()
            .brand(brand)
            .no(this.productNo)
            .name(this.name)
            .shortDescription(this.shortDescription)
            .productInfo(this.productInfo)
            .productNotice(this.productNotice)
            .productStatus(this.status)
            .build();
    }
}
