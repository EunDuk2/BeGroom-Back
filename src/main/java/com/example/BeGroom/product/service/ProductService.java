package com.example.BeGroom.product.service;

import com.example.BeGroom.product.domain.*;
import com.example.BeGroom.product.dto.ProductDetailDto;
import com.example.BeGroom.product.dto.ProductDetailResDto;
import com.example.BeGroom.product.dto.ProductListResDto;
import com.example.BeGroom.product.dto.ProductSearchCondition;
import com.example.BeGroom.product.repository.*;
import com.example.BeGroom.product.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductDetailRepository productDetailRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    // 관리자용 상품 목록 조회 (페이징)
    public Page<ProductListResDto> getAllProductsForAdmin(List<String> statuses, Pageable pageable) {

        Page<Product> products;

        if (statuses != null && !statuses.isEmpty()) {
            List<Product.ProductStatus> statusList = statuses.stream()
                    .map(Product.ProductStatus::valueOf)
                    .toList();
            products = productRepository.findByProductStatusIn(statusList, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        return products.map(product -> {
            String mainImageUrl = getMainImageUrl(product.getProductId());
            String brandName = getBrandName(product.getBrandId());
            return ProductListResDto.from(product, mainImageUrl, brandName);
        });
    }

    // 상품 상세 조회
    public ProductDetailResDto getProductDetail(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        String mainImageUrl = getMainImageUrl(productId);
        List<String> detailImageUrls = getDetailImageUrls(productId);
        List<ProductDetailDto> details = getProductDetails(productId);
        String brandName = getBrandName(product.getBrandId());

        return ProductDetailResDto.from(product, mainImageUrl, detailImageUrls, details, brandName);
    }

    // 상품 검색 (키워드, 필터, 정렬, 페이징)
    public Page<ProductListResDto> searchProducts(ProductSearchCondition condition, Pageable pageable) {

        List<Long> productIdsWithCategory = null;
        if (condition.getCategoryIds() != null && !condition.getCategoryIds().isEmpty()) {

            List<Long> expandedCategoryIds = expandCategoryIds(condition.getCategoryIds());
            productIdsWithCategory = productRepository.findProductIdsByCategoryIds(expandedCategoryIds);

            if (productIdsWithCategory.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        List<Long> productIdsWithDelivery = null;
        if (condition.getDeliveryTypes() != null && !condition.getDeliveryTypes().isEmpty()) {
            productIdsWithDelivery = productRepository.findProductIdsByDeliveryTypes(condition.getDeliveryTypes());

            if (productIdsWithDelivery.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        List<Long> productIdsWithPackaging = null;
        if (condition.getPackagingTypes() != null && !condition.getPackagingTypes().isEmpty()) {
            productIdsWithPackaging = productRepository.findProductIdsByPackagingTypes(condition.getPackagingTypes());

            if (productIdsWithPackaging.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        Specification<Product> spec = ProductSpecification.searchByCondition(
                condition,
                productIdsWithCategory,
                productIdsWithDelivery,
                productIdsWithPackaging
        );

        Page<Product> products = productRepository.findAll(spec, pageable);

        return products.map(product -> {
            String mainImageUrl = getMainImageUrl(product.getProductId());
            String brandName = getBrandName(product.getBrandId());

            return ProductListResDto.from(product, mainImageUrl, brandName);
        });
    }

    private List<Long> expandCategoryIds(List<Long> categoryIds) {
        List<Long> expandedIds = new ArrayList<>();

        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId).orElse(null);

            if (category == null) continue;

            if (category.getLevel() == 1) {
                List<Category> subCategories = categoryRepository.findByParentIdAndIsActiveOrderBySortOrderAsc(categoryId, true);

                for (Category subCategory : subCategories) {
                    expandedIds.add(subCategory.getCategoryId());
                }
            } else if (category.getLevel() == 2) {
                expandedIds.add(categoryId);
            }
        }

        return expandedIds;
    }

    // 메인 이미지 URL 조회
    private String getMainImageUrl(Long productId) {

        List<ProductImage> mainImage = productImageRepository.findByProductIdAndImageTypeOrderBySortOrderAsc(
                productId,
                ProductImage.ImageType.MAIN
        );

        return mainImage.isEmpty() ? null : mainImage.get(0).getImageUrl();
    }

    private List<String> getDetailImageUrls(Long productId) {
        List<ProductImage> detailImages = productImageRepository.findByProductIdAndImageTypeOrderBySortOrderAsc(
                productId,
                ProductImage.ImageType.DETAIL
        );

        return detailImages.stream()
                .map(ProductImage::getImageUrl)
                .toList();
    }

    private List<ProductDetailDto> getProductDetails(Long productId) {
        List<ProductDetail> productDetails = productDetailRepository.findByProductId(productId);

        return productDetails.stream()
                .map(ProductDetailDto::from)
                .toList();
    }

    private String getBrandName(Long brandId) {
        if (brandId == null) {
            return null;
        }

        return brandRepository.findById(brandId)
                .map(Brand::getName)
                .orElse(null);
    }

}