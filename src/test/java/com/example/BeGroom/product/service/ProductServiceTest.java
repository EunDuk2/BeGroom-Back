package com.example.BeGroom.product.service;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.domain.Role;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.product.domain.Brand;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductStatus;
import com.example.BeGroom.product.dto.BrandFilterResponse;
import com.example.BeGroom.product.dto.ProductDetailResponse;
import com.example.BeGroom.product.dto.ProductListResponse;
import com.example.BeGroom.product.dto.ProductSearchCondition;
import com.example.BeGroom.product.repository.*;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.repository.SellerRepository;
import com.example.BeGroom.wishlist.domain.Wishlist;
import com.example.BeGroom.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest extends IntegrationTestSupport {

    @Autowired
    private ProductService productService;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private ProductPriceRepository productPriceRepository;
    @Autowired
    private ProductDetailRepository productDetailRepository;
    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private ProductCategoryRepository productCategoryRepository;
    @Autowired
    private ProductOptionMappingRepository productOptionMappingRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired
    private MemberNotificationRepository memberNotificationRepository;

    private Seller seller;

    @BeforeEach
    void setUp() {
        seller = createSeller();
    }

    @AfterEach
    void tearDown() {
        stockRepository.deleteAllInBatch();
        productPriceRepository.deleteAllInBatch();
        wishlistRepository.deleteAllInBatch();
        productOptionMappingRepository.deleteAllInBatch();
        productCategoryRepository.deleteAllInBatch();
        productDetailRepository.deleteAllInBatch();
        productImageRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        brandRepository.deleteAllInBatch();
        memberNotificationRepository.deleteAllInBatch();
        sellerRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("키워드 검색 시 해당하는 상품들의 브랜드 목록을 반환한다.")
    @Test
    void getBrandFiltersByKeyword() {
        // given
        Brand brand1 = createBrand("구름", 1L);
        Brand brand2 = createBrand("하늘", 2L);
        Brand brand3 = createBrand("바다", 3L);

        createProduct("강아지 옷", 1L, brand1);
        createProduct("강아지 간식", 2L, brand1);
        createProduct("강아지 장난감", 3L, brand2);
        createProduct("고양이 옷", 4L, brand3);

        ProductSearchCondition condition = new ProductSearchCondition();
        condition.setKeyword("강아지");

        // when
        List<BrandFilterResponse> result = productService.getBrandFilters(condition);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting("brandName", "productCount")
            .containsExactlyInAnyOrder(
                tuple("구름", 2L),
                tuple("하늘", 1L)
            );

    }

    @DisplayName("회원이 상품을 검색하면 위시리스트 여부가 포함된 결과를 반환한다.")
    @Test
    void searchProductsWithWishlist() {
        // given
        Member member = createMember();
        Brand brand = createBrand("구름", 1L);

        Product product1 = createProduct("바밤바", 1L, brand);
        Product product2 = createProduct("수박바", 2L, brand);

        addToWishlist(member, product1);

        ProductSearchCondition condition = new ProductSearchCondition();

        // when
        Page<ProductListResponse> result = productService.searchProducts(condition, PageRequest.of(0, 10), member.getId());

        // then
        assertThat(result.getContent()).hasSize(2);
        ProductListResponse wishlistedProduct = result.getContent().stream()
            .filter(p -> p.getName().equals("바밤바"))
            .findFirst()
            .orElseThrow();
        ProductListResponse notWishlistedProduct = result.getContent().stream()
            .filter(p -> p.getName().equals("수박바"))
            .findFirst()
            .orElseThrow();

        assertThat(wishlistedProduct.getIsWishlisted()).isTrue();
        assertThat(notWishlistedProduct.getIsWishlisted()).isFalse();

    }

    @DisplayName("비회원이 상품을 검색하면 모든 위시리스트 여부가 false다.")
    @Test
    void searchProductsAsGuest() {
        // given
        Brand brand = createBrand("구름", 1L);
        createProduct("바밤바", 1L, brand);
        createProduct("수박바", 2L, brand);

        ProductSearchCondition condition = new ProductSearchCondition();

        // when
        Page<ProductListResponse> result = productService.searchProducts(condition, PageRequest.of(0, 10), null);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .extracting("isWishlisted")
            .containsOnly(false);

    }

    @DisplayName("여러 상품을 위시리스트에 추가한 회원이 상품 검색 시 모두 표시된다.")
    @Test
    void searchProductsWithMultipleWishlists() {
        // given
        Member member = createMember();
        Brand brand = createBrand("구름", 1L);

        Product product1 = createProduct("바밤바", 1L, brand);
        Product product2 = createProduct("수박바", 2L, brand);
        Product product3 = createProduct("누가바", 3L, brand);

        addToWishlist(member, product1);
        addToWishlist(member, product2);

        ProductSearchCondition condition = new ProductSearchCondition();

        // when
        Page<ProductListResponse> result = productService.searchProducts(condition, PageRequest.of(0, 10), member.getId());

        // then
        assertThat(result.getContent()).hasSize(3);
        long wishlistedCount = result.getContent().stream()
            .filter(ProductListResponse::getIsWishlisted)
            .count();

        assertThat(wishlistedCount).isEqualTo(2);
    }

    @DisplayName("키워드로 상품을 검색할 수 있다.")
    @Test
    void searchProductsByKeyword() {
        // given
        Brand brand = createBrand("구름", 1L);
        createProduct("바밤바", 1L, brand);
        createProduct("수박바", 2L, brand);
        createProduct("누가바", 3L, brand);

        ProductSearchCondition condition = new ProductSearchCondition();
        condition.setKeyword("바");

        // when
        Page<ProductListResponse> result = productService.searchProducts(condition, PageRequest.of(0, 10), null);

        // then
        assertThat(result.getContent()).hasSize(3)
            .extracting("name")
            .containsExactlyInAnyOrder("바밤바", "수박바", "누가바");

    }

    @DisplayName("상품 상세 조회 시 정상적으로 상품 정보를 반환한다.")
    @Test
    void getProductDetail() {
        // given
        Member member = createMember();
        Brand brand = createBrand("구름", 1L);
        Product product = createProduct("바밤바", 1L, brand);
        addToWishlist(member, product);

        // when
        ProductDetailResponse result = productService.getProductDetail(product.getId(), member.getId());

        // then
        assertThat(result.getName()).isEqualTo("바밤바");
        assertThat(result.getIsWishlisted()).isTrue();

    }

    @DisplayName("비회원이 상품 상세 조회 시 위시리스트 여부는 false다.")
    @Test
    void getProductDetailAsGuest() {
        // given
        Brand brand = createBrand("구름", 1L);
        Product product = createProduct("바밤바", 1L, brand);

        // when
        ProductDetailResponse result = productService.getProductDetail(product.getId(), null);

        // then
        assertThat(result.getName()).isEqualTo("바밤바");
        assertThat(result.getIsWishlisted()).isFalse();

    }

    @DisplayName("존재하지 않는 상품 조회 시 예외가 발생한다.")
    @Test
    void getProductDetailNotFound() {
        // given
        Long nonExistId = 99L;

        // when // then
        assertThatThrownBy(() -> productService.getProductDetail(nonExistId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("상품을 찾을 수 없습니다.");

    }

    @DisplayName("삭제된 상품 조회 시 예외가 발생한다.")
    @Test
    void getDeletedProduct() {
        // given
        Brand brand = createBrand("구름", 1L);
        Product product = createProduct("바밤바", 1L, brand);
        product.delete();
        productRepository.save(product);

        // when // then
        assertThatThrownBy(() -> productService.getProductDetail(product.getId(), null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("삭제된 상품입니다.");

    }

    @DisplayName("판매 중지된 상품 조회 시 예외가 발생한다.")
    @Test
    void getStoppedProduct() {
        // given
        Brand brand = createBrand("구름", 1L);
        Product product = Product.builder()
            .no(1L)
            .name("바밤바")
            .brand(brand)
            .productStatus(ProductStatus.STOP)
            .build();
        productRepository.save(product);

        // when // then
        assertThatThrownBy(() -> productService.getProductDetail(product.getId(), null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("판매 중지된 상품입니다.");

    }

    @DisplayName("페이징이 정상적으로 동작한다.")
    @Test
    void searchProductWithPaging() {
        // given
        Brand brand = createBrand("구름", 1L);
        for (int i = 1; i <= 25; i++) {
            createProduct("상품" + i, (long) i, brand);
        }

        ProductSearchCondition condition = new ProductSearchCondition();

        // when
        Page<ProductListResponse> page1 = productService.searchProducts(condition, PageRequest.of(0, 10), null);
        Page<ProductListResponse> page2 = productService.searchProducts(condition, PageRequest.of(1, 10), null);

        // then
        assertThat(page1.getContent()).hasSize(10);
        assertThat(page2.getContent()).hasSize(10);
        assertThat(page1.getTotalElements()).isEqualTo(25);
        assertThat(page1.getTotalPages()).isEqualTo(3);

    }

    private Member createMember() {
        Member member = Member.createMember(
            "test@naver.com",
            "test",
            "1234",
            "010",
            Role.USER
        );
        return memberRepository.save(member);
    }

    private Seller createSeller() {
        return sellerRepository.save(Seller.createSeller(
                "seller@naver.com",
                "seller",
                "1234",
                "01012341234"
            )
        );
    }

    private Brand createBrand(String name, Long code) {
        return brandRepository.save(Brand.builder()
            .seller(seller)
            .name(name)
            .brandCode(code)
            .build()
        );
    }

    private Product createProduct(String name, Long no, Brand brand) {
        return productRepository.save(Product.builder()
            .no(no)
            .name(name)
            .brand(brand)
            .productStatus(ProductStatus.SALE)
            .build());
    }

    private void addToWishlist(Member member, Product product) {
        wishlistRepository.save(
            Wishlist.builder()
                .member(member)
                .product(product)
                .build()
        );
    }
}