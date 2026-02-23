//package com.example.BeGroom.search.controller;
//
//import com.example.BeGroom.common.response.CommonSuccessDto;
//import com.example.BeGroom.search.service.ProductSyncService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/admin/products/sync")
//@RequiredArgsConstructor
//@Tag(name = "Product Sync API", description = "상품 검색 인덱스 동기화 API (관리자용)")
//public class ProductSyncController {
//
//    private final ProductSyncService syncService;
//
//    @PostMapping("/index-all")
//    @Operation(summary = "전체 상품 색인", description = "MySQL의 모든 상품을 ElasticSearch에 색인")
//    public ResponseEntity<CommonSuccessDto<String>> indexAllProducts() {
//        syncService.indexAllProducts();
//        return ResponseEntity.ok(
//            CommonSuccessDto.of(
//                "전체 상품 색인 완료",
//                HttpStatus.OK,
//                "전체 상품 색인 성공"
//            )
//        );
//    }
//
//    @PostMapping("/reindex-all")
//    @Operation(summary = "전체 상품 재색인", description = "기존 인덱스 삭제 후 전체 재색인")
//    public ResponseEntity<CommonSuccessDto<String>> reindexAllProducts() {
//        syncService.reindexAllProducts();
//        return ResponseEntity.ok(
//            CommonSuccessDto.of(
//                "전체 상품 재색인 완료",
//                HttpStatus.OK,
//                "전체 상품 재색인 성공"
//            )
//        );
//    }
//
//
//    @PostMapping("/{productId}")
//    @Operation(summary = "단일 상품 색인", description = "특정 상품을 ElasticSearch에 색인")
//    public ResponseEntity<CommonSuccessDto<String>> indexProduct(@PathVariable Long productId) {
//        syncService.indexProduct(productId);
//        return ResponseEntity.ok(
//            CommonSuccessDto.of(
//                "상품 색인 완료: " + productId,
//                HttpStatus.OK,
//                "상품 색인 성공"
//            )
//        );
//    }
//
//    @DeleteMapping("/{productId}")
//    @Operation(summary = "단일 상품 삭제", description = "ElasticSearch에서 상품 삭제")
//    public ResponseEntity<CommonSuccessDto<String>> deleteProduct(@PathVariable Long productId) {
//        syncService.deleteProduct(productId);
//        return ResponseEntity.ok(
//            CommonSuccessDto.of(
//                "상품 삭제 완료: " + productId,
//                HttpStatus.OK,
//                "상품 삭제 성공"
//            )
//        );
//    }
//
//    @DeleteMapping("/delete-all")
//    @Operation(summary = "전체 인덱스 삭제", description = "ElasticSearch의 모든 상품 삭제")
//    public ResponseEntity<CommonSuccessDto<String>> deleteAllProducts() {
//        syncService.deleteAllProducts();
//        return ResponseEntity.ok(
//            CommonSuccessDto.of(
//                "전체 인덱스 삭제 완료",
//                HttpStatus.OK,
//                "전체 인덱스 삭제 성공"
//            )
//        );
//    }
//}
