package com.example.BeGroom.product.service.Crawling;

import com.example.BeGroom.product.domain.*;
import com.example.BeGroom.product.dto.crawling.CrawlingResponse;
import com.example.BeGroom.product.dto.crawling.ProductOptionResponse;
import com.example.BeGroom.product.repository.*;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.repository.SellerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlingDataPersistenceService {

    private final Random random = new Random(); // 재고 랜덤 삽입

    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionMappingRepository productOptionMappingRepository;
    private final BrandRepository brandRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final SellerRepository sellerRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Product saveProductWithTransaction(CrawlingResponse.ProductData productData, ProductOptionResponse detailResponse) {

        // 중복 체크
        Optional<Product> existing = productRepository.findByProductNo(productData.getNo());
        if (existing.isPresent()) {
            return existing.get();
        }

        // 브랜드
        Brand brand = processBrand(productData, detailResponse);

        // Product 엔티티 생성 및 저장
        Product product = Product.builder()
                .productNo(productData.getNo())
                .brand(brand)
                .name(productData.getName())
                .shortDescription(productData.getShortDescription())
                .salesPrice(productData.getSalesPrice())
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
                .productStatus(determineProductStatus(productData))
                .build();

        product = productRepository.save(product);

        // 메인 이미지
        if (productData.getListImageUrl() != null) {
            productImageRepository.save(ProductImage.builder()
                    .product(product)
                    .imageUrl(productData.getListImageUrl())
                    .imageType(ProductImage.ImageType.MAIN)
                    .sortOrder(1)
                    .build());
        }

        // 상세 데이터 및 옵션 처리
        processDetailAndOptions(product, productData, detailResponse);

        return product;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCategoryMapping(Product product, Category category) {
        if (productCategoryRepository.existsByProduct_ProductIdAndCategory_CategoryId(product.getProductId(), category.getCategoryId())) {
            return;
        }

        long existingCount = productCategoryRepository.countByProduct_ProductId(product.getProductId());
        productCategoryRepository.save(ProductCategory.builder()
                .product(product)
                .category(category)
                .isPrimary(existingCount == 0)
                .build());
    }

    private Brand processBrand(CrawlingResponse.ProductData data, ProductOptionResponse detailResponse) {
        if (detailResponse != null && detailResponse.getData() != null) {
            return getOrCreateBrand(detailResponse.getData().getBrandInfo());
        }
        return brandRepository.findById(1L).orElse(null);
    }

    private void processDetailAndOptions(Product product, CrawlingResponse.ProductData data, ProductOptionResponse detailResponse) {
        if (detailResponse == null || detailResponse.getData() == null) {
            saveBasicProductDetail(product, data);
            return;
        }

        ProductOptionResponse.OptionData optionData = detailResponse.getData();

        // 상세 설명 및 고시정보 업데이트
        String productDetailHtml = optionData.getProductDetail() != null ? optionData.getProductDetail().getLegacyContent() : null;
        product.updateProductOption(
                optionData.getExpirationDate(),
                optionData.getGuides(),
                productDetailHtml,
                optionData.getProductNotice()
        );
        productRepository.save(product);

        // 상세 상품 있으면 저장, 없으면 기본 상세 저장
        if (optionData.getDealProducts() != null && !optionData.getDealProducts().isEmpty()) {
            for (ProductOptionResponse.DealProduct deal : optionData.getDealProducts()) {
                // 재고 랜덤 삽입
                int quantity = Boolean.TRUE.equals(deal.getIsSoldOut()) ? 0 : random.nextInt(100) + 1;

                productDetailRepository.save(ProductDetail.builder()
                        .product(product)
                        .name(deal.getName())
                        .basePrice(deal.getBasePrice())
                        .discountedPrice(deal.getDiscountedPrice())
                        .quantity(quantity)
                        .isAvailable(!Boolean.TRUE.equals(deal.getIsSoldOut()))
                        .build());
            }
        } else {
            saveBasicProductDetail(product, data);
        }

        // 배송/포장 옵션 매핑
        syncOptionMappings(product, optionData);
    }

    private void saveBasicProductDetail(Product product, CrawlingResponse.ProductData data) {

        int quantity = Boolean.TRUE.equals(data.getIsSoldOut()) ? 0 : random.nextInt(100) + 1;

        productDetailRepository.save(ProductDetail.builder()
                .product(product)
                .name(data.getName())
                .basePrice(data.getSalesPrice())
                .discountedPrice(data.getDiscountedPrice())
                .quantity(quantity)
                .isAvailable(!Boolean.TRUE.equals(data.getIsSoldOut()))
                .build());
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

    private Brand getOrCreateBrand(ProductOptionResponse.BrandInfo brandInfo) {
        if (brandInfo == null || brandInfo.getNameGate() == null || brandInfo.getNameGate().getName() == null) {
            return brandRepository.findById(1L).orElse(null);
        }

        Long brandCode = brandInfo.getNameGate().getBrandCode();
        String brandName = brandInfo.getNameGate().getName();
        String logoUrl = brandInfo.getLogoGate() != null ? brandInfo.getLogoGate().getImage() : null;
        String description = brandInfo.getLogoGate() != null ? brandInfo.getLogoGate().getDescription() : null;

        Optional<Brand> existingBrand = brandRepository.findByBrandCode(brandCode);

        // 이미 브랜드가 존재하면 정보 업데이트 후 반환
        if (existingBrand.isPresent()) {
            Brand brand = existingBrand.get();
            if (logoUrl != null || description != null) {
                brand.updateBrandInfo(logoUrl, description);
                return brandRepository.save(brand);
            }
            return brand;
        }

        // 새 브랜드 생성 및 Seller 연관관계 편의 메서드 호출
        Seller systemSeller = sellerRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("기본 판매자 정보가 없습니다."));

        Brand newBrand = Brand.builder()
                .seller(systemSeller)
                .brandCode(brandCode)
                .name(brandName)
                .logoUrl(logoUrl)
                .description(description)
                .build();

        systemSeller.addBrand(newBrand); // 기존 코드의 addBrand 로직 유지
        return brandRepository.save(newBrand);
    }

    private void mapOption(List<ProductDetail> details, String type, String value) {
        productOptionRepository.findByOptionTypeAndOptionValue(type, value)
                .or(() -> type.equals("delivery") ? productOptionRepository.findByOptionTypeAndOptionValue("delivery", "NORMAL_PARCEL") : Optional.empty())
                .ifPresent(option -> {
                    for (ProductDetail detail : details) {
                        boolean exists = productOptionMappingRepository.findByProductDetail_ProductDetailId(detail.getProductDetailId())
                                .stream().anyMatch(m -> m.getProductOption().getOptionId().equals(option.getOptionId()));
                        if (!exists) {
                            productOptionMappingRepository.save(ProductOptionMapping.builder()
                                    .productDetail(detail)
                                    .productOption(option)
                                    .build());
                        }
                    }
                });
    }

    private Product.ProductStatus determineProductStatus(CrawlingResponse.ProductData data) {
        if (!data.getIsPurchaseStatus()) {
            return Product.ProductStatus.WAIT;
        }

        return data.getIsSoldOut() ? Product.ProductStatus.SOLD_OUT : Product.ProductStatus.SALE;
    }
}
