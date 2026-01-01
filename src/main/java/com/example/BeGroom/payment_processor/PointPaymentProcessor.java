package com.example.BeGroom.payment_processor;

import com.example.BeGroom.payment.domain.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class PointPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.POINT;
    }
}
