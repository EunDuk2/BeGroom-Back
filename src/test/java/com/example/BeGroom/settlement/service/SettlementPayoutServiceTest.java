package com.example.BeGroom.settlement.service;


import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.repository.OrderProductRepository;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.product.domain.Brand;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductDetail;
import com.example.BeGroom.product.repository.BrandRepository;
import com.example.BeGroom.product.repository.ProductDetailRepository;
import com.example.BeGroom.product.repository.ProductRepository;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.repository.SellerRepository;
import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.domain.SettlementStatus;
import com.example.BeGroom.settlement.repository.SettlementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.example.BeGroom.order.domain.OrderStatus.COMPLETED;
import static com.example.BeGroom.payment.domain.PaymentMethod.POINT;
import static com.example.BeGroom.payment.domain.PaymentStatus.*;
import static com.example.BeGroom.product.domain.ProductStatus.SALE;
import static com.example.BeGroom.settlement.domain.SettlementPaymentStatus.PAYMENT;
import static com.example.BeGroom.settlement.domain.SettlementStatus.SETTLED;
import static com.example.BeGroom.settlement.domain.SettlementStatus.UNSETTLED;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class SettlementPayoutServiceTest {

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

    // todo : 정산 건이 1000건인데, 500번째에서 에러가 나면? 롤백 or 성공 건까지 저장
    // todo : settled 변경 시, 환불이 발생한다면? payment 상태 체크 고민

    @DisplayName("미정산 건들을 모두 지급 완료 상태로 변경한다.")
    @Test
    void executeSettlementPayout() {
        // given
        Settlement settlement = createSettlement(UNSETTLED, null);
        settlementRepository.save(settlement);

        // when
        settlementService.executeSettlementPayout();

        // then
        List<Settlement> result = settlementRepository.findAll();
        assertThat(result).hasSize(1)
                .allSatisfy(res -> {
                    assertThat(res.getStatus()).isEqualTo(SETTLED);
                    assertThat(res.getPayoutDate()).isNotNull();
                });

    }

    @DisplayName("이미 지급 완료된 건은 다시 처리하지 않는다.")
    @Test
    void executeSettlementPayoutWithOnlyUnsettled() {
        // given
        LocalDateTime localDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Settlement unsettled = createSettlement(UNSETTLED, null);
        Settlement settled = createSettlement(SETTLED, localDateTime);
        settlementRepository.saveAll(List.of(settled, unsettled));

        // when
        settlementService.executeSettlementPayout();

        // then
        Settlement result1 = settlementRepository.findById(unsettled.getId()).get();
        Settlement result2 = settlementRepository.findById(settled.getId()).get();

        assertThat(result1.getStatus()).isEqualTo(SETTLED);
        assertThat(result2.getPayoutDate()).isEqualTo(localDateTime);

    }


    private Settlement createSettlement(SettlementStatus status, LocalDateTime localDateTime){

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

        Payment payment = Payment.builder()
                .order(order)
                .amount(100000L)
                .paymentMethod(POINT)
                .paymentStatus(APPROVED)
                .isSettled(true)
                .build();

        paymentRepository.save(payment);

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
                .status(status)
                .settlementPaymentStatus(PAYMENT)
                .refundAmount(BigDecimal.ZERO)
                .payoutDate(localDateTime)
                .build();
    }

}
