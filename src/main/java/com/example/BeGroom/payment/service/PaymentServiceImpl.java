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

    /*
    create()
    // 알림 발송
        try {
            Long receiverId = order.getMember().getId();
            List<Long> receivers = List.of(receiverId);
            Map<String, String> variables = getOrderCompleteNoticeMap(order);
            notificationService.send(receivers, NotificationTemplate.ORDER_SINGLE_PRODUCT.getId(), variables);
        } catch (RuntimeException e) {
            throw e;
        }
     */
}
