package com.example.BeGroom.product.service;

import com.example.BeGroom.product.domain.*;
import com.example.BeGroom.product.dto.crawling.CrawlingRequest;
import com.example.BeGroom.product.dto.crawling.CrawlingResponse;
import com.example.BeGroom.product.dto.crawling.ProductOptionResponse;
import com.example.BeGroom.product.repository.*;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.repository.SellerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlingService {

    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionMappingRepository productOptionMappingRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final SellerRepository sellerRepository;
    private final RestTemplate restTemplate;

    private static final String API_BASE_URL = "https://api.kurly.com/collection/v2/home/sites/market";
    private static final String DETAIL_API_URL = "https://api.kurly.com/showroom/v2/products";

    // 카테고리 크롤링
    public List<Product> crawl(CrawlingRequest request) {

        List<Category> targetCategories = determineTargetCategories(request.getCategoryIds());

        if (targetCategories.isEmpty()) {
            return new ArrayList<>();
        }

        List<Product> allProducts = new ArrayList<>();

        for (Category category : targetCategories) {
            try {
                List<Product> products = crawlCategory(category.getExternalCategoryId(), request.getMaxProductsPerCategory());
                saveProductCategoryMappings(products, category.getCategoryId());
                allProducts.addAll(products);
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("카테고리 크롤링 실패 - categoryId: {}, 에러: {}",
                        category.getCategoryId(), e.getMessage());
                continue;
            }
        }

        return allProducts;
    }

    private List<Category> determineTargetCategories(List<Long> categoryIds) {

        if (categoryIds == null || categoryIds.isEmpty()) {
            return categoryRepository.findByLevelAndIsActiveOrderBySortOrderAsc(2, true);
        }

        List<Category> targetCategories = new ArrayList<>();
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));

            if (category.getLevel() == 1) {
                List<Category> subCategories = categoryRepository.findByParent_CategoryIdAndIsActiveOrderBySortOrderAsc(category.getCategoryId(), true);
                targetCategories.addAll(subCategories);
            } else if (category.getLevel() == 2) {
                targetCategories.add(category);
            } else {
                throw new IllegalArgumentException("지원하지 않는 카테고리 레벨: " + category.getLevel());
            }
        }

        return targetCategories;
    }

    // 단일 카테고리 크롤링 (내부 메서드)
    public List<Product> crawlCategory(String categoryId, int maxProducts) {

        List<Product> savedProducts = new ArrayList<>();
        int page = 1;
        int perPage = 96;

        while (savedProducts.size() < maxProducts) {
            try {
                // API URL 생성
                String url = String.format(
                        "%s/product-categories/%s/products?sort_type=4&page=%d&per_page=%d&filters=",
                        API_BASE_URL, categoryId, page, perPage
                );

                // API 호출
                CrawlingResponse response = restTemplate.getForObject(url, CrawlingResponse.class);

                if (response == null || response.getData() == null || response.getData().isEmpty()) {
                    break;
                }

                // 상품 저장
                for (CrawlingResponse.ProductData productData : response.getData()) {
                    if (savedProducts.size() >= maxProducts) {
                        break;
                    }

                    try {
                        Product product = saveProductInNewTransaction(productData);
                        savedProducts.add(product);
                    } catch (Exception e) {
                        log.error("상품 저장 실패 - productNo: {}, 이름: {}, 에러: {}",
                                productData.getNo(), productData.getName(), e.getMessage());
                        continue;
                    }
                }

                page++;
                Thread.sleep(1000); // API 요청 간격 (1초)

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("API 호출 실패 - categoryId: {}, page: {}, 에러: {}",
                        categoryId, page, e.getMessage());
                break;
            }
        }

        return savedProducts;
    }

    // 새 트랜잭션으로 상품 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Product saveProductInNewTransaction(CrawlingResponse.ProductData productData) {
        return saveProduct(productData);
    }

    // 상품-카테고리 매핑 일괄 저장 (중복 상품의 경우 카테고리만 추가)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductCategoryMappings(List<Product> products, Long categoryId) {

        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) return;

        for (Product product : products) {
            try {
                // 중복 체크: 이미 매핑된 카테고리인지 확인
                boolean exists = productCategoryRepository.existsByProduct_ProductIdAndCategory_CategoryId(
                        product.getProductId(),
                        categoryId
                );

                if (exists) {
                    log.debug("이미 매핑된 카테고리 스킵 - productId: {}, categoryId: {}",
                            product.getProductId(), categoryId);
                    continue;
                }

                // 첫 번째 카테고리인지 확인 (대표 카테고리 결정)
                long existingCount = productCategoryRepository.countByProduct_ProductId(product.getProductId());
                boolean isPrimary = (existingCount == 0);  // 첫 번째 매핑이면 대표 카테고리

                // 새 카테고리 매핑 추가
                ProductCategory productCategory = ProductCategory.builder()
                        .product(product)
                        .category(category)
                        .isPrimary(isPrimary)
                        .build();

                productCategoryRepository.save(productCategory);

                log.info("카테고리 매핑 추가 - productId: {}, categoryId: {}, isPrimary: {}",
                        product.getProductId(), categoryId, isPrimary);

            } catch (Exception e) {
                log.error("카테고리 매핑 실패 - productId: {}, categoryId: {}, 에러: {}",
                        product.getProductId(), categoryId, e.getMessage());
            }
        }
    }

    // 상품 데이터 DB에 저장
    private Product saveProduct(CrawlingResponse.ProductData productData) {

        Optional<Product> existing = productRepository.findByProductNo(productData.getNo());
        if (existing.isPresent()) {
            log.info("중복 크롤링 스킵 - productNo: {}", productData.getNo());
            return existing.get(); // 이미 있으면 그냥 반환 (중복 저장 안함)
        }

        Product.ProductStatus status = determineProductStatus(productData);

        Brand brand = getBrandFromApi(productData.getNo());
        if (brand == null) {
            brand = brandRepository.findById(1L).orElse(null);
        }

        // Product 저장
        Product product = Product.builder()
                .productNo(productData.getNo())
                .brand(brand)
                .name(productData.getName())
                .shortDescription(productData.getShortDescription())
                .salesPrice(productData.getSafeSalesPrice())
                .discountedPrice(productData.getDiscountedPrice())
                .discountRate(productData.getDiscountRate())
                .isBuyNow(productData.getIsBuyNow())
                .isPurchaseStatus(productData.getIsPurchaseStatus())
                .isOnlyAdult(productData.getIsOnlyAdult())
                .isSoldOut(productData.getIsSoldOut())
                .soldOutTitle(productData.getSoldOutTitle())
                .soldOutText(productData.getSoldOutText())
                .canRestockNotify(productData.getCanRestockNotify())
                .isLowStock(productData.getIsLowStock())
                .productStatus(status)
                .build();

        product = productRepository.save(product);

        // ProductImage 저장
        if (productData.getListImageUrl() != null) {
            productImageRepository.save(ProductImage.builder()
                    .product(product)
                    .imageUrl(productData.getListImageUrl())
                    .imageType(ProductImage.ImageType.MAIN)
                    .sortOrder(1)
                    .build());
        }

        saveProductOptions(product, productData.getNo(), productData);

        return product;
    }

    // 단일 상품 ProductDetail 저장
    private void saveBasicProductDetail(Product product, CrawlingResponse.ProductData productData) {
        productDetailRepository.save(ProductDetail.builder()
                .product(product)
                .name(productData.getName())
                .basePrice(productData.getSalesPrice())
                .discountedPrice(productData.getDiscountedPrice())
                .quantity(productData.getStock())
                .isAvailable(!Boolean.TRUE.equals(productData.getIsSoldOut()))
                .build());
    }

    // 상품 옵션 매핑 저장
    private void saveProductOptions(Product product, Long productNo, CrawlingResponse.ProductData productData) {

        try {
            String url = String.format("%s/%d", DETAIL_API_URL, productNo);
            ProductOptionResponse response = restTemplate.getForObject(url, ProductOptionResponse.class);

            if (response == null || response.getData() == null) {
                saveBasicProductDetail(product, productData);
                return;
            }

            ProductOptionResponse.OptionData data = response.getData();

            Brand detailBrand = getOrCreateBrand(data.getBrandInfo());
            product.setBrand(detailBrand);

            String productDetailHtml = data.getProductDetail() != null ? data.getProductDetail().getLegacyContent() : null;

            product.updateProductOption(
                    data.getExpirationDate(),
                    data.getGuides(),
                    productDetailHtml,
                    data.getProductNotice()
            );
            productRepository.save(product);

            if (data.getDealProducts() != null && !data.getDealProducts().isEmpty()) {
                for (ProductOptionResponse.DealProduct dealProduct : data.getDealProducts()) {
                    productDetailRepository.save(ProductDetail.builder()
                            .product(product)
                            .name(dealProduct.getName())
                            .basePrice(dealProduct.getBasePrice())
                            .discountedPrice(dealProduct.getDiscountedPrice())
                            .quantity(999)
                            .isAvailable(!Boolean.TRUE.equals(dealProduct.getIsSoldOut()))
                            .build());
                }
            } else {
                saveBasicProductDetail(product, productData);
            }

            // 배송/포장 옵션 매핑
            syncOptionMappings(product, data);

        } catch (Exception e) {
            saveBasicProductDetail(product, productData);
        }
    }

    private void syncOptionMappings(Product product, ProductOptionResponse.OptionData data) {
        List<ProductDetail> details = productDetailRepository.findByProduct_ProductId(product.getProductId());
        if (details.isEmpty()) return;

        if (data.getStorageType() != null && !data.getStorageType().isEmpty()) {
            mapOption(details, "packaging", data.getStorageType().get(0));
        }

        if (data.getDeliveryTypeInfos() != null && !data.getDeliveryTypeInfos().isEmpty()) {
            mapOption(details, "delivery", data.getDeliveryTypeInfos().get(0).getType());
        }
    }

    private Brand getBrandFromApi(Long productNo) {
        try {
            String url = String.format("%s/%d", DETAIL_API_URL, productNo);
            ProductOptionResponse response = restTemplate.getForObject(url, ProductOptionResponse.class);

            if (response == null || response.getData() == null) {
                return brandRepository.findById(1L).orElse(null);
            }

            return getOrCreateBrand(response.getData().getBrandInfo());
        } catch (Exception e) {
            log.error("브랜드 조회 실패 - productNo: {}", productNo);
            return brandRepository.findById(1L).orElse(null);
        }
    }

    private Brand getOrCreateBrand(ProductOptionResponse.BrandInfo brandInfo) {
        if (brandInfo == null || brandInfo.getNameGate() == null) {
            return brandRepository.findById(1L).orElse(null); // 비구름 브랜드 ID
        }

        Long brandCode = brandInfo.getNameGate().getBrandCode();
        String brandName = brandInfo.getNameGate().getName();

        if (brandCode == null || brandName == null) {
            return brandRepository.findById(1L).orElse(null);
        }

        Optional<Brand> existingBrand = brandRepository.findByBrandCode(brandCode);

        String logoUrl = brandInfo.getLogoGate() != null ? brandInfo.getLogoGate().getImage() : null;
        String description = brandInfo.getLogoGate() != null ? brandInfo.getLogoGate().getDescription() : null;

        if (existingBrand.isPresent()) {
            Brand brand = existingBrand.get();

            if (logoUrl != null || description != null) {
                brand.updateBrandInfo(logoUrl, description);
                brandRepository.save(brand);
            }

            return brand;
        }

        Seller SystemSeller = sellerRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("기본 판매자 정보가 없습니다."));

        Brand newBrand = Brand.builder()
                .seller(SystemSeller)
                .brandCode(brandCode)
                .name(brandName)
                .logoUrl(logoUrl)
                .description(description)
                .build();

        SystemSeller.addBrand(newBrand);

        return brandRepository.save(newBrand);
    }

    private void mapOption(List<ProductDetail> details, String optionType, String optionValue) {
        ProductOption option = productOptionRepository
                .findByOptionTypeAndOptionValue(optionType, optionValue)
                .orElseGet(() -> {
                    // fallback: delivery는 NORMAL_PARCEL
                    if (optionType.equals("delivery")) {
                        return productOptionRepository
                                .findByOptionTypeAndOptionValue("delivery", "NORMAL_PARCEL")
                                .orElse(null);
                    }
                    return null;
                });

        if (option == null) {
            return;
        }

        for (ProductDetail detail : details) {
            boolean exists = productOptionMappingRepository
                    .findByProductDetail_ProductDetailId(detail.getProductDetailId())
                    .stream()
                    .anyMatch(m -> m.getProductOption().getOptionId().equals(option.getOptionId()));

            if (exists) {
                continue;
            }

            productOptionMappingRepository.save(ProductOptionMapping.builder()
                    .productDetail(detail)
                    .productOption(option)
                    .build());
        }
    }

    private Product.ProductStatus determineProductStatus(CrawlingResponse.ProductData productData) {
        if (!productData.getIsPurchaseStatus()) {
            return Product.ProductStatus.WAIT;
        } else if (productData.getIsSoldOut()) {
            return Product.ProductStatus.SOLD_OUT;
        } else {
            return Product.ProductStatus.SALE;
        }
    }
}