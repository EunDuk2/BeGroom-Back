package com.example.BeGroom.seed_order.dto;

import java.util.List;

public record SeedRunResponse(
        String runId,
        Long memberId,
        String token,
        List<Long> orderIds,
        List<Long> productDetailIds
) {}

