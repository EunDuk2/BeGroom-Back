package com.example.BeGroom.payment.service;

import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentMethod;
import com.example.BeGroom.payment.domain.PaymentStatus;

public interface PaymentService {
    Payment create(Long orderId, PaymentMethod paymentMethod, PaymentStatus paymentStatus);
    void approve(Long paymentId);
}
