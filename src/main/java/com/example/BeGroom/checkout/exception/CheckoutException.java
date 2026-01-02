package com.example.BeGroom.checkout.exception;

import com.example.BeGroom.checkout.dto.CheckoutFailCode;
import lombok.Getter;

@Getter
public abstract class CheckoutException extends RuntimeException{

    private final Long orderId;
    private final Long paymentId;
    private final CheckoutFailCode failCode;

    protected CheckoutException(
            Long orderId,
            Long paymentId,
            CheckoutFailCode failCode,
            String message
    ) {
        super(message);
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.failCode = failCode;
    }

}
