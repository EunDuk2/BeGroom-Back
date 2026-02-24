package com.example.BeGroom.settlement.dto.res;

public record SettlementRefundDto(
        Long settlementId,
        Long paymentId,
        Long paymentAmount
) {
}
