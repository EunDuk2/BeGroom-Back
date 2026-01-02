package com.example.BeGroom.checkout.dto;

import com.example.BeGroom.payment.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutReqDto {

    @NotNull
    @Schema(example = "POINT")
    private PaymentMethod paymentMethod;

}
