package com.example.BeGroom.product.controller;

import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.product.dto.ProductDetailResDto;
import com.example.BeGroom.product.dto.ProductListResDto;
import com.example.BeGroom.product.dto.ProductSearchCondition;
import com.example.BeGroom.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<CommonSuccessDto<ProductDetailResDto>> getProductDetail(
            @PathVariable Long productId
    ) {
        ProductDetailResDto product = productService.getProductDetail(productId);

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        product,
                        HttpStatus.OK,
                        "상품 상세 조회 성공"
                )
        );
    }

    @GetMapping("/search")
    @Operation(summary = "상품 검색", description = "키워드 및 필터로 상품 검색")
    public ResponseEntity<CommonSuccessDto<Page<ProductListResDto>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> brandIds,
            @RequestParam(required = false) List<String> deliveryTypes,
            @RequestParam(required = false) List<String> packagingTypes,
            @RequestParam(required = false, defaultValue = "false") Boolean excludeSoldOut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "productId") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        ProductSearchCondition condition = ProductSearchCondition.builder()
                .keyword(keyword)
                .categoryIds(categoryIds)
                .brandIds(brandIds)
                .deliveryTypes(deliveryTypes)
                .packagingTypes(packagingTypes)
                .excludeSoldOut(excludeSoldOut)
                .build();

        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<ProductListResDto> products = productService.searchProducts(condition, pageable);

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        products,
                        HttpStatus.OK,
                        "상품 검색 성공"
                )
        );
    }
}