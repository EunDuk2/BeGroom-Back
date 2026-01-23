package com.example.BeGroom.settlement.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.repository.SellerRepository;
import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.repository.SettlementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.example.BeGroom.order.domain.OrderStatus.COMPLETED;
import static com.example.BeGroom.payment.domain.PaymentMethod.POINT;
import static com.example.BeGroom.payment.domain.PaymentStatus.APPROVED;
import static com.example.BeGroom.settlement.domain.SettlementPaymentStatus.PAYMENT;
import static com.example.BeGroom.settlement.domain.SettlementStatus.UNSETTLED;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class SettlementServiceTest {

    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;


    @DisplayName("승인 완료된 미정산 결제 건을 정산 테이블에 저장한다.")
    @Test
    void createSettlementWithPayments() {
        // given
        Payment payment1 = createPayment(APPROVED, false);
        Payment payment2 = createPayment(APPROVED, false);
        paymentRepository.saveAll(List.of(payment1, payment2));

        // when
//        Settlement settlement = createSettlement()

        // then

    }

    private Settlement createSettlement(Payment payment){
        Seller seller = sellerRepository.save(createSeller());
        return Settlement.builder()
                .seller(seller)
                .payment(payment)
                .paymentAmount(payment.getAmount())
                .feeRate(BigDecimal.valueOf(10.00))
                .fee(BigDecimal.valueOf(payment.getAmount()*10.00))
                .settlementAmount(BigDecimal.valueOf(payment.getAmount() - (payment.getAmount()*10.00)))
                .status(UNSETTLED)
                .settlementPaymentStatus(PAYMENT)
                .refundAmount(BigDecimal.ZERO)
                .build();
    }

    private Seller createSeller(){
        return Seller.builder()
                .email("goorm@goorm.com")
                .name("구름")
                .password("1234")
                .phoneNumber("01012345678")
                .build();
    }

    private Payment createPayment(PaymentStatus status, boolean isSettled){
        Member member = memberRepository.save(createMember());
        Order order = orderRepository.save(createOrder(member));
        return Payment.builder()
                .order(order)
                .amount(100000L)
                .paymentMethod(POINT)
                .paymentStatus(status)
                .isSettled(isSettled)
                .build();
    }

    private Member createMember(){
        return Member.builder()
                .email("hong@test.com")
                .name("홍길동")
                .password("1234")
                .phoneNumber("01012345678")
                .build();
    }

    private Order createOrder(Member member){
        return Order.builder()
                .member(member)
                .totalAmount(100000L)
                .orderStatus(COMPLETED)
                .build();
    }

}
