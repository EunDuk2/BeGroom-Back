package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.settlement.domain.Settlement;
import org.junit.jupiter.api.AfterEach;
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
import static com.example.BeGroom.payment.domain.PaymentStatus.FAILED;
import static com.example.BeGroom.settlement.domain.SettlementPaymentStatus.*;
import static com.example.BeGroom.settlement.domain.SettlementStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class SettlementRepositoryTest {

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


}
