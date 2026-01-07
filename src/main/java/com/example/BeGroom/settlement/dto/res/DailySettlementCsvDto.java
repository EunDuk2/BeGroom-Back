package com.example.BeGroom.settlement.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySettlementCsvDto {

    private LocalDate settlementDate;
    private BigDecimal paymentAmount;
    private BigDecimal fee;
    private BigDecimal settlementAmount;
    private BigDecimal refundAmount;

}
