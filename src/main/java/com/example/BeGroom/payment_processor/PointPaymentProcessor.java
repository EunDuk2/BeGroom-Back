package com.example.BeGroom.payment_processor;

import com.example.BeGroom.checkout.dto.CheckoutFailCode;
import com.example.BeGroom.checkout.dto.CheckoutResDto;
import com.example.BeGroom.checkout.exception.CheckoutInsufficientBalanceException;
import com.example.BeGroom.checkout.exception.CheckoutInsufficientStockException;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentFailReason;
import com.example.BeGroom.payment.domain.PaymentMethod;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.payment.service.PaymentService;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.exception.InsufficientStockException;
import com.example.BeGroom.wallet.exception.InsufficientBalanceException;
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
    private final PaymentRepository paymentRepository;
    private final WalletService walletService;

    @Override
    public PaymentMethod getMethod() { return PaymentMethod.POINT; }

    @Override
    @Transactional
    public CheckoutResDto process(Long paymentId) {
        // 결제 조회
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new EntityNotFoundException("없는 결제입니다."));
        // 결제 상태 변경 (PROCESSING)
        payment.markProcessing();
        // 주문 조회
        Order order = payment.getOrder();
        try {
            // 포인트 결제 처리
            walletService.payPoint(order.getMember().getId(), order.getTotalAmount(), payment.getId());
            // 결제 승인
            paymentService.approve(payment.getId());
            // 상품 재고 감소
            for(OrderProduct orderProduct : order.getOrderProductList()) {
                orderProduct.getProduct()
                        .decreaseStock(orderProduct.getQuantity());
            }
            // 주문 완료 처리
            order.complete();
            // 성공 반환
            return CheckoutResDto.completed(order.getId(), payment.getId());
        } catch (InsufficientBalanceException e) { // 현재 catch문 동작 못함
            // 실패 처리 (잔액 부족)
            paymentService.fail(payment.getId(), PaymentFailReason.INSUFFICIENT_BALANCE);
            // 예외 반환
            throw new CheckoutInsufficientBalanceException(order.getId(), payment.getId());
        } catch (InsufficientStockException e) {
            // 실패 처리 (재고 부족)
            paymentService.fail(payment.getId(), PaymentFailReason.INSUFFICIENT_STOCK);
            // 예외 반환
            throw new CheckoutInsufficientStockException(order.getId(), payment.getId());
        }

    }
}
