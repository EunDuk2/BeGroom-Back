package com.example.BeGroom.seed_order.dto;

public record SeedInitRequest(
        String runId,

        int memberCount,
        long walletBalance,

        int productDetailCount,     // 최종 옵션 개수 (예: 50_000)
        int detailsPerProduct,      // Product 1개당 옵션 개수(예: 10, 0이면 기본 10)

        int initialStock,           // 옵션당 초기 재고
        int priceMin,               // 랜덤 가격 최소
        int priceMax,               // 랜덤 가격 최대

        boolean reuseSellerBrand     // true면 기본 seller/brand 재사용(권장)
) {}
