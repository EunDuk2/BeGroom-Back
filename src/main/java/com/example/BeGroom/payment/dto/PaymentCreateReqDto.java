package com.example.BeGroom.payment.dto;

import com.example.BeGroom.payment.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCreateReqDto {

    @NotNull
    @Schema(example = "1")
    private Long orderId;

    @NotNull
    @Schema(example = "POINT")
    private PaymentMethod paymentMethod;

}
