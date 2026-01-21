package com.example.BeGroom.order.domain;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.domain.Role;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentMethod;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.product.domain.ProductDetail;
import com.example.BeGroom.product.exception.InsufficientStockException;
import com.example.BeGroom.wallet.domain.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.example.BeGroom.order.domain.OrderStatus.PAYMENT_PENDING;
import static com.example.BeGroom.payment.domain.PaymentMethod.POINT;
import static com.example.BeGroom.payment.domain.PaymentStatus.PROCESSING;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order 도메인 Test")
class OrderTest  {

    /* =========================
     *  주문 생성 성공
     * ========================= */

    @DisplayName("주문을 생성하면 주문 상품이 생성되고 총 금액이 계산된다")
    @Test
    void create() {
        // given
        Member member = createMember();

        ProductDetail apple = createProductDetail("사과", 3000, 1);
        ProductDetail pear = createProductDetail("배", 5000, 2);

        List<OrderLineRequest> orderLineRequests = List.of(
                createOrderLine(apple, 1),
                createOrderLine(pear, 2)
        );

        // when
        Order order = Order.create(member, orderLineRequests);

        // then
        assertThat(order.getMember()).isEqualTo(member);
        assertThat(order.getTotalAmount()).isEqualTo(13000);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getOrderProductList()).hasSize(2)
                .extracting(
                        op -> op.getProductDetail().getName(),
                        OrderProduct::getPrice,
                        OrderProduct::getQuantity
                )
                .containsExactlyInAnyOrder(
                        tuple("사과", 3000, 1),
                        tuple("배", 5000, 2)
                );
    }

    /* =========================
     *  주문 생성 실패 - 재고 조건
     * ========================= */

    @Nested
    @DisplayName("주문 생성 실패 - 재고 조건")
    class StockValidationTest {

        @Test
        @DisplayName("두 개의 상품을 주문할 때 모든 상품의 재고 수량이 주문 수량보다 부족하면 주문에 실패한다")
        void fail_when_all_products_stock_is_insufficient() {
            // given
            Member member = createMember();

            ProductDetail apple = createProductDetail("사과", 3000, 1);
            ProductDetail pear = createProductDetail("배", 5000, 1);

            List<OrderLineRequest> orderLineRequests = List.of(
                    createOrderLine(apple, 2),
                    createOrderLine(pear, 2)
            );

            // when then
            assertThatThrownBy(() -> Order.create(member, orderLineRequests))
                    .isInstanceOf(InsufficientStockException.class);
        }

        @Test
        @DisplayName("두 개의 상품을 주문할 때 하나라도 재고 수량이 주문 수량보다 부족하면 주문에 실패한다")
        void fail_when_any_product_stock_is_insufficient() {
            // given
            Member member = createMember();

            ProductDetail apple = createProductDetail("사과", 3000, 1);
            ProductDetail pear = createProductDetail("배", 5000, 2);

            List<OrderLineRequest> orderLineRequests = List.of(
                    createOrderLine(apple, 2),
                    createOrderLine(pear, 2)
            );

            // when then
            assertThatThrownBy(() -> Order.create(member, orderLineRequests))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }

    /* =========================
     *  주문 생성 실패 - 주문 상품 구성
     * ========================= */

    @Nested
    @DisplayName("주문 생성 실패 - 주문 상품 구성")
    class OrderLineValidationTest {

        @Test
        @DisplayName("주문 상품이 하나도 없으면 주문에 실패한다")
        void fail_when_order_products_is_empty() {
            // given
            Member member = createMember();
            List<OrderLineRequest> orderLineRequests = new ArrayList<>();

            // when then
            assertThatThrownBy(() -> Order.create(member, orderLineRequests))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("주문 상품이 없습니다.");
        }

        @Test
        @DisplayName("주문 상품 목록에 동일한 상품이 중복되어 있으면 주문에 실패한다")
        void fail_when_duplicate_product_exists() {
            // given
            Member member = createMember();

            ProductDetail apple = createProductDetail("사과", 3000, 10);

            List<OrderLineRequest> orderLineRequests = List.of(
                    createOrderLine(apple, 2),
                    createOrderLine(apple, 2)
            );

            // when then
            assertThatThrownBy(() -> Order.create(member, orderLineRequests))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("중복된 주문 상품이 있습니다.");
        }
    }

    /* =========================
     *  주문 생성 실패 - 주문 수량
     * ========================= */

    @Nested
    @DisplayName("주문 생성 실패 - 주문 수량")
    class QuantityValidationTest {

        @Test
        @DisplayName("주문 상품의 수량이 0이면 주문에 실패한다")
        void fail_when_order_quantity_is_zero() {
            // given
            Member member = createMember();

            ProductDetail apple = createProductDetail("사과", 3000, 10);

            List<OrderLineRequest> orderLineRequests = List.of(
                    createOrderLine(apple, 0)
            );

            // when then
            assertThatThrownBy(() -> Order.create(member, orderLineRequests))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("주문 상품의 수량이 0이하 입니다.");
        }

        @Test
        @DisplayName("주문 상품의 수량이 음수이면 주문에 실패한다")
        void fail_when_order_quantity_is_negative() {
            // given
            Member member = createMember();

            ProductDetail apple = createProductDetail("사과", 3000, 10);

            List<OrderLineRequest> orderLineRequests = List.of(
                    createOrderLine(apple, -1)
            );

            // when then
            assertThatThrownBy(() -> Order.create(member, orderLineRequests))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("주문 상품의 수량이 0이하 입니다.");
        }
    }

    /* =========================
     *  결제 시도 판단 및 결제 생성 성공
     * ========================= */

    @DisplayName("결제 시도 시 재고와 포인트가 모두 충분하면 결제가 생성된다.")
    @Test
    void checkout_success() {
        // given
        Member member = createMember();
        ProductDetail apple = createProductDetail("사과", 3000, 99);
        ProductDetail pear = createProductDetail("배", 5000, 99);
        List<OrderLineRequest> orderLineRequests = List.of(
                createOrderLine(apple, 1),
                createOrderLine(pear, 1)
        );

        Order order = Order.create(member, orderLineRequests);
        Wallet wallet = Wallet.create(member, 10000L);

        // when
        Payment payment = order.checkout(POINT, wallet);

        // then
        assertThat(order.getOrderStatus()).isEqualTo(PAYMENT_PENDING);
        assertThat(payment).isNotNull();
        assertThat(payment.getPaymentStatus()).isEqualTo(PROCESSING);
        assertThat(payment.getAmount()).isEqualTo(8000);
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