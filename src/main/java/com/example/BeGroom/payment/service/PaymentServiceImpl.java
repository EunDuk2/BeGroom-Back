package com.example.BeGroom.payment.service;

import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderStatus;
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
        // 주문 조회
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("없는 주문입니다."));
        // 주문 검증 (CREATED인지)
        order.validatePaymentCreatable();
        // 주문 상태 변경 (PAYMENT_PENDING)
        order.changeStatus(OrderStatus.PAYMENT_PENDING);
        // 결제 생성
        Payment payment = Payment.create(order, amount, paymentMethod, PaymentStatus.READY);
        // 결제 저장
        paymentRepository.save(payment);

        return payment;
    }
}
