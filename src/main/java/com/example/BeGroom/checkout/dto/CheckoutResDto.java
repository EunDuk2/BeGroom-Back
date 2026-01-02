package com.example.BeGroom.checkout.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResDto {

    @Schema(example = "COMPLETED")
    private CheckoutStatus checkoutStatus;

    @Schema(example = "1")
    private Long orderId;

    @Schema(example = "2")
    private Long paymentId;

    @Schema(example = "https://redirect.com")
    private String redirectUrl;

    public static CheckoutResDto completed(Long orderId, Long paymentId) {
        return new CheckoutResDto(
                CheckoutStatus.COMPLETED,
                orderId,
                paymentId,
                null
        );
    }

    public static CheckoutResDto redirect(Long orderId, Long paymentId, String redirectUrl) {
        return new CheckoutResDto(
                CheckoutStatus.REDIRECT_REQUIRED,
                orderId,
                paymentId,
                redirectUrl
        );
    }

}
