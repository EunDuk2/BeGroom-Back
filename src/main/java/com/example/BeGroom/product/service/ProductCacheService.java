package com.example.BeGroom.product.service;

import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductStatus;
import com.example.BeGroom.product.dto.*;
import com.example.BeGroom.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCacheService {
    private final ProductRepository productRepository;

    @Cacheable(
        value = "productSearch",
        key = "'search:' + #condition.toCacheKey().toString() + ':p' + #pageable.pageNumber + ':s' + #pageable.pageSize"
    )
    public CustomSlice<ProductListResponse> getCachedProducts(ProductSearchCondition condition, Pageable pageable) {
        Slice<ProductListResponse> products = productRepository.findAllByCondition(condition, pageable)
            .map(product -> ProductListResponse.of(product, false));

        return CustomSlice.from(products);
    }

    @Cacheable(
        value = "productDetail",
        key = "'detail:' + #productId"
    )
    public ProductDetailResponse getCachedProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (product.getDeletedAt() != null) {
            throw new IllegalStateException("삭제된 상품입니다.");
        }

        if (product.getProductStatus() == ProductStatus.STOP) {
            throw new IllegalStateException("판매 중지된 상품입니다.");
        }

        return ProductDetailResponse.of(product, false);
    }

    /**
     * 카테고리/키워드 검색 시 해당하는 상품들의 브랜드 목록
     */
    @Cacheable(
        value = "brandFilters",
        key = "'brands:' + #condition.toCacheKey().toString()"
    )
    public List<BrandFilterResponse> getBrandFilters(ProductSearchCondition condition) {
        return productRepository.findBrandsBySearchCondition(condition);
    }
}
