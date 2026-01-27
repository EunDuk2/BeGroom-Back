package com.example.BeGroom.product.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.product.dto.BrandFilterResponse;
import com.example.BeGroom.product.dto.ProductDetailResponse;
import com.example.BeGroom.product.dto.ProductListResponse;
import com.example.BeGroom.product.dto.ProductSearchCondition;
import com.example.BeGroom.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product API", description = "상품 조회 API")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상세 정보 조회")
    public ResponseEntity<CommonSuccessDto<ProductDetailResponse>> getProductDetail(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Long memberId = (user != null) ? user.getMemberId() : null;
        ProductDetailResponse response = productService.getProductDetail(productId, memberId);

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        response,
                        HttpStatus.OK,
                        "상품 상세 조회 성공"
                )
        );
    }

    @GetMapping("/search")
    @Operation(summary = "상품 검색", description = "키워드 및 필터로 상품 검색")
    public ResponseEntity<CommonSuccessDto<Page<ProductListResponse>>> searchProducts(
            @ModelAttribute ProductSearchCondition condition,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        Long memberId = (user != null) ? user.getMemberId() : null;
        Page<ProductListResponse> products = productService.searchProducts(condition, pageable, memberId);

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        products,
                        HttpStatus.OK,
                        "상품 검색 성공"
                )
        );
    }

    @GetMapping("/search/brands")
    @Operation(summary = "상품 브랜드 목록", description = "카테고리/키워드 검색 시 해당하는 상품들의 브랜드 목록 조회")
    public ResponseEntity<CommonSuccessDto<List<BrandFilterResponse>>> getBrandFilters(@ModelAttribute ProductSearchCondition condition) {
        List<BrandFilterResponse> brands = productService.getBrandFilters(condition);
        return ResponseEntity.ok(
            CommonSuccessDto.of(
                brands,
                HttpStatus.OK,
                "브랜드 필터 조회 성공"
            )
        );
    }
}