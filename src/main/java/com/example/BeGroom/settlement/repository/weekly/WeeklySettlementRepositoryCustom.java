package com.example.BeGroom.settlement.repository.weekly;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface WeeklySettlementRepositoryCustom {
    void upsert(int year, int month, int week, Long sellerId, Long paymentAmount,
                BigDecimal feeRate, BigDecimal settlementAmount, BigDecimal refundAmount,
                LocalDate startDate, LocalDate endDate);

    void updateRefund(BigDecimal refundAmount);
}
