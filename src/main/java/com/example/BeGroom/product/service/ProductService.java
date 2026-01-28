package com.example.BeGroom.product.service;

import com.example.BeGroom.product.domain.*;
import com.example.BeGroom.product.dto.BrandFilterResponse;
import com.example.BeGroom.product.dto.ProductDetailResponse;
import com.example.BeGroom.product.dto.ProductListResponse;
import com.example.BeGroom.product.dto.ProductSearchCondition;
import com.example.BeGroom.product.repository.*;
import com.example.BeGroom.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;

    /**
     * 상품 검색 (키워드, 필터, 정렬, 페이징)
     */
    public Page<ProductListResponse> searchProducts(ProductSearchCondition condition,
                                                    Pageable pageable,
                                                    Long memberId) {

        Page<Product> products = productRepository.findAllByCondition(condition, pageable);
        Set<Long> wishlistedProductIds = getWishlistedProductIds(memberId);

        return products.map(product ->
            ProductListResponse.of(product, wishlistedProductIds.contains(product.getId()))
        );
    }

    /**
     * 상품 상세 조회
     */
    public ProductDetailResponse getProductDetail(Long productId, Long memberId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (product.getDeletedAt() != null) {
            throw new IllegalStateException("삭제된 상품입니다.");
        }

        if (product.getProductStatus() == ProductStatus.STOP) {
            throw new IllegalStateException("판매 중지된 상품입니다.");
        }

        boolean isWishlisted = checkWishlisted(productId, memberId);

        return ProductDetailResponse.of(product, isWishlisted);
    }

    /**
     * 카테고리/키워드 검색 시 해당하는 상품들의 브랜드 목록
     */
    public List<BrandFilterResponse> getBrandFilters(ProductSearchCondition condition) {
        return productRepository.findBrandsBySearchCondition(condition);
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