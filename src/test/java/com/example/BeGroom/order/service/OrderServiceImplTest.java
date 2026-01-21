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
import com.example.BeGroom.order.repository.OrderProductRepository;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.product.domain.Brand;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductDetail;
import com.example.BeGroom.product.repository.BrandRepository;
import com.example.BeGroom.product.repository.ProductDetailRepository;
import com.example.BeGroom.product.repository.ProductRepository;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.repository.SellerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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

    @AfterEach
    void tearDown() {
        orderProductRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        productDetailRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        brandRepository.deleteAllInBatch();
        sellerRepository.deleteAllInBatch();
    }


    /* =========================
     *  주문 생성 성공
     * ========================= */

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

        // 실제 저장 여부 (Service 테스트에서 이 정도는 OK)
        assertThat(orderRepository.findById(order.getId())).isPresent();
    }


    /* =========================
     *  회원 조회 검증
     * ========================= */

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


    /* =========================
     *  상품 옵션 조회 검증
     * ========================= */

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