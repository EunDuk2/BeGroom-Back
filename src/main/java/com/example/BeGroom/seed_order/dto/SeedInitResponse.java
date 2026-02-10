package com.example.BeGroom.seed_order.dto;

public record SeedInitResponse(
        String runId,
        int createdMembers,
        int createdProductDetails,

        // k6에서 바로 저장해서 쓰라고 CSV를 문자열로 내려줌
        // users.csv: memberId,token
        String usersCsv,

        // product-details.csv: productDetailId
        String productDetailsCsv
) {}