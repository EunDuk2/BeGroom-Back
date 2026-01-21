package com.example.BeGroom.order.exception;

import com.example.BeGroom.order.dto.checkout.CheckoutFailCode;

public class CheckoutInsufficientBalanceException extends CheckoutException {

    public CheckoutInsufficientBalanceException(Long orderId, Long paymentId) {
        super(
                orderId,
                paymentId,
                CheckoutFailCode.INSUFFICIENT_BALANCE,
                "포인트 잔액이 부족합니다."
        );
    }

}
