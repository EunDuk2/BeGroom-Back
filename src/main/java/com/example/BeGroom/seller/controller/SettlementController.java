package com.example.BeGroom.seller.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.dto.req.SellerCreateReqDto;
import com.example.BeGroom.seller.dto.res.SellerCreateResDto;
import com.example.BeGroom.seller.dto.res.SettlementManageResDto;
import com.example.BeGroom.seller.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seller/settlement")
@RequiredArgsConstructor
@Tag(name = "Seller API", description = "판매자 관련 API")
public class SettlementController {

    private final SettlementService settlementService;

    // API 1. 메인 정산 관리
    @GetMapping
    @Operation(summary = "정산 관리", description = "정산 관리를 조회합니다.")
    public ResponseEntity<CommonSuccessDto<SettlementManageResDto>> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal
            ){
        SettlementManageResDto settlementManageResDto = settlementService.getSettlementManage(userPrincipal.getMemberId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                settlementManageResDto,
                                HttpStatus.OK,
                                "회원가입 성공"
                        )
                );
    }

    // API 2. 정산 관리(일간)

    // API 3. 정산 관리(주간)

    // API 4. 정산 관리(월간)

    // API 5. 정산 관리(년간)
}
