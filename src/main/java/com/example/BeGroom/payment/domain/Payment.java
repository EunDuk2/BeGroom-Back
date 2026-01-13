package com.example.BeGroom.payment.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.payment.exception.InvalidPaymentStateException;
import com.example.BeGroom.wallet.domain.Wallet;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "order_id")
    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(nullable = false)
    private boolean isSettled;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private PaymentFailReason paymentFailReason;

    private Payment(Order order, Long amount, PaymentMethod paymentMethod, PaymentStatus paymentStatus) {
        this.order = order;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.isSettled = false;
    }

    public static Payment create(Order order, Long amount, PaymentMethod paymentMethod, PaymentStatus paymentStatus) {
        return new Payment(order, amount, paymentMethod, paymentStatus);
    }

    public void process(Order order, Wallet wallet) {
        // OrderProduct한테 재고 차감 요청
        deductStock(order.getOrderProductList());
        // 포인트 차감 요청
        wallet.pay(order.getTotalAmount());
        // 결제 승인 처리
        approve();
        // 주문 완료 처리
        order.complete();
    }

    // 재고 차감
    public void deductStock(List<OrderProduct> orderProductList) {
        for(OrderProduct orderProduct : orderProductList) {
            orderProduct.deductStock();
        }
    }

    public void markProcessing() {
        if(this.paymentStatus != PaymentStatus.READY) {
            throw new InvalidPaymentStateException("결제 진행", this.paymentStatus);
        }
        this.paymentStatus = PaymentStatus.PROCESSING;
    }

    public void approve() {
        if(this.paymentStatus != PaymentStatus.PROCESSING) {
            throw new InvalidPaymentStateException("결제 승인", this.paymentStatus);
        }
        this.paymentStatus = PaymentStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void fail(PaymentFailReason paymentFailReason) {
        if(this.paymentStatus != PaymentStatus.READY) {
            throw new InvalidPaymentStateException("결제 처리", this.paymentStatus);
        }
        this.paymentStatus = PaymentStatus.FAILED;
        this.paymentFailReason = paymentFailReason;
    }

    public void refund(RefundReason refundReason) {
        if(this.paymentStatus != PaymentStatus.APPROVED) {
            throw new InvalidPaymentStateException("결제 환불", this.paymentStatus);
        }
        this.paymentStatus = PaymentStatus.REFUNDED;
        this.refundReason = refundReason;
    }

    public void markSettled(){
        this.isSettled = true;
    }

}
