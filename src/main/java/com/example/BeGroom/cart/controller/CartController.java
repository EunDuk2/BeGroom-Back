package com.example.BeGroom.cart.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.cart.dto.CartItemAddReqDto;
import com.example.BeGroom.cart.dto.CartItemUpdateQuantityReqDto;
import com.example.BeGroom.cart.dto.CartItemUpdateSelectedReqDto;
import com.example.BeGroom.cart.dto.CartResDto;
import com.example.BeGroom.cart.service.CartService;
import com.example.BeGroom.common.response.CommonSuccessDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Cart API", description = "장바구니 API")
public class CartController {

    private final CartService cartService;

    // 장바구니 조회
    @GetMapping
    @Operation(summary = "장바구니 조회", description = "회원의 장바구니 전체 조회")
    public ResponseEntity<CommonSuccessDto<CartResDto>> getCart(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        CartResDto cart = cartService.getCart(user.getMemberId());

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        cart,
                        HttpStatus.OK,
                        "장바구니 조회 성공"
                )
        );
    }

    // 장바구니에 상품 추가
    @PostMapping("/items")
    @Operation(summary = "장바구니 상품 추가", description = "장바구니에 상품 추가")
    public ResponseEntity<CommonSuccessDto<Void>> addItem(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CartItemAddReqDto request
    ) {
        cartService.addItem(user.getMemberId(), request.getProductDetailId(), request.getQuantity());

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        null,
                        HttpStatus.OK,
                        "장바구니에 상품을 추가했습니다."
                )
        );
    }

    // 장바구니 상품 수량 변경
    @PutMapping("/items/{cartItemId}/quantity")
    @Operation(summary = "상품 수량 변경", description = "장바구니 상품 수량 변경(숫자)")
    public ResponseEntity<CommonSuccessDto<Void>> updateQuantity(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateQuantityReqDto request
    ) {
        cartService.updateQuantity(user.getMemberId(), cartItemId, request.getQuantity());

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        null,
                        HttpStatus.OK,
                        "상품 수량을 변경했습니다."
                )
        );
    }

    // 장바구니 상품 수량 증가
    @PatchMapping("/items/{cartItemId}/increase")
    @Operation(summary = "상품 수량 증가", description = "+ 버튼 클릭 시 사용")
    public ResponseEntity<CommonSuccessDto<Void>> increaseQuantity(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long cartItemId
    ) {
        cartService.increaseQuantity(user.getMemberId(), cartItemId, 1);

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        null,
                        HttpStatus.OK,
                        "상품 수량을 증가했습니다."
                )
        );
    }

    // 장바구니 상품 수량 감소
    @PatchMapping("/items/{cartItemId}/decrease")
    @Operation(summary = "상품 수량 감소", description = "- 버튼 클릭 시 사용")
    public ResponseEntity<CommonSuccessDto<Void>> decreaseQuantity(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long cartItemId
    ) {
        cartService.decreaseQuantity(user.getMemberId(), cartItemId, 1);

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        null,
                        HttpStatus.OK,
                        "상품 수량을 감소했습니다."
                )
        );
    }

    // 장바구니 상품 선택 상태 변경
    @PutMapping("/items/{cartItemId}/select")
    @Operation(summary = "상품 선택 상태 변경", description = "개별 상품 선택/선택 해제")
    public ResponseEntity<CommonSuccessDto<Void>> updateSelected(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateSelectedReqDto request
    ) {
        cartService.updateSelected(user.getMemberId(), cartItemId, request.getIsSelected());

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        null,
                        HttpStatus.OK,
                        "상품 선택 상태를 변경했습니다."
                )
        );
    }

    // 전체 선택
    @PutMapping("/select-all")
    @Operation(summary = "상품 전체 선택", description = "장바구니의 모든 상품 선택")
    public ResponseEntity<CommonSuccessDto<Void>> selectAll(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        cartService.selectAll(user.getMemberId());

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        null,
                        HttpStatus.OK,
                        "장바구니의 모든 상품을 선택했습니다."
                )
        );
    }

    // 전체 선택 해제
    @PutMapping("/deselect-all")
    @Operation(summary = "상품 전체 선택 해제", description = "장바구니의 모든 상품 선택 해제")
    public ResponseEntity<CommonSuccessDto<Void>> deselectAll(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        cartService.deselectAll(user.getMemberId());

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        null,
                        HttpStatus.OK,
                        "장바구니의 모든 상품을 선택 해제했습니다."
                )
        );
    }

    // 장바구니 상품 삭제
    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "장바구니 상품 삭제", description = "개별 상품 삭제")
    public ResponseEntity<CommonSuccessDto<Void>> deleteItem(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long cartItemId
    ) {
        cartService.deleteItem(user.getMemberId(), cartItemId);

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        null,
                        HttpStatus.OK,
                        "장바구니에서 상품을 삭제했습니다."
                )
        );
    }

    // 선택된 상품 삭제
    @DeleteMapping("/selected")
    @Operation(summary = "선택된 상품 삭제", description = "선택된 상품만 삭제")
    public ResponseEntity<CommonSuccessDto<Void>> deleteSelectedItems(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        cartService.deleteSelectedItems(user.getMemberId());

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        null,
                        HttpStatus.OK,
                        "선택된 상품을 삭제했습니다."
                )
        );
    }

    // 장바구니 비우기 (전체 상품 삭제)
    @DeleteMapping
    @Operation(summary = "장바구니 비우기", description = "장바구니의 모든 상품 삭제")
    public ResponseEntity<CommonSuccessDto<Void>> clearCart(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        cartService.clearCart(user.getMemberId());

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        null,
                        HttpStatus.OK,
                        "장바구니를 비웠습니다."
                )
        );
    }

    // 장바구니 상품 개수 조회
    @GetMapping("/count")
    @Operation(summary = "장바구니 상품 개수", description = "장바구니에 담긴 상품 개수 조회")
    public ResponseEntity<CommonSuccessDto<Long>> getItemCount(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        long count = cartService.getItemCount(user.getMemberId());

        return ResponseEntity.ok(
                CommonSuccessDto.of(
                        count,
                        HttpStatus.OK,
                        "장바구니 상품 개수 조회 성공"
                )
        );
    }
}
