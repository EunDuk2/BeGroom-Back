package com.example.BeGroom.seller.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class RecentPaymentResDto {

    @Schema(example = "1", description = "주문번호")
    private Long paymentId;
    @Schema(example = "128000", description = "주문금액")
    private Long amount;
    @Schema(example = "2026-01-03T14:30:00", description = "주문일시")
    private LocalDateTime orderedAt;

}
