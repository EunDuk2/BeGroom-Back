package com.example.BeGroom.settlement.repository.daily;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailySettlementRepositoryCustom {
    void upsertAggregate(LocalDate date, Long sellerId, Long paymentAmount,
                BigDecimal feeRate, BigDecimal settlementAmount, BigDecimal refundAmount);

    void updateRefund(LocalDate date, Long sellerId, BigDecimal fee, BigDecimal refundAmount);
}
