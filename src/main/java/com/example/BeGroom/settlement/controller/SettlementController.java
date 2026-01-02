package com.example.BeGroom.settlement.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.settlement.domain.PeriodType;
import com.example.BeGroom.settlement.dto.res.PeriodSettlementResDto;
import com.example.BeGroom.settlement.dto.res.SettlementManageResDto;
import com.example.BeGroom.settlement.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlement")
@RequiredArgsConstructor
@Tag(name = "Settlements API", description = "정산 관련 API")
public class SettlementController {

    private final SettlementService settlementService;

    // API 1. 메인 정산 관리
    @GetMapping
    @Operation(summary = "정산 관리 페이지 조회", description = "정산 관리를 조회합니다.")
    public ResponseEntity<CommonSuccessDto<SettlementManageResDto>> getSettlementManage(
            @AuthenticationPrincipal UserPrincipal userPrincipal
            ){
        SettlementManageResDto settlementManageResDto = settlementService.getSettlementManage(userPrincipal.getMemberId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                settlementManageResDto,
                                HttpStatus.OK,
                                "정산 관리 조회 성공"
                        )
                );
    }

    // API 2. 건별 정산 집계
    @GetMapping("/product")
    @Operation(summary = "건별 정산 집계", description = "건별 정산을 조회합니다.")
    public ResponseEntity<CommonSuccessDto<List<SettlementManageResDto.SettlementByItem>>> getProductSettlement(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        List<SettlementManageResDto.SettlementByItem> settlementByItemList = settlementService.getProductSettlement(userPrincipal.getMemberId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                settlementByItemList,
                                HttpStatus.OK,
                                "건별 정산 조회 성공"
                        )
                );
    }


    // API 3. 기간별 정산 집계
    @GetMapping("/period")
    @Operation(summary = "기간별 정산 집계", description = "기간별 정산을 조회합니다.")
    public ResponseEntity<CommonSuccessDto<List<PeriodSettlementResDto>>> getDailySettlement(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam PeriodType type
            ){
        List<PeriodSettlementResDto> periodSettlementResDtoList = settlementService.getPeriodSettlement(userPrincipal.getMemberId(), type);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                periodSettlementResDtoList,
                                HttpStatus.OK,
                                "일 정산 조회 성공"
                        )
                );
    }
}
