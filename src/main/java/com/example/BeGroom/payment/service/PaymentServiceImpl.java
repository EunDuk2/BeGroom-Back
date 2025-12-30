package com.example.BeGroom.payment.service;

import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentMethod;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.payment.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Payment create(Long orderId, Long amount, PaymentMethod paymentMethod) {
        // 주문 조회 및 검증
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("없는 주문입니다."));
        // 결제 생성
        Payment payment = Payment.create(order, amount, paymentMethod, PaymentStatus.READY);
        // 결제 저장
        paymentRepository.save(payment);

        return payment;
    }
}
