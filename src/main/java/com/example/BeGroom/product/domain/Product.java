package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.order.domain.OrderProduct;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.BeGroom.product.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_no", nullable = false, unique = true)
    private Long productNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductDetail> productDetails = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> productImages = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductCategory> productCategories = new ArrayList<>();

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "sales_price", nullable = false)
    private Integer salesPrice;

    @Column(name = "discounted_price")
    private Integer discountedPrice;

    @Column(name = "discount_rate")
    private Integer discountRate;

    @Column(name = "is_buy_now", nullable = false)
    @Builder.Default
    private Boolean isBuyNow = false;

    @Column(name = "is_purchase_status", nullable = false)
    @Builder.Default
    private Boolean isPurchaseStatus = false;

    @Column(name = "is_only_adult", nullable = false)
    @Builder.Default
    private Boolean isOnlyAdult = false;

    @Column(name = "is_sold_out", nullable = false)
    @Builder.Default
    private Boolean isSoldOut = false;

    @Column(name = "sold_out_title", length = 100)
    @Builder.Default
    private String soldOutTitle = "Coming Soon";

    @Column(name = "sold_out_text", columnDefinition = "TEXT")
    private String soldOutText;

    @Column(name = "can_restock_notify")
    @Builder.Default
    private Boolean canRestockNotify = false;

    @Column(name = "is_low_stock", nullable = false)
    @Builder.Default
    private Boolean isLowStock = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false)
    @Builder.Default
    private ProductStatus productStatus = ProductStatus.WAIT;

    @Column(name = "wishlist_count", nullable = false)
    @Builder.Default
    private Integer wishlistCount = 0;

    @Column(name = "sales_count", nullable = false)
    @Builder.Default
    private Integer salesCount = 0;

    @Column(name = "expiration_date", columnDefinition = "TEXT")
    private String expirationDate;

    @Column(name = "guides", columnDefinition = "TEXT")
    private String guides;

    @Column(name = "product_detail", columnDefinition = "LONGTEXT")
    private String productDetail;

    @Column(name = "product_notice", columnDefinition = "JSON")
    private String productNotice;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum ProductStatus {
        WAIT, SALE, SOLD_OUT, STOP
    }


    // 기본 정보 수정
    public void updateBasicInfo(String name, String shortDescription) {
        validateName(name);

        this.name = name;
        this.shortDescription = shortDescription;
    }

    // 가격 정보 수정
    public void updatePrice(Integer salesPrice, Integer discountedPrice) {
        validatePrice(salesPrice, discountedPrice);

        this.salesPrice = salesPrice;
        this.discountedPrice = discountedPrice;

        if (discountedPrice != null && salesPrice > 0) {
            this.discountRate = (int)(((double) (salesPrice - discountedPrice) / salesPrice) * 100);
        } else {
            this.discountRate = 0;
        }
    }

    // 판매 상태 강제 변경 (관리자용)
    public void changeStatus(ProductStatus status) {
        if (status == null) throw new IllegalArgumentException("상품 상태는 필수입니다.");

        if (this.productStatus == status) {
            return;
        }

        switch (status) {
            case SALE -> restock();
            case SOLD_OUT -> soldOut(null, null);
            case STOP -> applyStopState();
            case WAIT -> applyWaitState();
        }
    }

    // 품절 상태 변경
    private void soldOut(String title, String text) {
        this.isSoldOut = true;
        this.productStatus = ProductStatus.SOLD_OUT;
        this.isPurchaseStatus = false;
        this.soldOutTitle = (title != null) ? title : "SOLD_OUT";
        this.soldOutText = text;
    }

    // 재입고 및 판매 시작
    private void restock() {
        this.isSoldOut = false;
        this.productStatus = ProductStatus.SALE;
        this.isPurchaseStatus = true;
        this.isLowStock = false;
        this.soldOutTitle = "Coming Soon";
        this.soldOutText = null;
    }

    private void applyWaitState() {
        this.productStatus = ProductStatus.WAIT;
        this.isPurchaseStatus = false;
    }

    private void applyStopState() {
        this.productStatus = ProductStatus.STOP;
        this.isPurchaseStatus = false;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        applyStopState();
    }

    // ProductDetail 재고 상태에 따라 Product 상태를 자동으로 업데이트 - 모든 ProductDetail이 품절(is_available=false)이면 Product도 품절 처리
    public void updateStockStatusFromDetails() {
        if (productDetails == null || productDetails.isEmpty()) {
            return;
        }

        // 모든 ProductDetail의 재고 상태 확인
        boolean allSoldOut = productDetails.stream()
                .allMatch(detail -> !detail.getIsAvailable());

        boolean anyLowStock = productDetails.stream()
                .anyMatch(ProductDetail::isLowStock);

        // 모든 옵션이 품절된 경우
        if (allSoldOut) {
            soldOut(null, null);
        } else {
            // 이미 품절 상태였다면 재입고 처리
            if (this.isSoldOut) {
                restock();
            }
            // 재고 부족 상태 업데이트
            this.isLowStock = anyLowStock;
        }
    }

    // 메인이미지 조회
    public String getMainImageUrl() {
        return this.productImages.stream()
                .filter(img -> img.getImageType() == ProductImage.ImageType.MAIN)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }

    // 위시리스트 카운트 조절 메서드
    public void increaseWishlistCount() {
        this.wishlistCount++;
    }

    public void decreaseWishlistCount() {
        if (this.wishlistCount > 0) {
            this.wishlistCount--;
        }
    }

    public void updateProductOption(String expirationDate,
                                    List<String> guides,
                                    String productDetail,
                                    List<?> productNotice) {
        this.expirationDate = expirationDate;

        // guides JSON 문자열로 변환
        if (guides != null && !guides.isEmpty()) {
            try {
                this.guides = new ObjectMapper().writeValueAsString(guides);
            } catch (Exception e) {
                this.guides = null;
            }
        }

        this.productDetail = productDetail;

        // productNotice JSON 문자열로 변환
        if (productNotice != null && !productNotice.isEmpty()) {
            try {
                this.productNotice = new ObjectMapper().writeValueAsString(productNotice);
            } catch (Exception e) {
                this.productNotice = null;
            }
        }
    }


    // 이름 유효성 검사
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (name.length() > 200) {
            throw new IllegalArgumentException("상품명은 200자를 초과할 수 없습니다.");
        }
    }

    // 가격 유효성 검사
    private void validatePrice(Integer salesPrice, Integer discountedPrice) {
        if (salesPrice == null || salesPrice < 0) {
            throw new IllegalArgumentException("원가는 0 이상이어야 합니다.");
        }

        if (discountedPrice != null) {
            if (discountedPrice < 0) throw new IllegalArgumentException("할인가는 0 이상이어야 합니다.");
            if (discountedPrice > salesPrice) throw new IllegalArgumentException("할인가격은 판매가보다 클 수 없습니다.");
        }
    }

    // 연관관계 편의 메서드
    public void addProductDetail(ProductDetail productDetail) {
        this.productDetails.add(productDetail);
        productDetail.setProduct(this);

        updateStockStatusFromDetails();
    }

    public void addProductImage(ProductImage productImage) {
        this.productImages.add(productImage);
        productImage.setProduct(this);
    }

    // isPrimary : 대표 카테고리 여부
    public void addCategory(Category category, boolean isPrimary) {
        if (category == null) return;

        // 이미 대표 카테고리가 있는데 또 대표로 설정하려 하면 기존 것을 해제하거나 예외 처리
        if (isPrimary) {
            productCategories.forEach(pc -> pc.updatePrimary(false));
        }

        ProductCategory pc = ProductCategory.create(this, category, isPrimary);
        this.productCategories.add(pc);
    }

    // 연관관계 편의 메서드 (Brand 설정용)
    public void setBrand(Brand brand) {
        this.brand = brand;

        if (brand != null && !brand.getProducts().contains(this)) {
            brand.getProducts().add(this);
        }
    }
}