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

import java.util.ArrayList;
import java.util.List;

import static com.example.BeGroom.order.dto.checkout.CheckoutStatus.COMPLETED;
import static com.example.BeGroom.payment.domain.PaymentMethod.POINT;
import static com.example.BeGroom.payment.domain.PaymentStatus.APPROVED;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

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

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAllInBatch();
        orderProductRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        walletTransactionRepository.deleteAllInBatch();
        walletRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        productDetailRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        brandRepository.deleteAllInBatch();
        sellerRepository.deleteAllInBatch();
    }


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

            Product product = createAndSaveProductHierarchy();

            ProductDetail productDetail1 = createAndSaveProductDetail(product, 3000, 5);
            ProductDetail productDetail2 = createAndSaveProductDetail(product, 5000, 5);

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
                            op -> op.getProductDetail().getProductDetailId(),
                            OrderProduct::getQuantity,
                            OrderProduct::getPrice
                    )
                    .containsExactlyInAnyOrder(
                            tuple(productDetail1.getProductDetailId(), 1, 3000),
                            tuple(productDetail2.getProductDetailId(), 2, 5000)
                    );

            // 실제 저장 여부
            assertThat(orderRepository.findById(order.getId())).isPresent();
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 주문 생성에 실패한다")
        void fail_when_member_not_found() {
            // given
            Product product = createAndSaveProductHierarchy();
            ProductDetail productDetail = createAndSaveProductDetail(product, 3000, 5);

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

        Product product = createAndSaveProductHierarchy();

        ProductDetail productDetail1 = createAndSaveProductDetail(product, 3000, 5);
        ProductDetail productDetail2 = createAndSaveProductDetail(product, 5000, 5);

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
        Product product = createAndSaveProductHierarchy();

        int[] productCounts = {1, 5, 10, 50, 100, 500};

        for (int count : productCounts) {

            // given: 상품 count개 생성
            List<ProductDetail> saved = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                ProductDetail pd = createAndSaveProductDetail(product, 3000, 5);
                saved.add(productDetailRepository.save(pd));
            }

            List<Long> ids = saved.stream()
                    .map(ProductDetail::getProductDetailId)
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

            productDetailRepository.findAllByProductDetailIdIn(ids);

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

    private Product createAndSaveProductHierarchy() {
        Seller seller = Seller.createSeller(
                "seller@naver.com",
                "seller",
                "1234",
                "01012341234"
        );
        sellerRepository.save(seller);

        Brand brand = Brand.builder()
                .seller(seller)
                .name("brand")
                .build();
        brandRepository.save(brand);

        Product product = Product.builder()
                .productNo(1L)
                .brand(brand)
                .name("product1")
                .salesPrice(10000)
                .build();
        return productRepository.save(product);
    }

    /* =========================
     *  ProductDetail
     * ========================= */

    private ProductDetail createAndSaveProductDetail(Product product, int price, int quantity) {
        ProductDetail productDetail = ProductDetail.builder()
                .product(product)
                .name("detail")
                .basePrice(price)
                .quantity(quantity)
                .build();
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
                        new OrderProductReqDto(pd1.getProductDetailId(), qty1),
                        new OrderProductReqDto(pd2.getProductDetailId(), qty2)
                )
        );
    }

}