package com.example.BeGroom.order.exception;

import com.example.BeGroom.order.dto.checkout.CheckoutFailCode;

public class CheckoutInsufficientStockException extends CheckoutException {

    public CheckoutInsufficientStockException(Long orderId, Long paymentId) {
        super(
                orderId,
                paymentId,
                CheckoutFailCode.INSUFFICIENT_STOCK,
                "재고가 부족합니다."
        );
    }

}
