package com.example.BeGroom.payment.service;

import com.example.BeGroom.notification.service.NotificationService;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentFailReason;
import com.example.BeGroom.payment.domain.PaymentMethod;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.payment.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.BeGroom.notification.domain.NotificationTemplate;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

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
        // 알림 발송
        try {
            Long receiverId = order.getMember().getId();
            List<Long> receivers = List.of(receiverId);
            Map<String, String> variables = getOrderCompleteNoticeMap(order);
            notificationService.send(receivers, NotificationTemplate.ORDER_SINGLE_PRODUCT.getId(), variables);
        } catch (Exception e) {
            // 전송 실패
        }
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

    private static @NonNull Map<String, String> getOrderCompleteNoticeMap(Order order) {
        List<OrderProduct> products = order.getOrderProductList();
        int productCount = products.size();

        Map<String, String> variables = new HashMap<>();
        variables.put("orderId", String.valueOf(order.getId()));

        String firstProductName = products.getFirst().getProductDetail().getName();
        if (productCount > 1) {
            variables.put("productName", firstProductName + " 외 " + (productCount - 1) + "건");
        }
        else {
            variables.put("productName", firstProductName);
        }
        return variables;
    }
}
