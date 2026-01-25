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
import com.example.BeGroom.settlement.domain.*;
import com.example.BeGroom.settlement.domain.id.DailySettlementId;
import com.example.BeGroom.settlement.domain.id.MonthlySettlementId;
import com.example.BeGroom.settlement.domain.id.WeeklySettlementId;
import com.example.BeGroom.settlement.domain.id.YearlySettlementId;
import com.example.BeGroom.settlement.repository.SettlementRepository;
import com.example.BeGroom.settlement.repository.daily.DailySettlementRepository;
import com.example.BeGroom.settlement.repository.monthly.MonthlySettlementRepository;
import com.example.BeGroom.settlement.repository.weekly.WeeklySettlementRepository;
import com.example.BeGroom.settlement.repository.yearly.YearlySettlementRepository;
import com.example.BeGroom.settlement.service.aggregation.AggregationService;
import com.example.BeGroom.settlement.service.aggregation.WeeklyPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static com.example.BeGroom.order.domain.OrderStatus.COMPLETED;
import static com.example.BeGroom.payment.domain.PaymentMethod.POINT;
import static com.example.BeGroom.payment.domain.PaymentStatus.APPROVED;
import static com.example.BeGroom.payment.domain.PaymentStatus.REFUNDED;
import static com.example.BeGroom.product.domain.ProductStatus.SALE;
import static com.example.BeGroom.settlement.domain.SettlementPaymentStatus.PAYMENT;
import static com.example.BeGroom.settlement.domain.SettlementPaymentStatus.REFUND;
import static com.example.BeGroom.settlement.domain.SettlementStatus.SETTLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class AggregationServiceTest {

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
    @Autowired
    private DailySettlementRepository dailySettlementRepository;
    @Autowired
    private WeeklySettlementRepository weeklySettlementRepository;
    @Autowired
    private MonthlySettlementRepository monthlySettlementRepository;
    @Autowired
    private YearlySettlementRepository yearlySettlementRepository;
    @Autowired
    private AggregationService aggregationService;

    private Seller seller;
    private Order order;

//    @AfterEach
//    void tearDown(){
//
//    }

    @BeforeEach
    void setUp(){
        Member member = Member.builder()
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

        ProductDetail productDetail = ProductDetail.builder()
                .no(1L)
                .product(product)
                .name("사과")
                .initialQuantity(1)
                .build();
        productDetailRepository.save(productDetail);

        order = Order.builder()
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
    }

    @Nested
    @DisplayName("날짜 및 기간 경계값 정합성 테스트")
    class DateBoundaryAggregation{

        @DisplayName("일간 경계: 23:59:59와 00:00:00 데이터는 각각의 일간 레코드에 집계된다.")
        @Test
        void aggregateDailyBoundary() {
            // given
            LocalDateTime today = LocalDateTime.of(2025, 1, 25, 23, 59, 59);
            LocalDateTime tomorrow = LocalDateTime.of(2025, 1, 26, 0, 0, 0);

            Settlement settlement1 = createSettlementWithPayoutDate(today, SETTLED, PAYMENT, false);
            Settlement settlement2 = createSettlementWithPayoutDate(tomorrow, SETTLED, PAYMENT, false);
            settlementRepository.saveAll(List.of(settlement1, settlement2));

            // when
            aggregationService.aggregate();

            // then
            DailySettlement day25 = dailySettlementRepository.findById(new DailySettlementId(today.toLocalDate(), seller.getId())).get();
            DailySettlement day26 = dailySettlementRepository.findById(new DailySettlementId(tomorrow.toLocalDate(), seller.getId())).get();

            assertThat(day25.getId().getDate()).isEqualTo(LocalDate.of(2025, 1, 25));
            assertThat(day26.getId().getDate()).isEqualTo(LocalDate.of(2025, 1, 26));

        }

        @DisplayName("주간 경계: 주차가 바뀌는 시점의 데이터가 각각의 주간 레코드에 집계된다.")
        @Test
        void aggregateWeeklyBoundary() {
            // given : 2026/1/25(일) 밤과 2026/1/26(월) 새벽
            LocalDateTime sundayNight = LocalDateTime.of(2026, 1, 25, 23, 59, 59);
            LocalDateTime mondayMorning = LocalDateTime.of(2026, 1, 26, 0, 0, 0);

            Settlement settlement1 = createSettlementWithPayoutDate(sundayNight, SETTLED, PAYMENT, false);
            Settlement settlement2 = createSettlementWithPayoutDate(mondayMorning, SETTLED, PAYMENT, false);
            settlementRepository.saveAll(List.of(settlement1, settlement2));

            WeeklyPeriod wp1 = WeeklyPeriod.calc(sundayNight.toLocalDate());
            WeeklyPeriod wp2 = WeeklyPeriod.calc(mondayMorning.toLocalDate());

            // when
            aggregationService.aggregate();

            // then
            WeeklySettlement week4 = weeklySettlementRepository.findById(
                    new WeeklySettlementId(wp1.getYear(), wp1.getMonth(), wp1.getWeek(), seller.getId())).get();
            WeeklySettlement week5 = weeklySettlementRepository.findById(
                    new WeeklySettlementId(wp2.getYear(), wp2.getMonth(), wp2.getWeek(), seller.getId())).get();

            assertThat(week4.getId())
                    .extracting("year", "month", "week")
                    .containsExactlyInAnyOrder(2026, 1, 4);
            assertThat(week5.getId())
                    .extracting("year", "month", "week")
                    .containsExactlyInAnyOrder(2026, 1, 5);

        }

        @DisplayName("월간 경계: 말일과 초일 데이터는 각각의 월간 레코드에 집계된다.")
        @Test
        void aggregateMonthlyBoundary() {
            // given : 1/31 밤과 2/1 새벽
            LocalDateTime janLast = LocalDateTime.of(2026, 1, 31, 23, 59, 59);
            LocalDateTime febFirst = LocalDateTime.of(2026, 2, 1, 0, 0, 0);

            Settlement settlement1 = createSettlementWithPayoutDate(janLast, SETTLED, PAYMENT, false);
            Settlement settlement2 = createSettlementWithPayoutDate(febFirst, SETTLED, PAYMENT, false);
            settlementRepository.saveAll(List.of(settlement1, settlement2));

            // when
            aggregationService.aggregate();

            // then
            MonthlySettlement jan = monthlySettlementRepository.findById(
                    new MonthlySettlementId(janLast.getYear(), janLast.getMonthValue(), seller.getId())).get();
            MonthlySettlement feb = monthlySettlementRepository.findById(
                    new MonthlySettlementId(febFirst.getYear(), febFirst.getMonthValue(), seller.getId())).get();

            assertThat(jan.getId())
                    .extracting("year", "month")
                    .containsExactlyInAnyOrder(2026, 1);
            assertThat(feb.getId())
                    .extracting("year", "month")
                    .containsExactlyInAnyOrder(2026, 2);

        }

        @DisplayName("연간 경계: 연말과 연초 데이터는 각각의 연간 레코드에 집계된다.")
        @Test
        void aggregateYearlyBoundary() {
            // given : 2025년 마지막과 2026 시작
            LocalDateTime decLast = LocalDateTime.of(2025, 12, 31, 23, 59, 59);
            LocalDateTime janFirst = LocalDateTime.of(2026, 1, 1, 0, 0, 0);

            Settlement settlement1 = createSettlementWithPayoutDate(decLast, SETTLED, PAYMENT, false);
            Settlement settlement2 = createSettlementWithPayoutDate(janFirst, SETTLED, PAYMENT, false);
            settlementRepository.saveAll(List.of(settlement1, settlement2));

            // when
            aggregationService.aggregate();

            // then
            YearlySettlement dec = yearlySettlementRepository.findById(
                    new YearlySettlementId(decLast.getYear(), seller.getId())).get();
            YearlySettlement jan = yearlySettlementRepository.findById(
                    new YearlySettlementId(janFirst.getYear(), seller.getId())).get();

            assertThat(dec.getId().getYear()).isEqualTo(2025);
            assertThat(jan.getId().getYear()).isEqualTo(2026);

        }

    }

    @DisplayName("지급 완료된 정산건이 모든 기간별 집계 레코드로 생성된다.")
    @Test
    void aggregateSettledPayment() {
        // given
        LocalDate localDate = LocalDate.now();
        Settlement settlement = createSettlement(
                10000L, BigDecimal.valueOf(1000), BigDecimal.valueOf(9000), BigDecimal.ZERO, SETTLED, PAYMENT);
        settlementRepository.save(settlement);

        // when
        aggregationService.aggregate();

        // then
        verifyAllPeriods(localDate, 9000L, 0L);

    }

    @DisplayName("환불된 정산건이 모든 기간별 집계 레코드에 업데이트된다.")
    @Test
    void aggregateSettledRefund() {
        // given
        LocalDate localDate = LocalDate.now();
        // insert 할 정산 데이터
        Settlement paymentSettlement = createSettlement(
                10000L, BigDecimal.valueOf(1000), BigDecimal.valueOf(9000), BigDecimal.ZERO, SETTLED, PAYMENT);
        settlementRepository.save(paymentSettlement);
        // update 할 환불 데이터
        Settlement refundSettlement = createSettlement(
                10000L, BigDecimal.valueOf(1000), BigDecimal.valueOf(9000), BigDecimal.valueOf(10000), SETTLED, REFUND);
        settlementRepository.save(refundSettlement);

        // when
        aggregationService.aggregate();

        // then
        verifyAllPeriods(localDate, 0L, 10000L);

    }


    private void verifyAllPeriods(LocalDate date, Long expectedSettle, Long expectedRefund){
        WeeklyPeriod wp = WeeklyPeriod.calc(date);
        BigDecimal settlementAmount = BigDecimal.valueOf(expectedSettle);
        BigDecimal refundAmount = BigDecimal.valueOf(expectedRefund);

        assertAll(
                // 일간 검증
                () -> {
                    DailySettlementId dailyId = new DailySettlementId(date, seller.getId());
                    DailySettlement daily = dailySettlementRepository.findById(dailyId).get();
                    assertThat(daily.getSettlementAmount()).as("Daily 정산액 불일치").isEqualByComparingTo(settlementAmount);
                    assertThat(daily.getRefundAmount()).as("Daily 환불액 불일치").isEqualByComparingTo(refundAmount);
                },
                // 주간 검증
                () -> {
                    WeeklySettlementId weeklyId = new WeeklySettlementId(wp.getYear(), wp.getMonth(), wp.getWeek(), seller.getId());
                    WeeklySettlement weekly = weeklySettlementRepository.findById(weeklyId).get();
                    assertThat(weekly.getSettlementAmount()).as("Weekly 정산액 불일치").isEqualByComparingTo(settlementAmount);
                    assertThat(weekly.getRefundAmount()).as("Weekly 환불액 불일치").isEqualByComparingTo(refundAmount);
                },
                // 월간 검증
                () -> {
                    MonthlySettlementId monthlyId = new MonthlySettlementId(date.getYear(), date.getMonthValue(), seller.getId());
                    MonthlySettlement monthly = monthlySettlementRepository.findById(monthlyId).get();
                    assertThat(monthly.getSettlementAmount()).as("Monthly 정산액 불일치").isEqualByComparingTo(settlementAmount);
                    assertThat(monthly.getRefundAmount()).as("Monthly 환불액 불일치").isEqualByComparingTo(refundAmount);
                },
                // 연간 검증
                () -> {
                    YearlySettlementId yearlyId = new YearlySettlementId(date.getYear(), seller.getId());
                    YearlySettlement yearly = yearlySettlementRepository.findById(yearlyId).get();
                    assertThat(yearly.getSettlementAmount()).as("Yearly 정산액 불일치").isEqualByComparingTo(settlementAmount);
                    assertThat(yearly.getRefundAmount()).as("Yearly 환불액 불일치").isEqualByComparingTo(refundAmount);
                }
        );
    }

    private Settlement createSettlementWithPayoutDate(
            LocalDateTime date, SettlementStatus status, SettlementPaymentStatus paymentStatus, boolean isAggregated){

        Payment payment = Payment.builder()
                .order(order)
                .amount(10000L)
                .paymentMethod(POINT)
                .paymentStatus(APPROVED)
                .isSettled(true)
                .build();

        paymentRepository.save(payment);

        Settlement settlement = Settlement.builder()
                .seller(seller)
                .payment(payment)
                .paymentAmount(10000L)
                .feeRate(BigDecimal.valueOf(10.00))
                .fee(BigDecimal.valueOf(1000.00))
                .settlementAmount(BigDecimal.valueOf(9000.00))
                .status(status)
                .settlementPaymentStatus(paymentStatus)
                .refundAmount(BigDecimal.ZERO)
                .payoutDate(date)
                .isAggregated(isAggregated)
                .build();

        ReflectionTestUtils.setField(settlement, "payoutDate", date);

        return settlement;
    }


    private Settlement createSettlement(
            Long paymentAmount, BigDecimal fee, BigDecimal amount, BigDecimal refundAmount,
            SettlementStatus status, SettlementPaymentStatus paymentStatus) {

        Payment payment = Payment.builder()
                .order(order)
                .amount(paymentAmount)
                .paymentMethod(POINT)
                .paymentStatus(APPROVED)
                .isSettled(true)
                .build();

        paymentRepository.save(payment);

        return Settlement.builder()
                .seller(seller)
                .payment(payment)
                .paymentAmount(paymentAmount)
                .feeRate(BigDecimal.valueOf(10.00))
                .fee(fee)
                .settlementAmount(amount)
                .status(status)
                .settlementPaymentStatus(paymentStatus)
                .refundAmount(refundAmount)
                .payoutDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

}
