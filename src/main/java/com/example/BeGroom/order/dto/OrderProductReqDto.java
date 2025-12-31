package com.example.BeGroom.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductReqDto {
    @NotNull
    private Long productId;
    @NotNull
    private Integer orderQuantity;
}
