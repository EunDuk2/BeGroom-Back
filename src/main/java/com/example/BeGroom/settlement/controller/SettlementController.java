package com.example.BeGroom.settlement.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.settlement.domain.PeriodType;
import com.example.BeGroom.settlement.dto.res.*;
import com.example.BeGroom.settlement.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/settlement")
@RequiredArgsConstructor
@Tag(name = "Settlements API", description = "정산 관련 API")
public class SettlementController {

    private final SettlementService settlementService;

    // API 1. 정산 요약 정보 조회
    @GetMapping
    @Operation(summary = "정산 요약 정보 조회", description = "정산 요약 정보를 조회합니다.")
    public ResponseEntity<CommonSuccessDto<SettlementManageResDto>> getSettlementManage(
            @AuthenticationPrincipal UserPrincipal userPrincipal
            ){
        SettlementManageResDto settlementManageResDto = settlementService.getSettlementManage(userPrincipal.getMemberId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                settlementManageResDto,
                                HttpStatus.OK,
                                "정산 요약 정보 조회 성공"
                        )
                );
    }

    // API 2. 건별 정산 집계
    @GetMapping("/product")
    @Operation(summary = "건별 정산 집계", description = "건별 정산을 조회합니다.")
    public ResponseEntity<CommonSuccessDto<Page<ProductSettlementResDto>>> getProductSettlement(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page
    ){
        Page<ProductSettlementResDto> settlementByItemList =
                settlementService.getProductSettlement(userPrincipal.getMemberId(), startDate, endDate, page);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                settlementByItemList,
                                HttpStatus.OK,
                                "건별 정산 조회 성공"
                        )
                );
    }


    // API 3. 일별 정산 집계
    @GetMapping("/period/daily")
    @Operation(summary = "일별 정산 집계", description = "일별 정산을 조회합니다.")
    public ResponseEntity<CommonSuccessDto<Page<DailySettlementResDto>>> getDailySettlement(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page
            ){
        Page<DailySettlementResDto> dailySettlementResDtos =
                settlementService.getDailySettlement(userPrincipal.getMemberId(), page);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                dailySettlementResDtos,
                                HttpStatus.OK,
                                "일별 정산 조회 성공"
                        )
                );
    }

    // API 4. 주차별 정산 집계
    @GetMapping("/period/weekly")
    @Operation(summary = "주차별 정산 집계", description = "주차별 정산을 조회합니다.")
    public ResponseEntity<CommonSuccessDto<Page<WeeklySettlementResDto>>> getWeeklySettlement(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page
    ){
        Page<WeeklySettlementResDto> weeklySettlementResDtos =
                settlementService.getWeeklySettlement(userPrincipal.getMemberId(), page);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                weeklySettlementResDtos,
                                HttpStatus.OK,
                                "주차별 정산 조회 성공"
                        )
                );
    }

    // API 5. 월별 정산 집계
    @GetMapping("/period/monthly")
    @Operation(summary = "월별 정산 집계", description = "월별 정산을 조회합니다.")
    public ResponseEntity<CommonSuccessDto<Page<MonthlySettlementResDto>>> getMonthlySettlement(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page
    ){
        Page<MonthlySettlementResDto> monthlySettlementResDtos =
                settlementService.getMonthlySettlement(userPrincipal.getMemberId(), page);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                monthlySettlementResDtos,
                                HttpStatus.OK,
                                "월별 정산 조회 성공"
                        )
                );
    }

    // API 6. 연도별 정산 집계
    @GetMapping("/period/yearly")
    @Operation(summary = "연도별 정산 집계", description = "연도별 정산을 조회합니다.")
    public ResponseEntity<CommonSuccessDto<Page<YearlySettlementResDto>>> getYearlySettlement(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page
    ){
        Page<YearlySettlementResDto> yearlySettlementResDtos =
                settlementService.getYearlySettlement(userPrincipal.getMemberId(), page);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                yearlySettlementResDtos,
                                HttpStatus.OK,
                                "연도별 정산 조회 성공"
                        )
                );
    }
}
