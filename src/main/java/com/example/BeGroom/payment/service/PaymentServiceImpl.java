package com.example.BeGroom.payment.service;

import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {


    @Override
    @Transactional
    public Payment create(Long orderId, Long amount, PaymentMethod paymentMethod) {
        // 주문 조회 및 검증

        // 결제 생성



        return null;
    }
}
