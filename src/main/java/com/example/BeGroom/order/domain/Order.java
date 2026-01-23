package com.example.BeGroom.order.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.order.exception.InvalidOrderStateException;
import com.example.BeGroom.payment.domain.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProductList = new ArrayList<>();

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<Payment> payments;

//    private Order(Member member, Long totalAmount, OrderStatus orderStatus) {
//        this.member = member;
//        this.totalAmount = totalAmount;
//        this.orderStatus = orderStatus;
//    }

    @Builder
    public Order(Member member, Long totalAmount, OrderStatus orderStatus) {
        this.member = member;
        this.totalAmount = totalAmount;
        this.orderStatus = orderStatus;
    }

    public static Order create(Member member, Long totalAmount, OrderStatus orderStatus) {
        return new Order(member, totalAmount, orderStatus);
    }

    public void addOrderProduct(OrderProduct orderProduct) {
        if(orderProduct == null) {
            throw new IllegalArgumentException("추가하려는 상품이 없습니다.");
        }
        if(this.orderStatus != OrderStatus.CREATED) {
            throw new InvalidOrderStateException("상품 추가", this.orderStatus);
        }
        // todo - assigned 함수 고려
        this.orderProductList.add(orderProduct);
        this.totalAmount += orderProduct.getTotalAmount();
    }

    public void markPaymentPending() {
        if(this.orderStatus == OrderStatus.COMPLETED ||
                this.orderStatus == OrderStatus.CANCELED) {
            throw new InvalidOrderStateException("결제 생성", this.orderStatus);
        }
        this.orderStatus = OrderStatus.PAYMENT_PENDING;
    }


    public void complete() {
        if(this.orderStatus != OrderStatus.PAYMENT_PENDING) {
            throw new InvalidOrderStateException("결제 완료", this.orderStatus);
        }
        this.orderStatus = OrderStatus.COMPLETED;
    }

    public void cancel() {
        if(this.orderStatus != OrderStatus.COMPLETED) {
            throw new InvalidOrderStateException("결제 취소", this.orderStatus);
        }
        this.orderStatus = OrderStatus.CANCELED;
    }

}
