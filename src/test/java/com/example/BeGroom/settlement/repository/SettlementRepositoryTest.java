package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.domain.OrderStatus;
import com.example.BeGroom.order.repository.OrderProductRepository;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.product.domain.Brand;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductDetail;
import com.example.BeGroom.product.repository.BrandRepository;
import com.example.BeGroom.product.repository.ProductDetailRepository;
import com.example.BeGroom.product.repository.ProductRepository;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.dto.res.RecentPaymentResDto;
import com.example.BeGroom.seller.dto.res.RecentRefundResDto;
import com.example.BeGroom.seller.dto.res.RecentSettlementResDto;
import com.example.BeGroom.seller.repository.SellerRepository;
import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.domain.SettlementStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.BeGroom.order.domain.OrderStatus.COMPLETED;
import static com.example.BeGroom.payment.domain.PaymentMethod.POINT;
import static com.example.BeGroom.payment.domain.PaymentStatus.*;
import static com.example.BeGroom.product.domain.ProductStatus.SALE;
import static com.example.BeGroom.seller.dto.res.RecentActivityResDto.*;
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
    private SellerRepository sellerRepository;
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
    private SettlementRepository settlementRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private EntityManager em;

    private Member member;
    private Seller seller;
    private ProductDetail productDetail;

//    @AfterEach
//    void tearDown(){
//        settlementRepository.deleteAllInBatch();
//        orderProductRepository.deleteAllInBatch();
//
//        paymentRepository.deleteAllInBatch();
//        productDetailRepository.deleteAllInBatch();
//
//        orderRepository.deleteAllInBatch();
//        productRepository.deleteAllInBatch();
//
//        brandRepository.deleteAllInBatch();
//        sellerRepository.deleteAllInBatch();
//        memberRepository.deleteAllInBatch();
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

    @DisplayName("조회 기간에 따른 건별 정산 내역을 조회한다.")
    @ParameterizedTest
    @CsvSource({
            ", , 5",                    // 1. 전체 조회 (5건)
            ", 2026-01-26, 4",          // 2. ~26일 23:59:59 (4건)
            "2026-01-01, , 3",          // 3. 1일 00:00:00 ~ (3건)
            "2026-01-01, 2026-01-26, 2" // 4. 기간 내 조회 (2건)
    })
    void SearchSettlementByPeriod() {
        // given

        // when

        // then

    }

    @DisplayName("특정 셀러의 주문건 중 가장 최근 데이터 1건을 조회한다.")
    @Test
    void findLatestOrderBySeller() {
        // given
        Payment oldOrder = createPayment(APPROVED, null);
        Payment latestOrder = createPayment(APPROVED, null);
        Payment cancleOrder = createPayment(CANCELED, null);
        paymentRepository.saveAll(List.of(oldOrder, latestOrder, cancleOrder));

        ReflectionTestUtils.setField(oldOrder, "approvedAt", LocalDateTime.now().minusDays(3));
        ReflectionTestUtils.setField(latestOrder, "approvedAt", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(cancleOrder, "approvedAt", LocalDateTime.now());

        // when
        List<RecentPaymentResDto> result = paymentRepository.findLatestOrderBySellerId(seller.getId(), PageRequest.of(0, 1));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getPaymentId()).isEqualTo(latestOrder.getId());

    }

    @DisplayName("특정 셀러의 결제 환불건 중 가장 최근 데이터 1건을 조회한다.")
    @Test
    void findLatestRefundBySeller() {
        // given
        Payment payment1 = createPayment(REFUNDED, LocalDateTime.now().minusDays(5));
        Payment payment2 = createPayment(REFUNDED, LocalDateTime.now().minusDays(3));
        Payment payment3 = createPayment(REFUNDED, LocalDateTime.now());
        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        Settlement oldSettlement = createSettlement(seller, payment1, SETTLED, LocalDateTime.now().minusDays(4));
        Settlement latestSettlement = createSettlement(seller, payment2, SETTLED, LocalDateTime.now().minusDays(2));
        Settlement unsettled = createSettlement(seller, payment3, UNSETTLED, LocalDateTime.now().minusDays(1));
        settlementRepository.saveAll(List.of(oldSettlement, latestSettlement, unsettled));

        // when
        List<RecentRefundResDto> result = paymentRepository.findLatestRefundBySellerId(seller.getId(), REFUNDED, PageRequest.of(0, 1));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPaymentId()).isEqualTo(payment3.getId());

    }

    @DisplayName("특정 셀러의 정산 완료건 중 가장 최근 데이터 1건을 조회한다.")
    @Test
    void findLatestSettlementBySeller() {
        // given
        Payment payment1 = createPayment(APPROVED, LocalDateTime.now().minusDays(5));
        Payment payment2 = createPayment(APPROVED, LocalDateTime.now().minusDays(3));
        Payment payment3 = createPayment(APPROVED, LocalDateTime.now());
        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        Settlement oldSettlement = createSettlement(seller, payment1, SETTLED, LocalDateTime.now().minusDays(4));
        Settlement latestSettlement = createSettlement(seller, payment2, SETTLED, LocalDateTime.now().minusDays(2));
        Settlement unsettled = createSettlement(seller, payment3, UNSETTLED, LocalDateTime.now().minusDays(1));
        settlementRepository.saveAll(List.of(oldSettlement, latestSettlement, unsettled));

        // when
        List<RecentSettlementResDto> result = settlementRepository.findLatestSettlementBySellerId(seller.getId(), PageRequest.of(0, 1));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSettlementId()).isEqualTo(latestSettlement.getId());

    }

    private Payment createPayment(PaymentStatus status, LocalDateTime pastDate){

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
                .paymentStatus(status)
                .isSettled(true)
                .build();

        // 생성 시각 강제 주입
        ReflectionTestUtils.setField(payment, "createdAt", pastDate);

        return payment;
    }

    private Settlement createSettlement(Seller seller, Payment payment, SettlementStatus status, LocalDateTime pastDate){
        Long paymentAmount = payment.getAmount();
        BigDecimal feeRate = BigDecimal.valueOf(10.00);
        BigDecimal fee = BigDecimal.valueOf(paymentAmount).multiply(feeRate).divide(new BigDecimal("100"));
        Settlement settlement = Settlement.builder()
                .seller(seller)
                .payment(payment)
                .paymentAmount(paymentAmount)
                .feeRate(feeRate)
                .fee(fee)
                .settlementAmount(BigDecimal.valueOf(paymentAmount).subtract(fee))
                .status(status)
                .settlementPaymentStatus(PAYMENT)
                .refundAmount(BigDecimal.ZERO)
                .build();

        // 생성 시각 강제 주입
        ReflectionTestUtils.setField(settlement, "createdAt", pastDate);

        return settlement;
    }
}
