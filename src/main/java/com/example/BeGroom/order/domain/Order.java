package com.example.BeGroom.order.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.order.exception.InvalidOrderStateException;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.product.domain.ProductDetail;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    private Order(Member member, Long totalAmount, OrderStatus orderStatus) {
        this.member = member;
        this.totalAmount = totalAmount;
        this.orderStatus = orderStatus;
    }

    public static Order create(Member member, List<OrderLineRequest> orderLineRequests) {
        // 주문 생성
        Order order = new Order(member, 0L, OrderStatus.CREATED);

        for(OrderLineRequest orderLineRequest : orderLineRequests) {
            ProductDetail productDetail = orderLineRequest.productDetail();
            int orderQuantity = orderLineRequest.quantity();

            // productDetail에게 재고 검증 요청
            productDetail.validateOrderable(orderQuantity);
            // orderProduct 추가 요청
            order.addOrderProduct(productDetail, orderQuantity);
        }

        return order;
    }

    private void addOrderProduct(ProductDetail productDetail, int quantity) {
        OrderProduct orderProduct = OrderProduct.create(productDetail, quantity, productDetail.getSellingPrice());

        this.totalAmount += orderProduct.getTotalAmount();

        orderProduct.assignOrder(this);
        orderProductList.add(orderProduct);
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
