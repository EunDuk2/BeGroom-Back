package com.example.BeGroom.product.controller;

import com.example.BeGroom.common.config.CrawlingConfig;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.dto.crawling.CrawlingResultDto;
import com.example.BeGroom.product.service.CrawlingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/crawling")
@RequiredArgsConstructor
@Tag(name = "Crawling API", description = "상품 데이터 크롤링 API")
public class CrawlingController {

    private final CrawlingService crawlingService;
    private final CrawlingConfig crawlingConfig;

    @PostMapping("/category/{categoryId}")
    @Operation(summary = "단일 카테고리 크롤링", description = "특정 카테고리의 상품 크롤링")
    public ResponseEntity<CommonSuccessDto<CrawlingResultDto>> crawlCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "200") int maxProducts
    ) {
        List<Product> products = crawlingService.crawlCategory(categoryId, maxProducts);

        CrawlingResultDto result = new CrawlingResultDto(
                products.size(),
                "완료"
        );

        return ResponseEntity.ok(CommonSuccessDto.of(
                result,
                HttpStatus.OK,
                "카테고리 " + categoryId + " 크롤링 완료"
        ));
    }

    @PostMapping("/categories")
    @Operation(summary = "전체 카테고리 크롤링", description = "전체 카테고리의 상품 한 번에 크롤링")
    public ResponseEntity<CommonSuccessDto<CrawlingResultDto>> crawlAllCategories(
            @RequestParam(defaultValue = "200") int maxProductsPerCategory
    ) {
        List<Product> allProducts = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (CrawlingConfig.Category category : crawlingConfig.getCategories()) {
            try {
                List<Product> products = crawlingService.crawlCategory(category.getId(), maxProductsPerCategory);
                allProducts.addAll(products);
                successCount++;

                // 카테고리 간 대기 (3초)
                Thread.sleep(3000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // 에러 발생해도 다음 카테고리 진행
                failCount++;
                continue;
            }
        }

        CrawlingResultDto result = new CrawlingResultDto(
                allProducts.size(),
                String.format("전체 크롤링 완료 (성공: %d, 실패: %d)", successCount, failCount)
        );

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        result,
                        HttpStatus.OK,
                        String.format("%d개 카테고리 중 %d개 성공", crawlingConfig.getCategories().size(), successCount)
                )
        );
    }
}
