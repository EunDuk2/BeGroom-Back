package com.example.BeGroom.settlement.dto.res;

import com.example.BeGroom.settlement.domain.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementManageResDto {

    @NotNull
    @Schema(description = "정산 요약 정보")
    private Summary summary;
    @NotNull
    @Schema(description = "정산 건별 목록")
    private List<SettlementByItem> settlementByItemList;

    @Data
    @AllArgsConstructor
    public static class Summary{
        @Schema(example = "2300000")
        private int totalPaymentAmount;
        @Schema(example = "66000")
        private int totalRefundAmount;
        @Schema(example = "223400")
        private int totalFeeAmount;
        @Schema(example = "2010600")
        private int totalSettlementAmount;
    }

    @Data
    public static class SettlementByItem {
        @Schema(example = "1")
        private Long id;
        @Schema(example = "2025-01-01T14:30:00")
        private LocalDateTime paidAt;
        @Schema(example = "128000")
        private long paymentAmount;
        @Schema(example = "9000")
        private long refundAmount;
        @Schema(example = "12800")
        private long feeAmount;
        @Schema(example = "107100")
        private long settlementAmount;
        @Schema(example = "SETTLED")
        private SettlementStatus settlementStatus;
    }

}
