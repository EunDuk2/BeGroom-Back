package com.example.BeGroom.checkout.service;

import com.example.BeGroom.checkout.dto.CheckoutResDto;
import com.example.BeGroom.payment.domain.PaymentMethod;

public interface CheckoutService {
    CheckoutResDto checkout(Long orderId, PaymentMethod paymentMethod);
}
