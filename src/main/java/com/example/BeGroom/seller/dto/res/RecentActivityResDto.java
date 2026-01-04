package com.example.BeGroom.seller.dto.res;

import com.example.BeGroom.order.domain.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityResDto {

    @Schema(description = "새로운 주문")
    private RecentOrderDto recentOrder;

    @Schema(description = "최근 환불")
    private RecentRefundDto recentRefund;

    @Schema(description = "최근 정산")
    private RecentSettlementDto recentSettlement;

    @AllArgsConstructor
    public static class RecentOrderDto{
        @Schema(example = "1", description = "주문번호")
        private Long orderId;
        @Schema(example = "128000", description = "주문금액")
        private Long amount;
        @Schema(example = "2026-01-03T14:30:00", description = "주문일시")
        private LocalDateTime orderedAt;
    }

    @AllArgsConstructor
    public static class RecentRefundDto{
        @Schema(example = "1", description = "결제번호")
        private Long paymentId;
        @Schema(example = "128000", description = "환불금액")
        private BigDecimal refundAmount;
        @Schema(example = "2026-01-03T14:30:00", description = "환불일시")
        private LocalDateTime refundedAt;
    }

    @AllArgsConstructor
    public static class RecentSettlementDto{
        @Schema(example = "1", description = "정산번호")
        private Long settlementId;
        @Schema(example = "115200", description = "정산금액")
        private BigDecimal settlementAmount;
        @Schema(example = "2026-01-03T14:30:00", description = "정산일시")
        private LocalDateTime settledAt;
    }
}
