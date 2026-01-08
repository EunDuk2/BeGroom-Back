package com.example.BeGroom.product.service.Crawling;

import com.example.BeGroom.product.domain.*;
import com.example.BeGroom.product.dto.crawling.CrawlingRequest;
import com.example.BeGroom.product.dto.crawling.CrawlingResponse;
import com.example.BeGroom.product.dto.crawling.ProductOptionResponse;
import com.example.BeGroom.product.repository.*;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.repository.SellerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlingService {

    private final CrawlingFetcher crawlingFetcher;
    private final CrawlingDataPersistenceService persistenceService;
    private final CategoryRepository categoryRepository;

    // 카테고리 크롤링
    @Async
    public void crawl(CrawlingRequest request) {

        List<Category> targetCategories = determineTargetCategories(request.getCategoryIds());
        if (targetCategories.isEmpty()) {
            log.warn("크롤링할 대상 카테고리가 없습니다.");
            return;
        }

        log.info("백그라운드에서 크롤링 시작: 대상 카테고리 수 {}", targetCategories.size());

        for (Category category : targetCategories) {
            try {
                // 내부 로직은 그대로 유지하되 결과 리스트를 반환하지 않고 로그만 남깁니다.
                crawlCategory(category, request.getMaxProductsPerCategory());
                Thread.sleep(1000); // 1초 대기
            } catch (InterruptedException e) {
                log.error("크롤링 중단됨");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("카테고리 크롤링 실패 - categoryId: {}, 에러: {}", category.getCategoryId(), e.getMessage());
            }
        }

        log.info("모든 백그라운드 크롤링 작업이 완료되었습니다.");    }

    // 단일 카테고리 크롤링 (내부 메서드)
    public List<Product> crawlCategory(Category category, int maxProducts) {

        List<Product> savedProducts = new ArrayList<>();
        int page = 1;
        int perPage = 96;

        while (savedProducts.size() < maxProducts) {
            try {
                // 목록 조회
                CrawlingResponse response = crawlingFetcher.fetchCategoryProducts(category.getExternalCategoryId(), page, perPage);
                if (response == null || response.getData() == null || response.getData().isEmpty()) break;

                List<CrawlingResponse.ProductData> pageData = response.getData();
                int remainingLimit = maxProducts - savedProducts.size();

                List<ProductWithDetail> batchResults = Flux.fromIterable(pageData)
                        .take(remainingLimit)
                        .flatMap(productData ->
                                crawlingFetcher.fetchProductDetailAsync(productData.getNo())
                                        .map(detail -> new ProductWithDetail(productData, detail))
                        , 10)
                        .collectList()
                        .block();

                // 수집한 데이터 순차적으로 DB에 저장
                if (batchResults != null) {
                    for (ProductWithDetail item : batchResults) {
                        try {
                            Product product = persistenceService.saveProductWithTransaction(item.data(), item.detail());
                            persistenceService.saveCategoryMapping(product, category);
                            savedProducts.add(product);
                        } catch (Exception e) {
                            log.error("상품 저장 실패 - productNo: {}, 에러: {}", item.data().getNo(), e.getMessage());
                        }
                    }
                }

                if (pageData.size() < perPage) break;
                page++;
                Thread.sleep(1000); // API 요청 간격 (1초)

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("API 호출 실패 - categoryId: {}, page: {}, 에러: {}",
                        category.getCategoryId(), page, e.getMessage());
                break;
            }
        }

        return savedProducts;
    }

    private List<Category> determineTargetCategories(List<Long> categoryIds) {

        if (categoryIds == null || categoryIds.isEmpty()) {
            return categoryRepository.findByLevelAndIsActiveOrderBySortOrderAsc(2, true);
        }

        List<Category> targetCategories = new ArrayList<>();
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));

            if (category.getLevel() == 1) {
                List<Category> subCategories = categoryRepository.findByParent_CategoryIdAndIsActiveOrderBySortOrderAsc(category.getCategoryId(), true);
                targetCategories.addAll(subCategories);
            } else if (category.getLevel() == 2) {
                targetCategories.add(category);
            } else {
                throw new IllegalArgumentException("지원하지 않는 카테고리 레벨: " + category.getLevel());
            }
        }

        return targetCategories;
    }

    private record ProductWithDetail(CrawlingResponse.ProductData data, ProductOptionResponse detail) {}
}