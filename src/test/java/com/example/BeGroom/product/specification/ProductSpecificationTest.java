package com.example.BeGroom.product.specification;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.product.domain.*;
import com.example.BeGroom.product.dto.ProductSearchCondition;
import com.example.BeGroom.product.repository.*;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ProductSpecificationTest extends IntegrationTestSupport {

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
    private ProductOptionRepository productOptionRepository;

    @BeforeEach
    void setUp() {
        stockRepository.deleteAllInBatch();
        productPriceRepository.deleteAllInBatch();
        productOptionMappingRepository.deleteAllInBatch();
        productCategoryRepository.deleteAllInBatch();
        productDetailRepository.deleteAllInBatch();
        productImageRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        brandRepository.deleteAllInBatch();
        sellerRepository.deleteAllInBatch();
    }

    @DisplayName("키워드 검색 시 부분 일치 및 공백 제거 검색이 가능하다.")
    @Test
    void searchByKeyword() {
        // given
        Brand brand = createBrand("구름", 1L);
        createProduct("그릭 요거트", 1L, brand);

        ProductSearchCondition condition1 = new ProductSearchCondition();
        condition1.setKeyword("그릭요거트");

        ProductSearchCondition condition2 = new ProductSearchCondition();
        condition2.setKeyword("그릭");

        ProductSearchCondition condition3 = new ProductSearchCondition();
        condition3.setKeyword("요거트");

        ProductSearchCondition condition4 = new ProductSearchCondition();
        condition4.setKeyword("      그릭       요  거트       ");

        // when
        Page<Product> result1 = productRepository.findAllByCondition(condition1, PageRequest.of(0, 10));
        Page<Product> result2 = productRepository.findAllByCondition(condition2, PageRequest.of(0, 10));
        Page<Product> result3 = productRepository.findAllByCondition(condition3, PageRequest.of(0, 10));
        Page<Product> result4 = productRepository.findAllByCondition(condition4, PageRequest.of(0, 10));

        // then
        assertThat(result1.getContent()).hasSize(1);
        assertThat(result2.getContent()).hasSize(1);
        assertThat(result3.getContent()).hasSize(1);
        assertThat(result4.getContent()).hasSize(1);

    }

    @DisplayName("브랜드별로 상품을 필터링할 수 있다.")
    @Test
    void searchByBrandId() {
        // given
        Brand brand1 = createBrand("구름", 1L);
        Brand brand2 = createBrand("비", 2L);
        Brand brand3 = createBrand("하늘", 3L);

        createProduct("맛있는 제주산 갈치", 1L, brand1);
        createProduct("신선 꽁꽁 꽁치", 2L, brand1);
        createProduct("달콤한 고구마", 3L, brand2);
        createProduct("감자밭에서 따 온 감자", 4L, brand3);

        // when
        ProductSearchCondition condition1 = new ProductSearchCondition();
        condition1.setBrandIds(List.of(brand1.getId()));

        ProductSearchCondition condition2 = new ProductSearchCondition();
        condition2.setBrandIds(List.of(brand2.getId(), brand3.getId()));

        Page<Product> result1 = productRepository.findAllByCondition(condition1, PageRequest.of(0, 10));
        Page<Product> result2 = productRepository.findAllByCondition(condition2, PageRequest.of(0, 10));

        // then
        assertThat(result1.getContent()).hasSize(2)
            .extracting("name")
            .containsExactlyInAnyOrder("맛있는 제주산 갈치", "신선 꽁꽁 꽁치");

        assertThat(result2.getContent()).hasSize(2)
            .extracting("name")
            .containsExactlyInAnyOrder("달콤한 고구마", "감자밭에서 따 온 감자");

    }

    @DisplayName("품절 상품을 제외하고 검색할 수 있다.")
    @Test
    void searchExcludingSoldOut() {
        // given
        Brand brand = createBrand("구름", 1L);

        createProduct("맛있는 제주산 갈치", 1L, brand);
        productRepository.save(Product.builder()
            .no(2L)
            .name("신선 꽁꽁 꽁치")
            .brand(brand)
            .productStatus(ProductStatus.SOLD_OUT)
            .build());

        // when
        ProductSearchCondition condition = new ProductSearchCondition();
        condition.setExcludeSoldOut(true);

        Page<Product> result = productRepository.findAllByCondition(condition, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1)
            .extracting("name")
            .containsExactlyInAnyOrder("맛있는 제주산 갈치");

    }

    @DisplayName("배송/포장 옵션으로 상품을 필터링할 수 있다.")
    @Test
    void searchByOption() {
        // given
        Brand brand = createBrand("구름", 1L);
        Product product1 = createProduct("맛있는 제주산 갈치", 1L, brand);
        Product product2 = createProduct("신선 꽁꽁 꽁치", 2L, brand);
        Product product3 = createProduct("달콤한 고구마", 3L, brand);
        Product product4 = createProduct("감자밭에서 따 온 감자", 4L, brand);

        createProductDetailWithOptions(product1, 1L, "DAWN", "COLD");
        createProductDetailWithOptions(product2, 2L, "DAWN", "FROZEN");
        createProductDetailWithOptions(product3, 3L, "NORMAL_PARCEL", "AMBIENT_TEMPERATURE");
        createProductDetailWithOptions(product4, 4L, "NORMAL_PARCEL", "AMBIENT_TEMPERATURE");

        // when
        ProductSearchCondition condition1 = new ProductSearchCondition();
        condition1.setDeliveryTypes(List.of("NORMAL_PARCEL"));

        ProductSearchCondition condition2 = new ProductSearchCondition();
        condition2.setPackagingTypes(List.of("FROZEN"));

        ProductSearchCondition condition3 = new ProductSearchCondition();
        condition3.setDeliveryTypes(List.of("DAWN"));
        condition3.setPackagingTypes(List.of("COLD"));

        Page<Product> result1 = productRepository.findAllByCondition(condition1, PageRequest.of(0, 10));
        Page<Product> result2 = productRepository.findAllByCondition(condition2, PageRequest.of(0, 10));
        Page<Product> result3 = productRepository.findAllByCondition(condition3, PageRequest.of(0, 10));

        // then
        assertThat(result1.getContent()).hasSize(2)
            .extracting("name")
            .containsExactlyInAnyOrder("달콤한 고구마", "감자밭에서 따 온 감자");

        assertThat(result2.getContent()).hasSize(1)
            .extracting("name")
            .containsExactlyInAnyOrder("신선 꽁꽁 꽁치");

        assertThat(result3.getContent()).hasSize(1)
            .extracting("name")
            .containsExactlyInAnyOrder("맛있는 제주산 갈치");

    }

    @DisplayName("키워드와 브랜드, 옵션을 동시에 필터링할 수 있다.")
    @Test
    void searchWithMultipleConditions() {
        // given
        Brand brand1 = createBrand("구름", 1L);
        Brand brand2 = createBrand("비", 2L);

        Product product1 = createProduct("깔끔한 우유", 1L, brand1);
        Product product2 = createProduct("상큼한 딸기우유", 2L, brand1);
        Product product3 = createProduct("달달한 초코우유", 3L, brand2);
        Product product4 = createProduct("빙그레 바나나우유", 4L, brand2);

        createProductDetailWithOptions(product1, 1L, "DAWN", "COLD");
        createProductDetailWithOptions(product2, 2L, "NORMAL_PARCEL", "AMBIENT_TEMPERATURE");
        createProductDetailWithOptions(product3, 3L, "DAWN", "FROZEN");
        createProductDetailWithOptions(product4, 4L, "NORMAL_PARCEL", "AMBIENT_TEMPERATURE");

        // when
        ProductSearchCondition condition = new ProductSearchCondition();
        condition.setKeyword("우유");
        condition.setBrandIds(List.of(brand2.getId()));
        condition.setDeliveryTypes(List.of("DAWN", "NORMAL_PARCEL"));
        condition.setPackagingTypes(List.of("FROZEN", "AMBIENT_TEMPERATURE"));

        Page<Product> result = productRepository.findAllByCondition(condition, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2)
            .extracting("name")
            .containsExactlyInAnyOrder("달달한 초코우유", "빙그레 바나나우유");

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
            .seller(createSeller())
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

    private ProductDetail createProductDetail(Product product, Long no) {
        ProductDetail productDetail = ProductDetail.builder()
            .product(product)
            .no(no)
            .name("detail")
            .initialQuantity(5)
            .build();

        productDetail.addPrice(10000, 10000);
        return productDetailRepository.save(productDetail);
    }

    private void createProductDetailWithOptions(Product product, Long detailNo, String deliveryType, String packagingType) {
        ProductDetail detail = createProductDetail(product, detailNo);
        addOption(detail, "delivery", deliveryType);
        addOption(detail, "packaging", packagingType);
    }

    private void addOption(ProductDetail detail, String optionType, String optionValue) {
        ProductOption option = productOptionRepository.findByOptionTypeAndOptionValue(optionType, optionValue)
            .orElseGet(() -> productOptionRepository.save(
                ProductOption.builder()
                    .optionType(optionType)
                    .optionValue(optionValue)
                    .displayName(optionValue)
                    .build()
            ));

        productOptionMappingRepository.save(
            ProductOptionMapping.builder()
                .productDetail(detail)
                .productOption(option)
                .build()
        );
    }
}