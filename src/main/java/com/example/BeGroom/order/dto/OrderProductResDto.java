package com.example.BeGroom.order.dto;

import com.example.BeGroom.order.domain.OrderProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProductResDto {

    @Schema(example = "1")
    private Long productId;

    @Schema(example = "국산콩 두부")
    private String productName;

    @Schema(example = "4990")
    private Integer orderPrice;

    @Schema(example = "2")
    private Integer orderQuantity;


    public static OrderProductResDto of(OrderProduct orderProduct) {
        return OrderProductResDto.builder()
                .productId(orderProduct.getProduct().getProductId())
                .productName(orderProduct.getProduct().getName())
                .orderPrice(orderProduct.getPrice())
                .orderQuantity(orderProduct.getQuantity())
                .build();
    }
}
