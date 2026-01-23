package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.converter.JsonListConverter;
import com.example.BeGroom.common.entity.BaseEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Assert;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product")
@Entity
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(nullable = false, unique = true)
    private Long no;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus productStatus;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductDetail> productDetails = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> productImages = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCategory> productCategories = new ArrayList<>();

    @Column(nullable = false)
    private Integer wishlistCount = 0;

    @Column(nullable = false)
    private Integer salesCount = 0;

    @Column(columnDefinition = "LONGTEXT")
    private String productInfo;

    @Convert(converter = JsonListConverter.class)
    @Column(columnDefinition = "JSON")
    private List<Object> productNotice;

    private LocalDateTime deletedAt;

    @Builder
    private Product(Brand brand, Long no, String name, String shortDescription, ProductStatus productStatus, String productInfo, List<Object> productNotice) {
        Assert.notNull(brand, "브랜드 정보는 필수입니다.");
        Assert.notNull(no, "상품 번호는 필수입니다.");
        validateName(name);

        this.no = no;
        this.name = name;
        this.shortDescription = shortDescription;
        this.productInfo = productInfo;
        this.productNotice = productNotice;
        this.productStatus = (productStatus != null) ? productStatus : ProductStatus.WAIT;

        updateBrand(brand);
    }

    public void addDetail(String name, Long no, Integer originalPrice, Integer sellingPrice, Integer stock) {
        ProductDetail detail = ProductDetail.builder()
            .product(this)
            .name(name)
            .no(no)
            .initialQuantity(stock)
            .build();

        detail.addPrice(originalPrice, sellingPrice);

        this.productDetails.add(detail);
    }

    public void updateBasicInfo(String name, String shortDescription, String productInfo, List<Object> productNotice) {
        validateName(name);
        this.name = name;
        this.shortDescription = shortDescription;
        this.productInfo = productInfo;
        this.productNotice = productNotice;
    }

    public void syncStatusByStock() {
        if (this.productStatus == ProductStatus.STOP || this.deletedAt != null) {
            return;
        }

        // 모든 상세 상품이 품절이면 상태를 SOLD_OUT으로 변경
        if (this.isSoldOut()) {
            this.productStatus = ProductStatus.SOLD_OUT;
        }
        // 재고가 있다면 (품절 상태였던 경우에만) 다시 SALE로 변경
        else if (this.productStatus == ProductStatus.SOLD_OUT) {
            this.productStatus = ProductStatus.SALE;
        }
    }

    public boolean isPurchasable() {
        if (this.deletedAt != null || this.productStatus != ProductStatus.SALE) {
            return false;
        }

        return productDetails.stream()
            .anyMatch(detail -> detail.getIsAvailable() && !detail.isSoldOut());
    }

    public boolean isSoldOut() {
        return productDetails.stream().allMatch(ProductDetail::isSoldOut);
    }

    public String getMainImageUrl() {
        return this.productImages.stream()
            .filter(img -> img.getImageType() == ImageType.MAIN)
            .min(Comparator.comparing(ProductImage::getSortOrder))
            .map(ProductImage::getImageUrl)
            .orElse(null);
    }

    public List<String> getDetailImageUrls() {
        return this.productImages.stream()
            .filter(img -> img.getImageType() == ImageType.DETAIL)
            .sorted(Comparator.comparing(ProductImage::getSortOrder))
            .map(ProductImage::getImageUrl)
            .toList();
    }

    public Integer getSalesPrice() {
        return productDetails.stream()
            .map(ProductDetail::getOriginalPrice)
            .min(Integer::compare)
            .orElse(0);
    }

    public Integer getDiscountedPrice() {
        return productDetails.stream()
            .map(ProductDetail::getSellingPrice)
            .min(Integer::compare)
            .orElse(0);
    }

    public Integer getDiscountRate() {
        return productDetails.stream()
            .min(Comparator.comparingInt(ProductDetail::getSellingPrice))
            .map(ProductDetail::getDiscountRate)
            .orElse(0);
    }

    public void markAsOnSale() { this.productStatus = ProductStatus.SALE; }
    public void markAsWait() { this.productStatus = ProductStatus.WAIT; }
    public void markAsStop() { this.productStatus = ProductStatus.STOP; }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        markAsStop();
    }

    // 위시리스트 수 증감
    public void increaseWishlistCount() {
        this.wishlistCount++;
    }
    public void decreaseWishlistCount() {
        if (this.wishlistCount > 0) {
            this.wishlistCount--;
        }
    }

    // 판매 수량 증감
    public void increaseSalesCount(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        this.salesCount += quantity;
    }
    public void decreaseSalesCount(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        this.salesCount -= quantity;
    }

    // 연관관계 편의 메서드
    public void updateBrand(Brand brand) {
        if (brand == null) throw new IllegalArgumentException("브랜드는 필수입니다.");

        if (this.brand != null) {
            this.brand.getProducts().remove(this);
        }

        this.brand = brand;

        if (!brand.getProducts().contains(this)) {
            brand.getProducts().add(this);
        }
    }

    public void addProductImage(ProductImage image) {
        if (image == null) return;

        if (!this.productImages.contains(image)) {
            this.productImages.add(image);
        }
    }

    public void addProductDetail(ProductDetail detail) {
        if (detail == null) return;

        if (!this.productDetails.contains(detail)) {
            this.productDetails.add(detail);
        }
        if (detail.getProduct() != this) {
            detail.setProduct(this);
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
}