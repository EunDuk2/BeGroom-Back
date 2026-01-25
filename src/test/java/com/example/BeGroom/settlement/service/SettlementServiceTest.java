package com.example.BeGroom.settlement.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.repository.OrderProductRepository;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.product.domain.Brand;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductDetail;
import com.example.BeGroom.product.domain.ProductStatus;
import com.example.BeGroom.product.repository.BrandRepository;
import com.example.BeGroom.product.repository.ProductDetailRepository;
import com.example.BeGroom.product.repository.ProductRepository;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.repository.SellerRepository;
import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.domain.SettlementPaymentStatus;
import com.example.BeGroom.settlement.domain.SettlementStatus;
import com.example.BeGroom.settlement.repository.SettlementRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.example.BeGroom.order.domain.OrderStatus.COMPLETED;
import static com.example.BeGroom.payment.domain.PaymentMethod.POINT;
import static com.example.BeGroom.payment.domain.PaymentStatus.*;
import static com.example.BeGroom.product.domain.ProductStatus.*;
import static com.example.BeGroom.product.domain.QProduct.product;
import static com.example.BeGroom.settlement.domain.SettlementPaymentStatus.PAYMENT;
import static com.example.BeGroom.settlement.domain.SettlementPaymentStatus.REFUND;
import static com.example.BeGroom.settlement.domain.SettlementStatus.UNSETTLED;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class SettlementServiceTest {

    @Autowired
    private SettlementService settlementService;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailRepository productDetailRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderProductRepository orderProductRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    private Member member;
    private Seller seller;
    private ProductDetail productDetail;

//    @AfterEach
//    void tearDown(){
//
//    }

    @BeforeEach
    void setUp(){
        member = Member.builder()
                .email("hong@test.com")
                .name("홍길동")
                .password("1234")
                .phoneNumber("01012345678")
                .build();
        memberRepository.save(member);

        seller = Seller.builder()
                .email("goorm@test.com")
                .name("구름")
                .password("1234")
                .phoneNumber("01012345678")
                .build();
        sellerRepository.save(seller);

        Brand brand = Brand.builder()
                .seller(seller)
                .brandCode(1L)
                .name("사과나라")
                .description("싱싱한 사과나라")
                .logoUrl("apple.png")
                .build();
        brandRepository.save(brand);

        Product product = Product.builder()
                .no(1L)
                .name("사과")
                .productInfo("싱싱하고 맛있는 사과")
                .productStatus(SALE)
                .productNotice(List.of())
                .brand(brand)
                .shortDescription("맛난 사과")
                .build();
        productRepository.save(product);

        productDetail = ProductDetail.builder()
                .no(1L)
                .product(product)
                .name("사과")
                .initialQuantity(1)
                .build();
        productDetailRepository.save(productDetail);
    }

    @DisplayName("결제 상태와 정산 여부에 따라 정산 데이터 생성 여부를 결정한다.")
    @ParameterizedTest
    @CsvSource({
            "APPROVED, false, 1",   // 승인 완료, 미정산 (O)
            "READY, false, 0",      // 승인 전, 미정산 (X)
            "APPROVED, true, 0",    // 승인 완료, 정산 (X)
            "REFUNDED, false, 0",   // 환불, 미정산 (X)
    })
    void createSettlementWithApprovedAndNoSettledPayments(PaymentStatus status, boolean isSettled, int expectedCount) {
        // given
        Payment payment = createPayment(status, isSettled);
        paymentRepository.save(payment);

        // when
        settlementService.aggregateApprovedPayments();

        // then
        assertThat(settlementRepository.count()).isEqualTo(expectedCount);

    }

    @DisplayName("정산 테이블로 적재된 원본 결제 데이터는 정산 완료 상태로 업데이트 된다.")
    @Test
    void updateOriginPaymentIsSettledToTrue() {
        // given
        Payment payment = createPayment(APPROVED, false);
        paymentRepository.save(payment);

        // when
        settlementService.aggregateApprovedPayments();

        // then
        Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(updatedPayment.isSettled()).isTrue();

    }

    @DisplayName("정합성: 정산 데이터 생성 시 수수료와 최종 정산 금액이 정확히 계산되어야 한다.")
    @Test
    void calculateFeeAndSettlementAmount() {
        // given
        Payment payment = createPayment(APPROVED, false);
        paymentRepository.save(payment);

        // when
        settlementService.aggregateApprovedPayments();

        // then
        Optional<Settlement> settlement = settlementRepository.findByPayment(payment);
        assertThat(settlement)
                .isPresent()
                .hasValueSatisfying(res -> {
                    assertThat(res.getPaymentAmount()).isEqualTo(100000L);
                    assertThat(res.getFee()).isEqualByComparingTo("10000.00");
                    assertThat(res.getSettlementAmount()).isEqualByComparingTo("90000.00");
                });
    }

    @DisplayName("환불 동기화: 이미 정산된 결제가 환불되면 정산 테이블의 환불 금액과 상태가 업데이트되어야 한다.")
    @Test
    void updateRefundAmountWhenPaymentRefunded() {
        // given
        Payment payment = createPayment(REFUNDED, true);
        paymentRepository.save(payment);
        Settlement settlement = createSettlement(payment);
        settlementRepository.save(settlement);

        // when
        settlementService.syncRefundedPayments();

        // then
        Settlement result = settlementRepository.findById(settlement.getId()).orElseThrow();
        assertThat(result)
                .extracting("refundAmount", "settlementPaymentStatus", "settlementAmount")
                .contains(new BigDecimal("100000"), REFUND, new BigDecimal("90000.0"));

    }


    private Payment createPayment(PaymentStatus status, boolean isSettled){

        Order order = Order.builder()
                .member(member)
                .totalAmount(100000L)
                .orderStatus(COMPLETED)
                .build();
        orderRepository.save(order);

        OrderProduct orderProduct = OrderProduct.builder()
                .price(100000)
                .productDetail(productDetail)
                .order(order)
                .quantity(1)
                .build();
        orderProductRepository.save(orderProduct);

        // order에 orderProduct 추가
        order.addOrderProduct(orderProduct);

        return Payment.builder()
                .order(order)
                .amount(100000L)
                .paymentMethod(POINT)
                .paymentStatus(status)
                .isSettled(isSettled)
                .build();
    }

    private Settlement createSettlement(Payment payment){
        Long paymentAmount = payment.getAmount();
        BigDecimal feeRate = BigDecimal.valueOf(10.00);
        BigDecimal fee = BigDecimal.valueOf(paymentAmount).multiply(feeRate).divide(new BigDecimal("100"));
        return Settlement.builder()
                .seller(seller)
                .payment(payment)
                .paymentAmount(paymentAmount)
                .feeRate(feeRate)
                .fee(fee)
                .settlementAmount(BigDecimal.valueOf(paymentAmount).subtract(fee))
                .status(UNSETTLED)
                .settlementPaymentStatus(PAYMENT)
                .refundAmount(BigDecimal.ZERO)
                .build();
    }

}
