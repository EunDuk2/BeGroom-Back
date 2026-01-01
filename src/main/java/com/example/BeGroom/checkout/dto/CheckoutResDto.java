package com.example.BeGroom.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResDto {

    private CheckoutStatus checkoutStatus;
    private Long orderId;
    private Long paymentId;
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
