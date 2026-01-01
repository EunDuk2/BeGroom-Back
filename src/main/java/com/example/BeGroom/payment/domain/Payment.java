package com.example.BeGroom.payment.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.order.domain.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "order_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Order order;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private PaymentStatus paymentStatus;

    private LocalDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private RefundReason refundReason;

    private Payment(Order order, Long amount, PaymentMethod paymentMethod, PaymentStatus paymentStatus) {
        this.order = order;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    public static Payment create(Order order, Long amount, PaymentMethod paymentMethod, PaymentStatus paymentStatus) {
        return new Payment(order, amount, paymentMethod, paymentStatus);
    }

    public void approve() {
        if(this.paymentStatus != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("결제 승인 불가능한 상태입니다. status=" + this.paymentStatus);
        }
        this.paymentStatus = PaymentStatus.APPROVED;
    }

}
