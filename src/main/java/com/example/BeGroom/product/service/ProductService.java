package com.example.BeGroom.product.service;

import com.example.BeGroom.product.domain.*;
import com.example.BeGroom.product.dto.*;
import com.example.BeGroom.product.repository.*;
import com.example.BeGroom.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final WishlistRepository wishlistRepository;
    private final ProductCacheService productCacheService;

    /**
     * 상품 검색 (키워드, 필터, 정렬, 페이징)
     */
    public CustomSlice<ProductListResponse> searchProducts(ProductSearchCondition condition,
                                                           Pageable pageable,
                                                           Long memberId) {

        CustomSlice<ProductListResponse> products = productCacheService.getCachedProducts(condition, pageable);
        Set<Long> wishlistedProductIds = getWishlistedProductIds(memberId);
        List<ProductListResponse> updatedContent = products.content().stream()
            .map(product -> product.withWishlist(wishlistedProductIds.contains(product.productId())))
            .toList();

        return new CustomSlice<>(
            updatedContent,
            products.hasNext(),
            products.numberOfElements()
        );
    }

    /**
     * 상품 상세 조회
     */
    public ProductDetailResponse getProductDetail(Long productId, Long memberId) {
        ProductDetailResponse detail = productCacheService.getCachedProductDetail(productId);
        boolean isWishlisted = checkWishlisted(productId, memberId);

        return detail.withWishlist(isWishlisted);
    }

    public List<BrandFilterResponse> getBrandFilters(ProductSearchCondition condition) {
        return productCacheService.getBrandFilters(condition);
    }

    // ===== Private Helper Methods =====

    /**
     * 위시리스트 상품 ID 조회
     */
    private Set<Long> getWishlistedProductIds(Long memberId) {
        if (memberId == null) {
            return Collections.emptySet();
        }

        return wishlistRepository.findAllByMember_Id(memberId).stream()
                .map(wishlist -> wishlist.getProduct().getId())
                .collect(Collectors.toSet());
    }

    /**
     * 위시리스트 여부 확인
     */
    private Boolean checkWishlisted(Long productId, Long memberId) {
        if (memberId == null) {
            return false;
        }

        return wishlistRepository.existsByMember_IdAndProduct_Id(memberId, productId);
    }
}