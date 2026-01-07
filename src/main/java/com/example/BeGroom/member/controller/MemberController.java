package com.example.BeGroom.member.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.dto.*;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.member.service.MemberService;
import com.example.BeGroom.wallet.domain.Wallet;
import com.example.BeGroom.wallet.domain.WalletTransaction;
import com.example.BeGroom.wallet.repository.WalletRepository;
import com.example.BeGroom.wallet.repository.WalletTransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Tag(name = "Member API", description = "회원 관련 API")
public class MemberController {
    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "회원가입", description = "새로운 회원을 등록한다.")
    public ResponseEntity<CommonSuccessDto<MemberCreateResDto>> create(
            @Valid @RequestBody MemberCreateReqDto reqDto
    ) {
        Member member = memberService.create(reqDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                new MemberCreateResDto(member.getId()),
                                HttpStatus.CREATED,
                                "회원가입 성공"
                        )
                );
    }

    @GetMapping("/profile")
    @Operation(summary = "프로필 불러오기", description = "회원의 프로필 정보를 불러온다.")
    public ResponseEntity<CommonSuccessDto<MemberGetProfileResDto>> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if(userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = userPrincipal.getEmail();
        MemberGetProfileResDto responseDto = memberService.getMyProfile(email);
        CommonSuccessDto<MemberGetProfileResDto> commonResponse = CommonSuccessDto.of(responseDto, HttpStatus.OK, "get profile success");
        return ResponseEntity.ok(commonResponse);
    }

    @GetMapping("/orders")
    @Operation(summary = "주문 불러오기", description = "회원의 주문 정보를 불러온다.")
    public ResponseEntity<CommonSuccessDto<GetMemberOrdersResDto>> getMyOrders(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if(userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long memberId = userPrincipal.getMemberId();
        GetMemberOrdersResDto responseDto = memberService.getMyOrders(memberId);
        CommonSuccessDto<GetMemberOrdersResDto> commonResponse = CommonSuccessDto.of(responseDto, HttpStatus.OK, "get orders success");
        return ResponseEntity.ok(commonResponse);
    }

    @GetMapping("/wallet")
    @Operation(summary = "회원 지갑 불러오기", description = "회원의 캐시와 거래 내역을 불러온다.")
    public ResponseEntity<CommonSuccessDto<GetMemberWalletResDto>> getMyWallet(@AuthenticationPrincipal UserPrincipal userPrincipal,
    @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if(userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long memberId = userPrincipal.getMemberId();

        GetMemberWalletResDto responseDto = memberService.getWalletTransactions(memberId, pageable);
        CommonSuccessDto<GetMemberWalletResDto> commonResponse = CommonSuccessDto.of(responseDto, HttpStatus.OK, "get wallet success");
        return ResponseEntity.ok(commonResponse);
    }

    @GetMapping("/wish")
    @Operation(summary = "회원 위시리스트 불러오기", description = "회원의 위시리스트를 불러온다.")
    public ResponseEntity<CommonSuccessDto<GetMemberWishesResDto>> getMyWishes(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if(userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long memberId = userPrincipal.getMemberId();
        GetMemberWishesResDto responseDto = memberService.getMyWishes(memberId);
        CommonSuccessDto<GetMemberWishesResDto> commonResponse = CommonSuccessDto.of(responseDto, HttpStatus.OK, "get wishlist success");
        return ResponseEntity.ok(commonResponse);
    }
}
