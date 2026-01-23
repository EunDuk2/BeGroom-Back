package com.example.BeGroom.payment.domain;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.domain.Role;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderLineRequest;
import com.example.BeGroom.product.domain.ProductDetail;
import com.example.BeGroom.wallet.domain.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.BeGroom.order.domain.OrderStatus.COMPLETED;
import static com.example.BeGroom.payment.domain.PaymentMethod.POINT;
import static com.example.BeGroom.payment.domain.PaymentStatus.APPROVED;
import static com.example.BeGroom.payment.domain.PaymentStatus.PROCESSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    @DisplayName("결제 처리 시 재고, 지갑, 주문, 결제 상태가 모두 완료 상태로 변경된다")
    @Test
    void process_success() {
        // given
        Member member = createMember();
        ProductDetail apple = createProductDetail("사과", 3000, 99);
        ProductDetail pear = createProductDetail("배", 5000, 99);
        List<OrderLineRequest> orderLineRequests = List.of(
                createOrderLine(apple, 1),
                createOrderLine(pear, 1)
        );
        Order order = Order.create(member, orderLineRequests);
        Wallet wallet = Wallet.create(member, 20000L);
        Payment payment = Payment.create(order, order.getTotalAmount(), POINT, PROCESSING);
        order.markPaymentPending();

        // when
        payment.process(order, wallet);

        // then
        assertThat(payment.getPaymentStatus()).isEqualTo(APPROVED);
        assertThat(order.getOrderStatus()).isEqualTo(COMPLETED);
        assertThat(wallet.getBalance()).isEqualTo(20000L - order.getTotalAmount());
    }


    /* =========================
     *  테스트 내부 공용 메서드 (Given)
     * ========================= */

    private Member createMember() {
        return Member.createMember(
                "test@naver.com",
                "test",
                "1234",
                "010",
                Role.USER
        );
    }

    private ProductDetail createProductDetail(String name, int price, int quantity) {
        return ProductDetail.builder()
                .name(name)
                .basePrice(price)
                .quantity(quantity)
                .build();
    }

    private OrderLineRequest createOrderLine(ProductDetail productDetail, int quantity) {
        return new OrderLineRequest(productDetail, quantity);
    }
}