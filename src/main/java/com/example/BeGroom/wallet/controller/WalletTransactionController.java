package com.example.BeGroom.wallet.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.wallet.dto.WalletTransactionPageResDto;
import com.example.BeGroom.wallet.service.WalletTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet-transactions")
@RequiredArgsConstructor
@Tag(name = "Wallet Transaction API", description = "포인트 원장 API")
public class WalletTransactionController {

    private final WalletTransactionService walletTransactionService;

    @GetMapping
    @Operation(summary = "포인트 원장 조회", description = "로그인한 회원의 원장 리스트(페이지)를 조회한다.")
    public ResponseEntity<CommonSuccessDto<Page<WalletTransactionPageResDto>>> getWalletTransactions(
            @AuthenticationPrincipal UserPrincipal user,
            @PageableDefault(size = 10)Pageable pageable
            ) {

        Page<WalletTransactionPageResDto> pageResDtos
                = walletTransactionService.getWalletTransactionPage(user.getMemberId(), pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                pageResDtos,
                                HttpStatus.OK,
                                "포인트 원장 페이지 조회 성공"
                        )
                );
    }
}
