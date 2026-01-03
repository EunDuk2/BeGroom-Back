package com.example.BeGroom.settlement.dto.res;

import com.example.BeGroom.settlement.domain.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodSettlementResDto {

    @NotEmpty
    @Schema(example = "2025-12-01", description = "정산 기간")
    private String period;

    @Schema(example = "10", description = "주문 건수")
    private int orderCnt;

    @Schema(example = "500000", description = "총 매출 금액")
    private long totalSalesAmount;

    @Schema(example = "50000", description = "총 수수료 금액")
    private long totalFeeAmount;

    @Schema(example = "450000", description = "정산 금액")
    private long settlementAmount;

    @NotNull
    @Schema(example = "SETTLED", description = "정산 상태")
    private SettlementStatus status;
}
