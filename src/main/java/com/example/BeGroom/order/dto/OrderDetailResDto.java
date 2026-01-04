package com.example.BeGroom.order.dto;

import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.domain.OrderStatus;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentMethod;
import com.example.BeGroom.payment.domain.PaymentStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResDto {

    @Schema(example = "1")
    private Long orderId;

    @Schema(example = "2025-01-01T14:30:00")
    private LocalDateTime orderedAt;

    @Schema(example = "COMPLETED")
    private OrderStatus orderStatus;

    @Schema(example = "6990")
    private Long totalAmount;

    @Schema(example = "POINT")
    private PaymentMethod paymentMethod;

    @Schema(example = "APPROVED")
    private PaymentStatus paymentStatus;

    @ArraySchema(
            schema = @Schema(implementation = OrderProductResDto.class),
            arraySchema = @Schema(description = "주문 상품 목록")
    )
    private List<OrderProductResDto> orderProductList;

    public static OrderDetailResDto of(
            Order order,
            Payment payment,
            List<OrderProduct> orderProducts
    ) {
        List<OrderProductResDto> orderProductResDtos
                = orderProducts.stream().map(OrderProductResDto::of).toList();

        return OrderDetailResDto.builder()
                .orderId(order.getId())
                .orderedAt(order.getCreatedAt())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(payment == null ? null : payment.getPaymentMethod())
                .paymentStatus(payment == null ? null : payment.getPaymentStatus())
                .orderProductList(orderProductResDtos)
                .build();
    }

}
