package com.example.BeGroom.seller.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.dto.DashboardResDto;
import com.example.BeGroom.seller.dto.OrderManageResDto;
import com.example.BeGroom.seller.dto.SellerCreateReqDto;
import com.example.BeGroom.seller.dto.SellerCreateResDto;
import com.example.BeGroom.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
@Tag(name = "Seller API", description = "판매자 관련 API")
public class SellerController {

    private final SellerService sellerService;

    // API 1. 회원가입
    @PostMapping("/join")
    @Operation(summary = "회원가입", description = "새로운 판매자를 등록합니다.")
    public ResponseEntity<CommonSuccessDto<SellerCreateResDto>> create(
            @Valid @RequestBody SellerCreateReqDto sellerCreateReqDto
    ){
        Seller seller = sellerService.create(sellerCreateReqDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                new SellerCreateResDto(seller.getId()),
                                HttpStatus.CREATED,
                                "회원가입 성공"
                        )
                );
    }

    // API 2. 판매자 대시보드 조회
    @GetMapping("/dashboard")
    @Operation(summary = "대시보드", description = "판매자 대시보드를 조회합니다.")
    public ResponseEntity<CommonSuccessDto<DashboardResDto>> getDashboard(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        DashboardResDto dashboardResDto = sellerService.getDashboard(userPrincipal.getMemberId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                dashboardResDto,
                                HttpStatus.OK,
                                "판매자 대시보드 조회 성공"
                        )
                );
    }

    // API 3. 주문 관리 조회
    @GetMapping("/order")
    @Operation(summary = "주문관리", description = "주문 관리를 조회합니다.")
    public ResponseEntity<CommonSuccessDto<OrderManageResDto>> getOrderManage(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        OrderManageResDto orderManageResDto = sellerService.getOrderManage(userPrincipal.getMemberId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                orderManageResDto,
                                HttpStatus.OK,
                                "판매자 주문간리 조회 성공"
                        )
                );
    }

}
