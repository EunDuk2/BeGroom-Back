package com.example.BeGroom.settlement.repository.yearly;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface YearlySettlementRepositoryCustom {
    void upsert(int year, Long sellerId, Long paymentAmount,
                BigDecimal feeRate, BigDecimal settlementAmount, BigDecimal refundAmount,
                LocalDate startDate, LocalDate endDate);

    void updateRefund(BigDecimal returnAmount);
}
