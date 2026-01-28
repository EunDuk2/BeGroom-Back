package com.example.BeGroom.seller.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class RecentRefundResDto {

    @Schema(example = "1", description = "결제번호")
    private Long paymentId;
    @Schema(example = "128000", description = "환불금액")
    private BigDecimal refundAmount;
    @Schema(example = "2026-01-03T14:30:00", description = "환불일시")
    private LocalDateTime refundedAt;

}
