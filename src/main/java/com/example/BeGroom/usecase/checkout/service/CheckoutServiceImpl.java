package com.example.BeGroom.usecase.checkout.service;

import com.example.BeGroom.usecase.checkout.dto.CheckoutResDto;
import com.example.BeGroom.payment.domain.PaymentMethod;
import com.example.BeGroom.payment_processor.PaymentProcessor;
import com.example.BeGroom.payment_processor.PaymentProcessorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final PaymentProcessorFactory paymentProcessorFactory;

    @Override
    @Transactional
    public CheckoutResDto checkout(Long paymentId, PaymentMethod paymentMethod) {
        // method로 processor가져오기
        PaymentProcessor paymentProcessor = paymentProcessorFactory.get(paymentMethod);
        // 결과값 반환
        return paymentProcessor.process(paymentId);
    }

}
