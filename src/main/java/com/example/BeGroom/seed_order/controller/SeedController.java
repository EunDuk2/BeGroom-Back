package com.example.BeGroom.seed_order.controller;

import com.example.BeGroom.seed_order.dto.SeedRunRequest;
import com.example.BeGroom.seed_order.dto.SeedRunResponse;
import com.example.BeGroom.seed_order.service.SeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/seed")
public class SeedController {

    private final SeedService seedService;

    /**
     * 1) seed run 생성:
     * - member/wallet 준비
     * - productDetail 2개 준비(Stock 자동 생성)
     * - order N개 생성
     * - 응답으로 memberId, token(옵션), orderIds 반환
     */
    @PostMapping("/run")
    public SeedRunResponse createRun(@RequestBody SeedRunRequest req) {
        return seedService.createRun(req);
    }

    /**
     * 2) seed run 정리:
     * - runId 기준으로 이 run에서 만든 주문들/상품옵션들 삭제
     */
    @DeleteMapping("/run")
    public void cleanupRun() {
        seedService.cleanupRun();
    }

    /**
     * 3) csv 만들기 편하게:
     * - 응답을 csv 텍스트로 반환(파일 저장은 k6나 스크립트에서)
     */
    @PostMapping(value = "/run/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public String createRunAsCsv(@RequestBody SeedRunRequest req) {
        return seedService.createRunAsCsv(req);
    }
}

