package com.example.BeGroom.order.dto;

import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryDto {

    @Schema(example = "1")
    private Long orderId;

    @Schema(example = "2025-01-01T14:30:00")
    private LocalDateTime orderedAt;

    @Schema(example = "COMPLETED")
    private OrderStatus orderStatus;

    @Schema(example = "6990")
    private Long totalAmount;

    @Schema(example = "국산콩 두부")
    private String representativeProductName;

    @Schema(example = "2")
    private Integer additionalProductCount;

    public static OrderSummaryDto of(
            Order order,
            OrderProductAggregate aggregate
    ) {
        return OrderSummaryDto.builder()
                .orderId(order.getId())
                .orderedAt(order.getCreatedAt())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .representativeProductName(
                        aggregate != null ? aggregate.getRepresentativeProductName() : null
                )
                .additionalProductCount(
                        aggregate != null ? aggregate.getProductCount().intValue() - 1 : 0
                )
                .build();
    }
}
