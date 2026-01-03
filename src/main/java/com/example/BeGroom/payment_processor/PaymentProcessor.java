package com.example.BeGroom.payment_processor;

import com.example.BeGroom.usecase.checkout.dto.CheckoutResDto;
import com.example.BeGroom.payment.domain.PaymentMethod;

public interface PaymentProcessor {
    PaymentMethod getMethod();
    CheckoutResDto process(Long paymentId);
}
