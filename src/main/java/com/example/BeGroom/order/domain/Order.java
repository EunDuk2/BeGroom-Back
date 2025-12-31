package com.example.BeGroom.order.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    private Order(Member member, Long totalAmount, OrderStatus orderStatus) {
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
        if(getOrderStatus() != OrderStatus.CREATED) {
            throw new IllegalStateException("주문 생성 상태에서만 상품을 추가할 수 있습니다.");
        }
        // todo - assigned 함수 고려
        this.orderProductList.add(orderProduct);
        this.totalAmount += orderProduct.getTotalAmount();
    }

}
