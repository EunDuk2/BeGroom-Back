package com.example.BeGroom.usecase.cancelOrder.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.usecase.cancelOrder.dto.CancelOrderResDto;
import com.example.BeGroom.usecase.cancelOrder.service.CancelOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CancelOrderController {

    private final CancelOrderService cancelOrderService;

    @PostMapping("/orders/{orderId}/cancel")
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
