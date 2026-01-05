package com.example.BeGroom.seller.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.dto.res.*;
import com.example.BeGroom.seller.dto.req.SellerCreateReqDto;
import com.example.BeGroom.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    // API 2. 판매자 대시보드 요약 정보 조회
    @GetMapping("/dashboard")
    @Operation(summary = "대시보드 페이지 조회", description = "대시보드의 요약 정보를 조회합니다.")
    public ResponseEntity<CommonSuccessDto<DashboardResDto>> getDashboard(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        DashboardResDto dashboardResDto = sellerService.getDashboard(userPrincipal.getMemberId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                dashboardResDto,
                                HttpStatus.OK,
                                "판매자 대시보드 요약 정보 조회 성공"
                        )
                );
    }

    // API 3. 주문 관리 요약 정보 조회
    @GetMapping("/dashboard/order/info")
    @Operation(summary = "주문 관리 페이지 조회", description = "주문 관리 페이지 요약 정보를 조회합니다.")
    public ResponseEntity<CommonSuccessDto<OrderInfoResDto>> getOrderInfo(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        OrderInfoResDto orderInfoResDto = sellerService.getOrderInfo(userPrincipal.getMemberId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                orderInfoResDto,
                                HttpStatus.OK,
                                "주문 관리 페이지 조회 성공"
                        )
                );
    }

    // API 4. 주문 관리 주문 목록 조회
    @GetMapping("/dashboard/order/list")
    @Operation(summary = "주문 목록 조회", description = "주문 관리 페이지 주문 목록 조회합니다.")
    public ResponseEntity<CommonSuccessDto<Page<OrderListResDto>>> getOrderList(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page
    ){
        Page<OrderListResDto> orderList =
                sellerService.getOrderList(userPrincipal.getMemberId(), page);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                orderList,
                                HttpStatus.OK,
                                "주문 목록 조회 성공"
                        )
                );
    }

    // API 5. 최근 활동
    @GetMapping("/dashboard/recent")
    @Operation(summary = "대시보드 최근 활동 조회", description = "대시보드의 최근 활동 내역을 조회합니다.")
    public ResponseEntity<CommonSuccessDto<RecentActivityResDto>> getRecentActivities(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        RecentActivityResDto recentActivityResDto = sellerService.getRecentActivities(userPrincipal.getMemberId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                recentActivityResDto,
                                HttpStatus.OK,
                                "대시보드 최근 활동 조회 성공"
                        )
                );
    }

}
