package com.example.BeGroom.wallet.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.wallet.dto.WalletBalanceResDto;
import com.example.BeGroom.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wallet")
@Tag(name = "Wallet API", description = "포인트 관련 API")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/balance")
    @Operation(summary = "포인트 잔액 조회", description = "로그인한 회원의 잔액을 조회한다.")
    public ResponseEntity<CommonSuccessDto<WalletBalanceResDto>> getBalance(@AuthenticationPrincipal UserPrincipal user) {
        Long balance = walletService.getBalance(user.getMemberId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                new WalletBalanceResDto(balance),
                                HttpStatus.OK,
                                "잔액 조회 성공"
                        )
                );
    }

}
