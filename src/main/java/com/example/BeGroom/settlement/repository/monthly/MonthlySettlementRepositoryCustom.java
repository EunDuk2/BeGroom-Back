package com.example.BeGroom.settlement.repository.monthly;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MonthlySettlementRepositoryCustom {

    void upsertAggregate(int year, int month, Long sellerId, Long paymentAmount,
                BigDecimal feeRate, BigDecimal settlementAmount, BigDecimal refundAmount,
                LocalDate startDate, LocalDate endDate);

    void updateRefund(int year, int month, Long sellerId, BigDecimal fee, BigDecimal returnAmount);
}
