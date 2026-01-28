package com.example.BeGroom.seller.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecentSettlementResDto {

    @Schema(example = "1", description = "정산번호")
    private Long settlementId;
    @Schema(example = "115200", description = "정산금액")
    private BigDecimal settlementAmount;
    @Schema(example = "2026-01-03T14:30:00", description = "정산일시")
    private LocalDateTime settledAt;

}
