package com.example.BeGroom.usecase.cancel_order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderResDto {
    @Schema(example = "1")
    private Long orderId;
}
