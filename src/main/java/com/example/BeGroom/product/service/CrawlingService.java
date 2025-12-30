package com.example.BeGroom.product.service;

import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductDetail;
import com.example.BeGroom.product.domain.ProductImage;
import com.example.BeGroom.product.dto.crawling.CrawlingResponse;
import com.example.BeGroom.product.dto.crawling.DetailResponse;
import com.example.BeGroom.product.repository.ProductDetailRepository;
import com.example.BeGroom.product.repository.ProductImageRepository;
import com.example.BeGroom.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 상품 크롤링 서비스
 */
@Service
@RequiredArgsConstructor
public class CrawlingService {

    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductImageRepository productImageRepository;
    private final RestTemplate restTemplate;

    private static final String API_BASE_URL = "https://api.kurly.com/collection/v2/home/sites/market";
    private static final String DETAIL_API_URL = "https://api.kurly.com/showroom/v1/products";

    /**
     * 카테고리 크롤링
     */
    @Transactional
    public List<Product> crawlCategory(Long categoryId, int maxProducts) {
        List<Product> savedProducts = new ArrayList<>();
        int page = 1;
        int perPage = 96;

        while (savedProducts.size() < maxProducts) {
            try {
                // API URL 생성
                String url = String.format(
                        "%s/product-categories/%d/products?sort_type=4&page=%d&per_page=%d&filters=",
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
                        Product product = saveProduct(productData);
                        savedProducts.add(product);
                    } catch (Exception e) {
                        continue;
                    }
                }

                page++;
                Thread.sleep(1000); // API 요청 간격 (1초)

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                break;
            }
        }

        return savedProducts;
    }

    /**
     * 상품 데이터 DB에 저장
     */
    private Product saveProduct(CrawlingResponse.ProductData productData) {
        Product.ProductStatus status = determineProductStatus(productData);

        // 1. Product 저장
        Product product = Product.builder()
                .productNo(productData.getNo())
                .sellerId(1L)
                .brand(productData.getBrand())
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
                .productStatus(status)
                .build();

        product = productRepository.save(product);

        // 2. ProductDetail 저장
        if (productData.getIsMultiplePrice()) {
            crawlProductDetails(product.getProductId(), productData.getNo());
        } else {
            saveBasicProductDetail(product.getProductId(), productData);
        }

        // 3. ProductImage 저장
        if (productData.getListImageUrl() != null) {
            ProductImage productImage = ProductImage.builder()
                    .productId(product.getProductId())
                    .imageUrl(productData.getListImageUrl())
                    .imageType(ProductImage.ImageType.MAIN)
                    .sortOrder(1)
                    .build();

            productImageRepository.save(productImage);
        }

        return product;
    }

    /**
     * 단일 상품 ProductDetail 저장
     */
    private void saveBasicProductDetail(Long productId, CrawlingResponse.ProductData productData) {
        ProductDetail productDetail = ProductDetail.builder()
                .productId(productId)
                .name(productData.getName())
                .basePrice(productData.getSalesPrice())
                .discountedPrice(productData.getDiscountedPrice())
                .quantity(productData.getStock())
                .isAvailable(!productData.getIsSoldOut())
                .build();

        productDetailRepository.save(productDetail);
    }

    /**
     * 옵션 상품 상세 크롤링
     */
    private void crawlProductDetails(Long productId, Long externalNo) {
        try {
            String url = DETAIL_API_URL + "/" + externalNo + "/minimal";
            DetailResponse detail = restTemplate.getForObject(url, DetailResponse.class);

            if (detail != null && detail.getData() != null && detail.getData().getDealProducts() != null) {
                // 옵션별 ProductDetail 저장
                for (DetailResponse.DealProduct dealProduct : detail.getData().getDealProducts()) {
                    ProductDetail productDetail = ProductDetail.builder()
                            .productId(productId)
                            .name(dealProduct.getName())
                            .basePrice(dealProduct.getBasePrice())
                            .discountedPrice(dealProduct.getDiscountedPrice())
                            .quantity(999)  // 옵션별 재고는 알 수 없음
                            .isAvailable(true)
                            .build();

                    productDetailRepository.save(productDetail);
                }

                Thread.sleep(500);  // 상세 API 호출 간격
            }
        } catch (Exception e) {
            // 상세 크롤링 실패 시 기본 ProductDetail 저장
            ProductDetail fallback = ProductDetail.builder()
                    .productId(productId)
                    .name("기본")
                    .basePrice(0)
                    .quantity(0)
                    .isAvailable(false)
                    .build();

            productDetailRepository.save(fallback);
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
