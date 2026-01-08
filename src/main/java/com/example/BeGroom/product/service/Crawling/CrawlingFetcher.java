package com.example.BeGroom.product.service.Crawling;

import com.example.BeGroom.product.dto.crawling.CrawlingResponse;
import com.example.BeGroom.product.dto.crawling.ProductOptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlingFetcher {

    private final WebClient webClient;
    private static final String API_BASE_URL = "https://api.kurly.com/collection/v2/home/sites/market";
    private static final String DETAIL_API_URL = "https://api.kurly.com/showroom/v2/products";

    public CrawlingResponse fetchCategoryProducts(String categoryId, int page, int perPage) {
        return webClient.get()
                .uri(API_BASE_URL + "/product-categories/{categoryId}/products?sort_type=4&page={page}&per_page={perPage}",
                        categoryId, page, perPage)
                .retrieve()
                .bodyToMono(CrawlingResponse.class)
                .doOnError(e -> log.error("목록 조회 중 오류 발생: {}", e.getMessage()))
                .block(); // 기존 서비스 로직 동기 처리를 위해 block 사용
    }

    public Mono<ProductOptionResponse> fetchProductDetailAsync(Long productNo) {
        return webClient.get()
                .uri(DETAIL_API_URL + "/{productNo}", productNo)
                .retrieve()
                .bodyToMono(ProductOptionResponse.class)
                .onErrorResume(e -> {
                    log.error("상세 조회 실패 (productNo: {}): {}", productNo, e.getMessage());
                    return Mono.just(new ProductOptionResponse()); // 실패 시 빈 객체 반환하여 전체 흐름 유지
                });
    }
}
