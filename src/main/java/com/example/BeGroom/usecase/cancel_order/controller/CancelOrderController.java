package com.example.BeGroom.usecase.cancel_order.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.usecase.cancel_order.dto.CancelOrderResDto;
import com.example.BeGroom.usecase.cancel_order.service.CancelOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "CancelOrder API", description = "주문 취소 유스케이스 API")
public class CancelOrderController {

    private final CancelOrderService cancelOrderService;

    @PostMapping("/orders/{orderId}/cancel")
    @Operation(summary = "주문 취소", description = "결제완료된 주문을 취소한다.")
    public ResponseEntity<CommonSuccessDto<CancelOrderResDto>> cancelOrder(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long orderId
    ) {

        cancelOrderService.cancelOrder(user.getMemberId(), orderId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                new CancelOrderResDto(orderId),
                                HttpStatus.OK,
                                "주문 취소 성공"
                        )
                );
    }
}
