package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.domain.SettlementPaymentStatus;
import com.example.BeGroom.settlement.domain.SettlementStatus;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.example.BeGroom.member.domain.Member.*;
import static com.example.BeGroom.member.domain.Role.USER;
import static com.example.BeGroom.order.domain.OrderStatus.COMPLETED;
import static com.example.BeGroom.payment.domain.PaymentMethod.POINT;
import static com.example.BeGroom.payment.domain.PaymentStatus.APPROVED;
import static com.example.BeGroom.payment.domain.PaymentStatus.FAILED;
import static com.example.BeGroom.settlement.domain.SettlementPaymentStatus.*;
import static com.example.BeGroom.settlement.domain.SettlementStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.*;

@Transactional
public class SettlementRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @AfterEach
    void tearDown(){
//        memberRepository.deleteAllInBatch();
//        orderRepository.deleteAllInBatch();
//        paymentRepository.deleteAllInBatch();
    }

    @DisplayName("승인 완료된 미정산 결제 건을 조회한다.")
    @Test
    void findApprovedUnsettledPayments() {
        // given
        Member member = createMember("hong@naver.com", "홍길동", "1234", "01012345678", USER);
        Order order1 = Order.create(member, 100000L, COMPLETED);
        Order order2 = Order.create(member, 200000L, COMPLETED);
        Order order3 = Order.create(member, 300000L, COMPLETED);
        Payment payment1 = Payment.create(order1, 100000L, POINT, APPROVED);
        Payment payment2 = Payment.create(order2, 200000L, POINT, FAILED);
        Payment payment3 = Payment.create(order2, 300000L, POINT, APPROVED);
        memberRepository.save(member);
        orderRepository.saveAll(List.of(order1, order2, order3));
        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        // when
        List<Payment> payments = paymentRepository.findApprovedPayments(APPROVED);

        // then
        assertThat(payments).hasSize(2)
                .extracting("amount", "paymentStatus", "isSettled")
                .containsExactlyInAnyOrder(
                        tuple(100000L, APPROVED, false),
                        tuple(300000L, APPROVED, false)
                );
    }

    @DisplayName("승인 완료된 미정산 결제 건을 정산 테이블에 적재한다.")
    @Test
    void createSettlementByPayments() {
        // given
        Member member = createMember("hong@naver.com", "홍길동", "1234", "01012345678", USER);
        Order order1 = Order.create(member, 100000L, COMPLETED);
        Order order2 = Order.create(member, 200000L, COMPLETED);
        Order order3 = Order.create(member, 300000L, COMPLETED);
        Payment payment1 = Payment.create(order1, 100000L, POINT, APPROVED);
        Payment payment2 = Payment.create(order2, 200000L, POINT, FAILED);
        Payment payment3 = Payment.create(order2, 300000L, POINT, APPROVED);
        memberRepository.save(member);
        orderRepository.saveAll(List.of(order1, order2, order3));
        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        List<Payment> payments = paymentRepository.findApprovedPayments(APPROVED);

        // when


        // then

    }



    private Settlement createSettlement(Seller seller, Payment payment, Long paymentAmount){
        return Settlement.builder()
                .seller(seller)
                .payment(payment)
                .paymentAmount(paymentAmount)
                .feeRate(BigDecimal.valueOf(10.00))
                .fee(BigDecimal.valueOf(paymentAmount*10.00))
                .settlementAmount(BigDecimal.valueOf(paymentAmount - (paymentAmount*10.00)))
                .status(UNSETTLED)
                .settlementPaymentStatus(PAYMENT)
                .refundAmount(BigDecimal.ZERO)
                .build();
    }
}
