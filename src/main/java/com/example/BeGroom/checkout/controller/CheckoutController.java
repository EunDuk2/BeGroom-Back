package com.example.BeGroom.checkout.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.checkout.dto.CheckoutReqDto;
import com.example.BeGroom.checkout.dto.CheckoutResDto;
import com.example.BeGroom.checkout.service.CheckoutService;
import com.example.BeGroom.common.response.CommonSuccessDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Checkout API", description = "결제 요청 API")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/payments/{paymentId}/checkout")
    @Operation(summary = "결제 완료 요청", description = "생성된 결제에 대한 결제 완료를 요청한다.")
    public ResponseEntity<CommonSuccessDto<CheckoutResDto>> checkout(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long paymentId,
            @Valid @RequestBody CheckoutReqDto reqDto
            ) {

        CheckoutResDto checkoutResDto
                = checkoutService.checkout(paymentId, reqDto.getPaymentMethod());

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                checkoutResDto,
                                HttpStatus.OK,
                                "결제 완료 요청 성공"
                        )
                );
    }

}
