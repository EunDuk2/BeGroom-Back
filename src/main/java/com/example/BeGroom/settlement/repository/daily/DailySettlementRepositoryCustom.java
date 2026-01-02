package com.example.BeGroom.settlement.repository.daily;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailySettlementRepositoryCustom {
    void upsert(LocalDate date, Long sellerId, Long paymentAmount,
                BigDecimal feeRate, BigDecimal settlementAmount, BigDecimal refundAmount);

    void updateRefund(BigDecimal refundAmount);
}
