package com.example.BeGroom.payment_processor;

import com.example.BeGroom.checkout.dto.CheckoutResDto;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentMethod;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.payment.service.PaymentService;
import com.example.BeGroom.wallet.service.WalletService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointPaymentProcessor implements PaymentProcessor {

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final WalletService walletService;

    @Override
    public PaymentMethod getMethod() { return PaymentMethod.POINT; }

    @Override
    @Transactional
    public CheckoutResDto process(Long orderId) {
        // 주문 조회
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("없는 주문입니다."));
        // 결제 생성
        Payment payment = paymentService.create(orderId, PaymentMethod.POINT, PaymentStatus.PROCESSING);
        // 포인트 결제 처리
        walletService.payPoint(order.getMember().getId(), order.getTotalAmount(), payment.getId());
        // 결제 승인
        paymentService.approve(payment.getId());
        // 반환값 조립
        CheckoutResDto checkoutResDto =
                CheckoutResDto.completed(order.getId(), payment.getId());

        return checkoutResDto;
    }
}
