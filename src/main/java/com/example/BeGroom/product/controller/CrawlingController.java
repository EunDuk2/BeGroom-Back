package com.example.BeGroom.product.controller;

import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.product.dto.crawling.CrawlingRequest;
import com.example.BeGroom.product.dto.crawling.CrawlingResultDto;
import com.example.BeGroom.product.service.Crawling.CrawlingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/crawling")
@RequiredArgsConstructor
@Tag(name = "Crawling API", description = "상품 데이터 크롤링 API")
public class CrawlingController {

    private final CrawlingService crawlingService;

    @PostMapping
    @Operation(
            summary = "카테고리 크롤링",
            description = """
            카테고리 기반 상품 크롤링
            
            **사용 방법:**
            1. 전체 중분류 크롤링
            POST /api/admin/crawling?maxProductsPerCategory=25
            
            2. 특정 대분류 크롤링 (예: 채소 카테고리 전체)
            POST /api/admin/crawling?categoryIds=1&maxProductsPerCategory=200
            
            3. 특정 중분류 크롤링 (예: 고구마·감자·당근)
            POST /api/admin/crawling?categoryIds=2&maxProductsPerCategory=25
            
            4. 여러 카테고리 동시 크롤링
            POST /api/admin/crawling?categoryIds=1,2,3&maxProductsPerCategory=20
            
            
            **파라미터:**
            - categoryIds: 크롤링할 카테고리 ID (null이면 전체 중분류)
            - maxProductsPerCategory: 카테고리당 최대 크롤링 개수 (기본값: 10)
            
            
            **주의:**
            - 대분류 ID를 넣으면 자동으로 하위 중분류 모두 크롤링
            - 크롤링 시간이 오래 걸릴 수 있음 (카테고리당 약 30초~1분)
            """
    )
    public ResponseEntity<CommonSuccessDto<CrawlingResultDto>> crawl(
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(defaultValue = "25") Integer maxProductsPerCategory
    ) {
        CrawlingRequest request = CrawlingRequest.builder()
                .categoryIds(categoryIds)
                .maxProductsPerCategory(maxProductsPerCategory)
                .build();

        crawlingService.crawl(request);

        String message = createStartMessage(categoryIds);

        CrawlingResultDto result = new CrawlingResultDto(0, message); // 실시간 개수는 알 수 없으므로 0 혹은 시작 메시지 전달
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(
                CommonSuccessDto.of(
                        result,
                        HttpStatus.ACCEPTED,
                        "크롤링 요청이 수락되었습니다."
                )
        );
    }

    // 응답 메시지 생성
    private String createStartMessage(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return "전체 카테고리에 대한 크롤링 작업이 백그라운드에서 시작되었습니다.";
        }
        return String.format("%d개의 지정된 카테고리에 대한 크롤링 작업이 시작되었습니다.", categoryIds.size());
    }
}