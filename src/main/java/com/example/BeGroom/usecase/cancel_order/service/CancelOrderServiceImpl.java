package com.example.BeGroom.usecase.cancel_order.service;

import com.example.BeGroom.notification.domain.NotificationTemplate;
import com.example.BeGroom.notification.service.NotificationService;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.payment.domain.RefundReason;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.payment.service.PaymentService;
import com.example.BeGroom.wallet.service.WalletService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CancelOrderServiceImpl implements CancelOrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final WalletService walletService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void cancelOrder(Long memberId, Long orderId) {

        // 주문 조회
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("없는 주문입니다."));
        // 결제 조회
        Payment payment = paymentRepository.findByOrderIdAndPaymentStatus(orderId, PaymentStatus.APPROVED)
                .orElseThrow(() -> new EntityNotFoundException("없는 결제입니다."));
        // 포인트 복구
        walletService.refundPoint(memberId, order.getTotalAmount(), orderId);
        // 상품 재고 복구
        for(OrderProduct orderProduct : order.getOrderProductList()) {
            if (orderProduct.getProductDetail() != null) {
                orderProduct.getProductDetail().increaseStock(orderProduct.getQuantity());
            }
        }
        // 주문 취소 처리
        order.cancel();
        // 결제 환불 처리
        payment.refund(RefundReason.CUSTOMER_REQUEST);
        // 알림 발송
        try {
            Long receiverId = order.getMember().getId();
            List<Long> receivers = List.of(receiverId);
            Map<String, String> variables = getRefundCompleteNoticeMap(order);
            notificationService.send(receivers, NotificationTemplate.ORDER_REFUND_COMPLETE.getId(), variables);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    private static @NonNull Map<String, String> getRefundCompleteNoticeMap(Order order) {
        Map<String, String> variables = new HashMap<>();
        variables.put("orderId", String.valueOf(order.getId()));
        variables.put("refundAmount", String.valueOf(order.getTotalAmount()));
        return variables;
    }
}
