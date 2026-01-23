package com.example.BeGroom.product.controller;

import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.product.dto.admin.ProductCreateRequest;
import com.example.BeGroom.product.dto.ProductListResponse;
import com.example.BeGroom.product.dto.admin.ProductPriceUpdateRequest;
import com.example.BeGroom.product.dto.admin.ProductUpdateRequest;
import com.example.BeGroom.product.dto.admin.StockUpdateRequest;
import com.example.BeGroom.product.service.AdminProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    private final AdminProductService adminProductService;

    @PostMapping
    @Operation(summary = "신규 상품 등록", description = "새로운 상품 정보와 초기 재고/옵션을 등록합니다.")
    public ResponseEntity<CommonSuccessDto<Long>> createProduct(
        @Valid @RequestBody ProductCreateRequest request
    ) {
        Long productId = adminProductService.createProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                CommonSuccessDto.of(
                    productId,
                    HttpStatus.CREATED,
                    "상품이 성공적으로 등록되었습니다."
            )
        );
    }

    @PatchMapping("/{productId}")
    @Operation(summary = "상품 기본 정보 수정", description = "기존 상품의 명칭, 설명 등을 수정합니다.")
    public ResponseEntity<CommonSuccessDto<Void>> updateProduct (
        @PathVariable Long productId,
        @Valid @RequestBody ProductUpdateRequest request
        ) {
        adminProductService.updateProduct(productId, request);

        return ResponseEntity.ok(
            CommonSuccessDto.of(
                null,
                HttpStatus.OK,
                "상품 정보 수정 완료"
            )
        );
    }

    @PatchMapping("/details/{detailId}/price")
    @Operation(summary = "상세 상품 가격 수정", description = "새로운 가격 이력을 추가하여 가격을 변경")
    public ResponseEntity<CommonSuccessDto<Void>> updateProductPrice(
        @PathVariable Long detailId,
        @Valid @RequestBody ProductPriceUpdateRequest request
    ) {
        adminProductService.updateProductPrice(detailId, request.getOriginalPrice(), request.getSellingPrice());
        return ResponseEntity.ok(
            CommonSuccessDto.of(
                null,
                HttpStatus.OK,
                "가격 정보가 업데이트되었습니다."
            )
        );
    }

    @PatchMapping("/details/{detailId}/stock")
    @Operation(summary = "상품 옵션 재고 조정", description = "관리자가 수동으로 재고 관리(입고/차감)")
    public ResponseEntity<CommonSuccessDto<Void>> updateStock(
        @PathVariable Long detailId,
        @Valid @RequestBody StockUpdateRequest request
    ) {
        adminProductService.updateStock(detailId, request.getQuantityChange());
        return ResponseEntity.ok(
            CommonSuccessDto.of(
                null,
                HttpStatus.OK,
                "재고가 성공적으로 업데이트되었습니다."
            )
        );
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "상품 삭제", description = "상품을 소프트 딜리트 처리")
    public ResponseEntity<CommonSuccessDto<Void>> deleteProduct(
        @PathVariable Long productId
    ) {
        adminProductService.deleteProduct(productId);
        return ResponseEntity.ok(
            CommonSuccessDto.of(
                null,
                HttpStatus.OK,
                "상품 삭제 완료"
            )
        );
    }

    @GetMapping
    @Operation(summary = "전체 상품 목록 조회 (관리자)", description = "모든 상태의 상품 조회 가능, statuses 파라미터로 특정 상태 필터링 가능")
    public ResponseEntity<CommonSuccessDto<Page<ProductListResponse>>> getAllProducts(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<ProductListResponse> products = adminProductService.getAllProductsForAdmin(statuses, pageable);

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        products,
                        HttpStatus.OK,
                        "전체 상품 조회 성공"
                )
        );
    }

    @PatchMapping("/{productId}/status")
    @Operation(summary = "상품 판매 상태 변경", description = "SALE, WAIT, STOP 등 상품의 노출 상태를 변경")
    public ResponseEntity<CommonSuccessDto<Void>> changeProductStatus(
        @PathVariable Long productId,
        @RequestParam String status
    ) {
        adminProductService.changeProductStatus(productId, status);

        return ResponseEntity.ok(
            CommonSuccessDto.of(
                null,
                HttpStatus.OK,
                "상품 상태가 " + status + "(으)로 변경되었습니다."
            )
        );
    }
}