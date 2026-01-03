package com.example.BeGroom.usecase.checkout.service;

import com.example.BeGroom.usecase.checkout.dto.CheckoutResDto;
import com.example.BeGroom.payment.domain.PaymentMethod;

public interface CheckoutService {
    CheckoutResDto checkout(Long paymentId, PaymentMethod paymentMethod);
}
