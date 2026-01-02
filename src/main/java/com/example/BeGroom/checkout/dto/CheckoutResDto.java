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

    @Schema(example = "INSUFFICIENT_BALANCE")
    private CheckoutFailCode failCode;

    @Schema(example = "포인트 잔액이 부족합니다.")
    private String failMessage;

    public static CheckoutResDto completed(Long orderId, Long paymentId) {
        return new CheckoutResDto(
                CheckoutStatus.COMPLETED,
                orderId,
                paymentId,
                null,
                null,
                null
        );
    }

    public static CheckoutResDto redirect(Long orderId, Long paymentId, String redirectUrl) {
        return new CheckoutResDto(
                CheckoutStatus.REDIRECT_REQUIRED,
                orderId,
                paymentId,
                redirectUrl,
                null,
                null
        );
    }

    public static CheckoutResDto failed(Long orderId, Long paymentId, CheckoutFailCode checkoutFailCode) {
        return new CheckoutResDto(
                CheckoutStatus.FAILED,
                orderId,
                paymentId,
                null,
                checkoutFailCode,
                checkoutFailCode.getMessage()
        );
    }

}
