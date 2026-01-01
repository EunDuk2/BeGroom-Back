package com.example.BeGroom.payment.service;

import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentMethod;

public interface PaymentService {
    Payment create(Long orderId, PaymentMethod paymentMethod);
    void approve(Long paymentId);
}
