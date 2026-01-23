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

import java.util.ArrayList;
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
    private final CategoryRepository categoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Product saveProductWithTransaction(CrawlingResponse.ProductData productData, ProductOptionResponse detailResponse) {

        // 중복 체크
        Optional<Product> existing = productRepository.findByNo(productData.getNo());
        if (existing.isPresent()) {
            return existing.get();
        }

        // 브랜드
        Brand brand = processBrand(detailResponse);

        // Product 엔티티 생성 및 저장
        Product product = Product.builder()
                .no(productData.getNo())
                .brand(brand)
                .name(productData.getName())
                .shortDescription(productData.getShortDescription())
                .productStatus(determineProductStatus(productData))
                .productNotice(detailResponse != null && detailResponse.getData() != null ? new ArrayList<>(detailResponse.getData().getProductNotice()) : null)
                .build();

        product = productRepository.save(product);

        // 메인 이미지
        if (productData.getListImageUrl() != null) {
            productImageRepository.save(ProductImage.builder()
                    .product(product)
                    .imageUrl(productData.getListImageUrl())
                    .imageType(ImageType.MAIN)
                    .sortOrder(1)
                    .build());
        }

        // 상세 데이터 및 옵션 처리
        processDetailAndOptions(product, productData, detailResponse);

        return product;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCategoryMapping(Product product, Category category) {
        Category managedCategory = categoryRepository.findById(category.getId())
            .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다: " + category.getId()));

        if (productCategoryRepository.existsByProduct_IdAndCategory_Id(product.getId(), category.getId())) {
            return;
        }

        long existingCount = productCategoryRepository.countByProduct_Id(product.getId());
        productCategoryRepository.save(ProductCategory.builder()
                .product(product)
                .category(managedCategory)
                .isPrimary(existingCount == 0)
                .build());
    }

    private void processDetailAndOptions(Product product, CrawlingResponse.ProductData data, ProductOptionResponse detailResponse) {
        List<ProductDetail> savedDetails = new ArrayList<>();

        if (detailResponse == null || detailResponse.getData() == null) {
            savedDetails.add(saveProductDetail(product, data.getName(), data.getNo(),
                data.getSalesPrice(), data.getDiscountedPrice(), data.getIsSoldOut()));
        } else {
            ProductOptionResponse.OptionData optionData = detailResponse.getData();

            // 상세 설명 및 고시정보 업데이트
            String productDetailHtml = optionData.getProductDetail() != null ? optionData.getProductDetail().getLegacyContent() : null;
            product.updateBasicInfo(
                product.getName(),
                product.getShortDescription(),
                productDetailHtml,
                optionData.getProductNotice() != null ? new ArrayList<>(optionData.getProductNotice()) : null
            );
            productRepository.save(product);

            // 상세 상품 있으면 저장, 없으면 기본 상세 저장
            if (optionData.getDealProducts() != null && !optionData.getDealProducts().isEmpty()) {
                for (ProductOptionResponse.DealProduct deal : optionData.getDealProducts()) {
                    savedDetails.add(saveProductDetail(product, deal.getName(), deal.getNo(),
                        deal.getBasePrice(), deal.getDiscountedPrice(), deal.getIsSoldOut()));
                }
            } else {
                savedDetails.add(saveProductDetail(product, data.getName(), data.getNo(),
                    data.getSalesPrice(), data.getDiscountedPrice(), data.getIsSoldOut()));
            }

            syncOptionMappings(savedDetails, optionData);
        }
    }

    private ProductDetail saveProductDetail(Product product, String name, Long no, Integer originalPrice, Integer discountedPrice, Boolean isSoldOut) {
        int quantity = Boolean.TRUE.equals(isSoldOut) ? 0 : random.nextInt(100) + 1;

        ProductDetail detail = ProductDetail.builder()
            .product(product)
            .no(no)
            .name(name)
            .initialQuantity(quantity)
            .build();

        detail.addPrice(originalPrice, discountedPrice);

        return productDetailRepository.save(detail);
    }

    private Brand processBrand(ProductOptionResponse detailResponse) {
        if (detailResponse != null && detailResponse.getData() != null && detailResponse.getData().getBrandInfo() != null) {
            return getOrCreateBrand(detailResponse.getData().getBrandInfo());
        }
        return brandRepository.findById(1L).orElse(null);
    }

    private Brand getOrCreateBrand(ProductOptionResponse.BrandInfo brandInfo) {
        if (brandInfo.getNameGate() == null || brandInfo.getNameGate().getName() == null) {
            return brandRepository.findById(1L).orElse(null);
        }

        Long brandCode = brandInfo.getNameGate().getBrandCode();
        String brandName = brandInfo.getNameGate().getName();
        return brandRepository.findByBrandCode(brandCode)
            .map(existingBrand -> {
                // 정보 업데이트 로직
                String logoUrl = brandInfo.getLogoGate() != null ? brandInfo.getLogoGate().getImage() : null;
                String description = brandInfo.getLogoGate() != null ? brandInfo.getLogoGate().getDescription() : null;
                existingBrand.updateInfo(brandName, logoUrl, description);
                return brandRepository.save(existingBrand);
            })
            .orElseGet(() -> {
                Seller systemSeller = sellerRepository.findById(1L)
                    .orElseThrow(() -> new EntityNotFoundException("기본 판매자 정보가 없습니다."));

                return brandRepository.save(Brand.builder()
                    .seller(systemSeller)
                    .brandCode(brandCode)
                    .name(brandName)
                    .logoUrl(brandInfo.getLogoGate() != null ? brandInfo.getLogoGate().getImage() : null)
                    .description(brandInfo.getLogoGate() != null ? brandInfo.getLogoGate().getDescription() : null)
                    .build());
            });
    }

    private void syncOptionMappings(List<ProductDetail> details, ProductOptionResponse.OptionData data) {
        if (details.isEmpty()) return;

        if (data.getStorageType() != null && !data.getStorageType().isEmpty()) {
            mapOption(details, "packaging", data.getStorageType().get(0));
        }
        if (data.getDeliveryTypeInfos() != null && !data.getDeliveryTypeInfos().isEmpty()) {
            mapOption(details, "delivery", data.getDeliveryTypeInfos().get(0).getType());
        }
    }

    private void mapOption(List<ProductDetail> details, String type, String value) {
        productOptionRepository.findByOptionTypeAndOptionValue(type, value)
            .or(() -> "delivery".equals(type) ?
                productOptionRepository.findByOptionTypeAndOptionValue("delivery", "NORMAL_PARCEL") : Optional.empty())
            .ifPresent(option -> {
                for (ProductDetail detail : details) {
                    detail.addOption(option); // ProductDetail의 연관관계 편의 메서드 활용
                }
            });
    }

    private ProductStatus determineProductStatus(CrawlingResponse.ProductData data) {
        if (Boolean.TRUE.equals(data.getIsSoldOut())) {
            return ProductStatus.SOLD_OUT;
        }
        return ProductStatus.SALE;
    }
}
