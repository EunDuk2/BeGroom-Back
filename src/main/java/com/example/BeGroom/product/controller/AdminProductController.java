package com.example.BeGroom.product.controller;

import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.product.dto.ProductListResDto;
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
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin Product API", description = "관리자 상품 관리 API")
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "전체 상품 목록 조회 (관리자)", description = "모든 상태의 상품 조회 가능, statuses 파라미터로 특정 상태 필터링 가능")
    public ResponseEntity<CommonSuccessDto<Page<ProductListResDto>>> getAllProducts(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "productId") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<ProductListResDto> products = productService.getAllProductsForAdmin(statuses, pageable);

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        products,
                        HttpStatus.OK,
                        "전체 상품 조회 성공"
                )
        );
    }
}