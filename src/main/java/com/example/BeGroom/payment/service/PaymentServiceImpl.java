package com.example.BeGroom.payment.service;

import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentFailReason;
import com.example.BeGroom.payment.domain.PaymentMethod;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.payment.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Payment create(Long orderId, PaymentMethod paymentMethod) {
        // 주문 조회
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("없는 주문입니다."));
        // 주문 검증 및 상태 변경
        order.markPaymentPending();
        // 결제 생성
        Payment payment = Payment.create(order, order.getTotalAmount(), paymentMethod, PaymentStatus.READY);
        // 결제 저장
        paymentRepository.save(payment);

        return payment;
    }

    @Transactional
    @Override
    public void approve(Long paymentId) {
        // 결제 조회
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new EntityNotFoundException("없는 결제입니다."));
        // 결제 검증 및 상태 변경 (PROCESSING -> APPROVED)
        payment.approve();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long paymentId, PaymentFailReason reason) {
        // 결제 조회
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new EntityNotFoundException("없는 결제입니다."));
        // 실패 처리
        payment.fail(reason);
    }


}
