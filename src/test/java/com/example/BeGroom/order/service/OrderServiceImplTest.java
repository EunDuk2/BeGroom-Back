package com.example.BeGroom.order.service;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.domain.Role;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.domain.OrderStatus;
import com.example.BeGroom.order.dto.OrderCreateReqDto;
import com.example.BeGroom.order.dto.OrderProductReqDto;
import com.example.BeGroom.order.dto.checkout.CheckoutResDto;
import com.example.BeGroom.order.dto.checkout.CheckoutStatus;
import com.example.BeGroom.order.repository.OrderProductRepository;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentMethod;
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
import com.example.BeGroom.wallet.domain.Wallet;
import com.example.BeGroom.wallet.repository.WalletRepository;
import com.example.BeGroom.wallet.repository.WalletTransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.BeGroom.order.dto.checkout.CheckoutStatus.COMPLETED;
import static com.example.BeGroom.payment.domain.PaymentMethod.POINT;
import static com.example.BeGroom.payment.domain.PaymentStatus.APPROVED;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class OrderServiceImplTest extends IntegrationTestSupport {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductDetailRepository productDetailRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderProductRepository orderProductRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private EntityManager em;


    /* =========================
     *  주문 생성
     * ========================= */

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("정상적인 요청이면 주문이 생성되고 저장된다")
        void create_success() {
            // given
            Member member = createAndSaveMember();

            Product product1 = createAndSaveProductHierarchy(1L, "1");
            Product product2 = createAndSaveProductHierarchy(2L, "2");

            ProductDetail productDetail1 = createAndSaveProductDetail(product1, 1L, 3000, 5);
            ProductDetail productDetail2 = createAndSaveProductDetail(product2, 2L, 5000, 5);

            OrderCreateReqDto orderCreateReqDto =
                    createOrderCreateReqDto(
                            productDetail1, 1,
                            productDetail2, 2
                    );

            // when
            Order order = orderService.create(member.getId(), orderCreateReqDto);

            // then
            assertThat(order.getId()).isNotNull();
            assertThat(order.getMember().getId()).isEqualTo(member.getId());
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(order.getTotalAmount()).isEqualTo(13000);

            assertThat(order.getOrderProductList()).hasSize(2)
                    .extracting(
                            op -> op.getProductDetail().getId(),
                            OrderProduct::getQuantity,
                            OrderProduct::getPrice
                    )
                    .containsExactlyInAnyOrder(
                            tuple(productDetail1.getId(), 1, 3000),
                            tuple(productDetail2.getId(), 2, 5000)
                    );

            // 실제 저장 여부
            assertThat(orderRepository.findById(order.getId())).isPresent();
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 주문 생성에 실패한다")
        void fail_when_member_not_found() {
            // given
            Product product = createAndSaveProductHierarchy(1L, "1");
            ProductDetail productDetail = createAndSaveProductDetail(product, 1L, 3000, 5);

            OrderCreateReqDto orderCreateReqDto =
                    createOrderCreateReqDto(
                            productDetail, 1,
                            productDetail, 2
                    );

            Long invalidMemberId = 9999L;

            // when then
            assertThatThrownBy(() -> orderService.create(invalidMemberId, orderCreateReqDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("없는 사용자입니다.");
        }

        @Test
        @DisplayName("존재하지 않는 상품 옵션이 포함되면 주문 생성에 실패한다")
        void fail_when_product_detail_not_found() {
            // given
            Member member = createAndSaveMember();

            OrderCreateReqDto orderCreateReqDto =
                    new OrderCreateReqDto(
                            List.of(
                                    new OrderProductReqDto(9999L, 1)
                            )
                    );

            // when then
            assertThatThrownBy(() -> orderService.create(member.getId(), orderCreateReqDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("없는 상품 옵션입니다.");
        }
    }


    @DisplayName("주문에 대해 체크아웃을 수행하면 결제가 승인되고 주문이 완료된다")
    @Test
    void checkout_success() {
        // given
        Member member = createAndSaveMember();
        createAndSaveWallet(member);

        Product product1 = createAndSaveProductHierarchy(1L, "1");
        Product product2 = createAndSaveProductHierarchy(2L, "2");

        ProductDetail productDetail1 = createAndSaveProductDetail(product1, 1L, 3000, 5);
        ProductDetail productDetail2 = createAndSaveProductDetail(product2, 2L, 5000, 5);

        OrderCreateReqDto orderCreateReqDto =
                createOrderCreateReqDto(
                        productDetail1, 1,
                        productDetail2, 2
                );

        Order order = orderService.create(member.getId(), orderCreateReqDto);

        // when
        CheckoutResDto resDto = orderService.checkout(member.getId(), order.getId(), POINT);

        // then
        assertThat(resDto.getOrderId()).isEqualTo(1L);
        assertThat(resDto.getPaymentId()).isEqualTo(1L);
        assertThat(resDto.getCheckoutStatus()).isEqualTo(COMPLETED);

        Payment payment = paymentRepository.findById(1L).get();
        assertThat(payment.getPaymentStatus()).isEqualTo(APPROVED);
        assertThat(payment.getAmount()).isEqualTo(13000);

        Order completedOrder = orderRepository.findById(1L).get();
        assertThat(completedOrder.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(completedOrder.getTotalAmount()).isEqualTo(13000);
    }


    /* =========================
     *  외부 성능 테스트
     * ========================= */

    @Test
    @DisplayName("상품 개수 증가에 따른 findById vs findAllByIdIn 비교")
    @Disabled("성능 비교용 실험 테스트")
    void findById_vs_findAllByIdIn_by_product_count() {
        Member member = createAndSaveMember();
        Product product = createAndSaveProductHierarchy(1L, "1");

        int[] productCounts = {1, 5, 10, 50, 100, 500};

        for (int count : productCounts) {

            // given: 상품 count개 생성
            List<ProductDetail> saved = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                ProductDetail pd = createAndSaveProductDetail(product, (long) i, 3000, 5);
                saved.add(productDetailRepository.save(pd));
            }

            List<Long> ids = saved.stream()
                    .map(ProductDetail::getId)
                    .toList();

        /* ===============================
           findById
         =============================== */
            em.clear();
            long startFindById = System.nanoTime();

            for (Long id : ids) {
                productDetailRepository.findById(id).orElseThrow();
            }

            long endFindById = System.nanoTime();

        /* ===============================
           findAllByIdIn
         =============================== */
            em.clear();
            long startFindAll = System.nanoTime();

            productDetailRepository.findAllByIdIn(ids);

            long endFindAll = System.nanoTime();

            System.out.println("""
            상품 개수: %d
            - findById 시간     : %d ns
            - findAllByIdIn 시간: %d ns
            --------------------------------
            """.formatted(
                    count,
                    endFindById - startFindById,
                    endFindAll - startFindAll
            ));
        }
    }



    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("동시에 주문 체크아웃을 시도하면 어떤 결과가 나오는지 확인한다")
    @Test
    void checkout_concurrent_observe() throws InterruptedException {
        // given
        Member member = createAndSaveMember();
        createAndSaveWallet(member);

        Product product = createAndSaveProductHierarchy(1L, "1");

        // 재고 충분히 줌
        ProductDetail productDetail1 = createAndSaveProductDetail(product, 1L, 3000, 5);
        ProductDetail productDetail2 = createAndSaveProductDetail(product, 2L, 5000, 5);

        OrderCreateReqDto orderCreateReqDto =
                createOrderCreateReqDto(
                        productDetail1, 1,
                        productDetail2, 2
                );

        Order order = orderService.create(member.getId(), orderCreateReqDto);

        int threadCount = 2;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // 동시에 출발
                    orderService.checkout(member.getId(), order.getId(), POINT);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("=== test === checkout 실패: " + e.getClass().getSimpleName()
                            + " - " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();   // 동시에 시작
        doneLatch.await();        // 모두 종료 대기

        // then (일단은 관찰 위주)
        System.out.println("=== test === 성공한 결제 수: " + successCount.get());
        System.out.println("=== test === 실패한 결제 수: " + failCount.get());

        List<Payment> payments = paymentRepository.findAll();
        Order completedOrder = orderRepository.findById(order.getId()).get();

        System.out.println("=== test === 결제 개수: " + payments.size());
        System.out.println("=== test === 주문 상태: " + completedOrder.getOrderStatus());

        ProductDetail reloaded1 =
                productDetailRepository.findById(productDetail1.getId()).get();
        ProductDetail reloaded2 =
                productDetailRepository.findById(productDetail2.getId()).get();

        System.out.println("=== test === 상품1 남은 재고: " + reloaded1.getStock().getQuantity());
        System.out.println("=== test === 상품2 남은 재고: " + reloaded2.getStock().getQuantity());
    }

    @DisplayName("동시에 (각자) 주문 생성 -> 체크아웃까지 시도하면 어떤 결과가 나오는지 관찰한다")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    void checkout_concurrent_each_thread_create_order_then_checkout_observe() throws InterruptedException {
        // given (공통 리소스)
        Member member = createAndSaveMember();
        createAndSaveWallet(member);

        Product product = createAndSaveProductHierarchy(1L, "1");

        // 재고 충분히 줌 (동시 체크아웃이 두 번 일어나도 충분한 재고)
        ProductDetail productDetail1 = createAndSaveProductDetail(product, 1L, 3000, 50);
        ProductDetail productDetail2 = createAndSaveProductDetail(product, 2L, 5000, 50);

        OrderCreateReqDto orderCreateReqDto =
                createOrderCreateReqDto(
                        productDetail1, 1,
                        productDetail2, 2
                );

        int threadCount = 2;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // 각 스레드에서 만든 orderId를 모아두기 (관찰용)
        List<Long> createdOrderIds = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executorService.submit(() -> {
                try {
                    startLatch.await(); // 동시에 출발

                    // 1) 각자 주문 생성
                    Order order = orderService.create(member.getId(), orderCreateReqDto);
                    createdOrderIds.add(order.getId());

                    System.out.println("=== test === [T" + idx + "] order 생성 완료. orderId=" + order.getId());

                    // 2) 각자 체크아웃(결제)
                    CheckoutResDto resDto = orderService.checkout(member.getId(), order.getId(), POINT);

                    System.out.println("=== test === [T" + idx + "] checkout 성공. orderId=" + resDto.getOrderId()
                            + ", paymentId=" + resDto.getPaymentId());

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("=== test === [T" + idx + "] checkout 실패: " + e.getClass().getSimpleName()
                            + " - " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 동시에 시작
        doneLatch.await();      // 모두 종료 대기
        executorService.shutdown();

        // then (관찰 위주 출력)
        System.out.println("=== test === 성공한 결제 수: " + successCount.get());
        System.out.println("=== test === 실패한 결제 수: " + failCount.get());
        System.out.println("=== test === 생성된 주문 IDs: " + createdOrderIds);

        // 전체 Payment, Order 상태 출력 (단순 관찰)
        List<Payment> payments = paymentRepository.findAll();
        System.out.println("=== test === 전체 결제 개수: " + payments.size());
        for (Payment p : payments) {
            System.out.println("=== test === payment.id: " + p.getId() + ", status=" + p.getPaymentStatus());
        }

        // 방금 생성한 주문들만 상태 출력
        for (Long orderId : createdOrderIds) {
            Order reloadedOrder = orderRepository.findById(orderId).orElseThrow();
            System.out.println("=== test === order.id: " + reloadedOrder.getId()
                    + ", orderStatus=" + reloadedOrder.getOrderStatus());
        }

        // 재고도 확인
        ProductDetail reloaded1 = productDetailRepository.findById(productDetail1.getId()).orElseThrow();
        ProductDetail reloaded2 = productDetailRepository.findById(productDetail2.getId()).orElseThrow();

        System.out.println("=== test === 상품1 남은 재고: " + reloaded1.getStock().getQuantity());
        System.out.println("=== test === 상품2 남은 재고: " + reloaded2.getStock().getQuantity());
    }


    @DisplayName("동시에 요청하지 않아도 오래된 상태로 Lost Update가 발생하는지 확인한다")
    @Test
    void checkout_lost_update_without_concurrency() throws Exception {
        // given
        Member member = createAndSaveMember();
        createAndSaveWallet(member);

        Product product = createAndSaveProductHierarchy(1L, "1");

        ProductDetail productDetail1 = createAndSaveProductDetail(product, 1L, 3000, 5);
        ProductDetail productDetail2 = createAndSaveProductDetail(product, 2L, 5000, 5);

        OrderCreateReqDto orderCreateReqDto =
                createOrderCreateReqDto(
                        productDetail1, 2,
                        productDetail2, 2
                );

        Order order = orderService.create(member.getId(), orderCreateReqDto);

        CountDownLatch aReadDone = new CountDownLatch(1);
        CountDownLatch bCommitDone = new CountDownLatch(1);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // Thread A: 먼저 읽고 멈춤
        executorService.submit(() -> {
            try {
                orderService.checkoutWithDelay(
                        member.getId(),
                        order.getId(),
                        aReadDone,
                        bCommitDone
                );
            } catch (Exception e) {
                System.out.println("Thread A 실패: " + e.getMessage());
            }
        });

        // Thread B: A가 읽은 뒤 실행
        executorService.submit(() -> {
            try {
                aReadDone.await(); // A가 조회 끝낼 때까지 대기
                orderService.checkout(member.getId(), order.getId(), POINT);
            } catch (Exception e) {
                System.out.println("Thread B 실패: " + e.getMessage());
            } finally {
                bCommitDone.countDown();
            }
        });

        Thread.sleep(1000); // 둘 다 종료 대기

        // then
        ProductDetail reloaded =
                productDetailRepository.findById(productDetail1.getId()).get();

        System.out.println("=== test === 최종 재고: " + reloaded.getStock().getQuantity());
    }





    /* =========================
     *  테스트 내부 공용 메서드 (Given)
     * ========================= */

    /* =========================
     *  Member
     * ========================= */

    private Member createAndSaveMember() {
        Member member = Member.createMember(
                "test@naver.com",
                "test",
                "1234",
                "010",
                Role.USER
        );
        return memberRepository.save(member);
    }

    /* =========================
     *  Wallet
     * ========================= */

    private Wallet createAndSaveWallet(Member member) {
        Wallet wallet = Wallet.create(member, 50000L);
        return walletRepository.save(wallet);
    }

    /* =========================
     *  Seller → Brand → Product
     * ========================= */

    private Product createAndSaveProductHierarchy(Long brandCode, String name) {
        Seller seller = Seller.createSeller(
                "seller@naver.com",
                "seller",
                "1234",
                "01012341234"
        );
        sellerRepository.save(seller);

        Brand brand = Brand.builder()
                .seller(seller)
                .brandCode(brandCode)
                .name(name)
                .build();
        brandRepository.save(brand);

        Product product = Product.builder()
                .no(brandCode)
                .brand(brand)
                .name("product1")
                .productStatus(ProductStatus.SALE)
                .build();
        return productRepository.save(product);
    }

    /* =========================
     *  ProductDetail
     * ========================= */

    private ProductDetail createAndSaveProductDetail(Product product, Long no, int price, int quantity) {
        ProductDetail productDetail = ProductDetail.builder()
                .product(product)
                .no(no)
                .name("detail")
                .initialQuantity(quantity)
                .build();

        productDetail.addPrice(price, price);
        return productDetailRepository.save(productDetail);
    }

    /* =========================
     *  OrderCreateReqDto
     * ========================= */

    private OrderCreateReqDto createOrderCreateReqDto(
            ProductDetail pd1, int qty1,
            ProductDetail pd2, int qty2
    ) {
        return new OrderCreateReqDto(
                List.of(
                        new OrderProductReqDto(pd1.getId(), qty1),
                        new OrderProductReqDto(pd2.getId(), qty2)
                )
        );
    }

}