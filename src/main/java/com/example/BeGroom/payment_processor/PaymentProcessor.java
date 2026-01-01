package com.example.BeGroom.payment_processor;

import com.example.BeGroom.payment.domain.PaymentMethod;

public interface PaymentProcessor {
    PaymentMethod getMethod();
}
