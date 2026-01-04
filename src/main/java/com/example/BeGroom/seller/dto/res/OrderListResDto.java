package com.example.BeGroom.seller.dto.res;

import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.settlement.domain.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResDto {

    @Schema(description = "주문 목록")
    private List<OrderItem> orders;

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
