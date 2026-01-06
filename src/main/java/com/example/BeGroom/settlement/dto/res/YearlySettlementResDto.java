package com.example.BeGroom.settlement.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YearlySettlementResDto {

    @Schema(example = "2025", description = "년")
    private int year;

    @Schema(example = "2025-12-01", description = "정산 시작일")
    private LocalDate startDate;

    @Schema(example = "2025-12-07", description = "정산 종료일")
    private LocalDate endDate;

//    @Schema(example = "10", description = "주문 건수")
//    private int orderCnt;

    @Schema(example = "500000", description = "총 매출 금액")
    private BigDecimal totalSalesAmount;

    @Schema(example = "50000", description = "총 수수료 금액")
    private BigDecimal totalFeeAmount;

    @Schema(example = "450000", description = "정산 금액")
    private BigDecimal settlementAmount;

//    @NotNull
//    @Schema(example = "SETTLED", description = "정산 상태")
//    private SettlementStatus status;
}
