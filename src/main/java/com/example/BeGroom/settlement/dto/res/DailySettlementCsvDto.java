package com.example.BeGroom.settlement.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySettlementCsvDto {

    @Schema(example = "2026-01-07", description = "정산일")
    private LocalDate settlementDate;

    @Schema(example = "1200000.00", description = "결제금액")
    private BigDecimal paymentAmount;

    @Schema(example = "120000.00", description = "수수료")
    private BigDecimal fee;

    @Schema(example = "108000.00", description = "정산금액")
    private BigDecimal settlementAmount;

    @Schema(example = "0.00", description = "환불금액")
    private BigDecimal refundAmount;

}
