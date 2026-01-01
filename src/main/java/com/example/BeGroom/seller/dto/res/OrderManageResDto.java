package com.example.BeGroom.seller.dto.res;

import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.seller.domain.SettlementStatus;
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
public class OrderManageResDto {

    @NotNull
    @Schema(description = "주문 요약 정보")
    private Summary summary;
    @NotNull
    @Schema(description = "주문 목록")
    private List<OrderItem> orders;

    @Data
    @AllArgsConstructor
    public static class Summary{
        @Schema(example = "2")
        private int refundCnt;
        @Schema(example = "1")
        private int unsettledCnt;
    }

    @Data
    public static class OrderItem {
        @Schema(example = "1")
        private Long id;
        @Schema(example = "2024-12-28 11:10")
        private LocalDateTime createdAt;
        @Schema(example = "128000")
        private long price;
        @Schema(example = "POINT")
        private String paymentMethod;
        @Schema(example = "UNSETTLED")
        private SettlementStatus settlementStatus;
        @Schema(example = "REFUND")
        private PaymentStatus paymentStatus;
    }
}
