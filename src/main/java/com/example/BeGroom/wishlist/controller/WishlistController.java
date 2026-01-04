package com.example.BeGroom.wishlist.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.wishlist.dto.WishlistListResDto;
import com.example.BeGroom.wishlist.dto.WishlistResDto;
import com.example.BeGroom.wishlist.dto.WishlistToggleReqDto;
import com.example.BeGroom.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Wishlist API", description = "위시리스트 API")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "위시리스트 조회", description = "회원의 위시리스트 전체 조회 (기본: 최신순 정렬)")
    public ResponseEntity<CommonSuccessDto<WishlistListResDto>> getWishlist(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(sortDirection, sort);

        List<WishlistResDto> items = wishlistService.getWishlist(user.getMemberId(), sortObj);
        WishlistListResDto response = WishlistListResDto.from(items);

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        response,
                        HttpStatus.OK,
                        "위시리스트 조회 성공"
                )
        );
    }

    @PostMapping("/toggle")
    @Operation(summary = "위시리스트 토글", description = "위시리스트에 상품 추가 또는 삭제")
    public ResponseEntity<CommonSuccessDto<Void>> toggleWishlist(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody WishlistToggleReqDto request
    ) {
        wishlistService.toggleWishlist(user.getMemberId(), request.getProductId());

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        null,
                        HttpStatus.OK,
                        "위시리스트 상태가 변경되었습니다."
                )
        );
    }

    @GetMapping("/count")
    @Operation(summary = "위시리스트 상품 개수 조회", description = "회원의 위시리스트 상품 개수 조회")
    public ResponseEntity<CommonSuccessDto<Long>> getWishlistCount(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        long count = wishlistService.getWishlistCount(user.getMemberId());

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        count,
                        HttpStatus.OK,
                        "위시리스트 상품 개수 조회 성공"
                )
        );
    }
}
