package com.example.BeGroom.product.service;

import com.example.BeGroom.product.domain.Brand;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductDetail;
import com.example.BeGroom.product.domain.ProductStatus;
import com.example.BeGroom.product.dto.admin.ProductCreateRequest;
import com.example.BeGroom.product.dto.ProductListResponse;
import com.example.BeGroom.product.dto.admin.ProductUpdateRequest;
import com.example.BeGroom.product.repository.BrandRepository;
import com.example.BeGroom.product.repository.ProductDetailRepository;
import com.example.BeGroom.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final ProductDetailRepository productDetailRepository;

    /**
     * 신규 상품 등록
     */
    @Transactional
    public Long createProduct(ProductCreateRequest request) {
        Brand brand = brandRepository.findById(request.getBrandId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다."));

        Product product = request.toEntity(brand);

        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            request.getDetails().forEach(detailRequest ->
                product.addDetail(
                    detailRequest.getName(),
                    detailRequest.getProductNo(),
                    detailRequest.getOriginalPrice(),
                    detailRequest.getSellingPrice(),
                    detailRequest.getStock()
                )
            );
        }

        Product savedProduct = productRepository.save(product);

        log.info("신규 상품 등록 완료. ID: {}, 이름: {}, 옵션 수: {}건", savedProduct.getId(), savedProduct.getName(), savedProduct.getProductDetails().size());
        return savedProduct.getId();
    }

    /**
     * 상품 정보 수정
     */
    @Transactional
    public void updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = findProductById(productId);

        validateNotDeleted(product);

        product.updateBasicInfo(
            request.getName(),
            request.getShortDescription(),
            request.getProductInfo(),
            request.getProductNotice()
        );

        log.info("상품 정보 수정 완료. ID: {}", productId);
    }

    /**
     * 상세 상품 가격 수정
     */
    @Transactional
    public void updateProductPrice(Long detailId, Integer originalPrice, Integer sellingPrice) {
        ProductDetail detail = findProductDetailById(detailId);
        validateNotDeleted(detail.getProduct());

        detail.addPrice(originalPrice, sellingPrice);

        log.info("상품 옵션(ID: {}) 가격 변경 완료: 정가 {}, 판매가 {}", detailId, originalPrice, sellingPrice);
    }

    /**
     * 상세 상품 재고 수동 관리(입고/조정)
     */
    @Transactional
    public void updateStock(Long detailId, int quantityChange) {
        ProductDetail detail = findProductDetailById(detailId);
        validateNotDeleted(detail.getProduct());

        if (quantityChange > 0) {
            detail.increaseStock(quantityChange);
        } else if (quantityChange < 0) {
            detail.decreaseStock(Math.abs(quantityChange));
        }

        detail.getProduct().syncStatusByStock();

        log.info("상품 옵션(ID: {}) 재고 조정 완료: {}건", detailId, quantityChange);
    }

    /**
     * 상품 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = findProductById(productId);

        if (product.getDeletedAt() != null) {
            throw new IllegalStateException("이미 삭제된 상품입니다.");
        }

        product.delete();
        log.info("상품 삭제(Soft Delete) 완료. ID: {}", productId);
    }

    /**
     * 관리자용 상품 목록 조회 (페이징)
     */
    public Page<ProductListResponse> getAllProductsForAdmin(List<String> statuses, Pageable pageable) {
        Page<Product> products;

        if (statuses != null && !statuses.isEmpty()) {
            List<ProductStatus> statusEnums = statuses.stream()
                .map(status -> {
                    try {
                        return ProductStatus.valueOf(status.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("유효하지 않은 상품 상태입니다: " + status);
                    }
                })
                .toList();
            products = productRepository.findByProductStatusIn(statusEnums, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        return products.map(product -> ProductListResponse.of(product, false));
    }

    /**
     * 상품 상태 변경
     */
    @Transactional
    public void changeProductStatus(Long productId, String statusStr) {
        Product product = findProductById(productId);
        validateNotDeleted(product);
        ProductStatus targetStatus = convertToStatusEnum(statusStr);

        switch (targetStatus) {
            case SALE -> product.markAsOnSale();
            case WAIT -> product.markAsWait();
            case STOP -> product.markAsStop();
            case SOLD_OUT -> log.warn("SOLD_OUT 상태는 재고 로직에 의해 자동으로 관리됩니다. ID: {}", productId);
            default -> throw new IllegalArgumentException("지원하지 않는 상태 변경 요청입니다: " + statusStr);
        }

        log.info("상품 {} 상태 변경 완료: {}", productId, targetStatus);
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
    }

    private ProductDetail findProductDetailById(Long detailId) {
        return productDetailRepository.findById(detailId)
            .orElseThrow(() -> new IllegalArgumentException("상세 상품을 찾을 수 없습니다: " + detailId));
    }

    private ProductStatus convertToStatusEnum(String statusStr) {
        try {
            return ProductStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("유효하지 않은 상품 상태입니다: " + statusStr);
        }
    }

    private void validateNotDeleted(Product product) {
        if (product.getDeletedAt() != null) {
            throw new IllegalStateException("삭제된 상품은 수정하거나 상태를 변경할 수 없습니다. ID: " + product.getId());
        }
    }

}
