package com.example.BeGroom.order.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.dto.*;
import com.example.BeGroom.order.service.OrderService;
import com.example.BeGroom.usecase.checkout.dto.CheckoutReqDto;
import com.example.BeGroom.usecase.checkout.dto.CheckoutResDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@Tag(name = "Order API", description = "주문 관련 API")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "주문생성", description = "새로운 주문을 생성한다.")
    public ResponseEntity<CommonSuccessDto<OrderCreateResDto>> create(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody OrderCreateReqDto reqDto
    ) {

        Order order = orderService.create(user.getMemberId(), reqDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                new OrderCreateResDto(order.getId()),
                                HttpStatus.CREATED,
                                "주문 생성 성공"
                        )
                );
    }

    @PostMapping("/{orderId}/checkout")
    public ResponseEntity<CommonSuccessDto<CheckoutResDto>> checkout(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long orderId,
            @Valid @RequestBody CheckoutReqDto reqDto
    ) {
        CheckoutResDto checkoutResDto
                = orderService.checkout(user.getMemberId(), orderId, reqDto.getPaymentMethod());

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                checkoutResDto,
                                HttpStatus.OK,
                                "결제 요청 성공"
                        )
                );
    }



    @GetMapping
    @Operation(summary = "주문 내역 조회", description = "로그인한 사용자의 주문 내역을 조회한다.")
    public ResponseEntity<CommonSuccessDto<Page<OrderSummaryDto>>> getOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        Page<OrderSummaryDto> page = orderService.getOrders(pageable, user.getMemberId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                page,
                                HttpStatus.OK,
                                "주문 내역 조회 성공"
                        )
                );
    }

    @GetMapping("/detail/{orderId}")
    @Operation(summary = "주문 상세 내역 조회", description = "주문 상세 내역을 조회한다.")
    public ResponseEntity<CommonSuccessDto<OrderDetailResDto>> getOrderDetail(
            @PathVariable Long orderId
    ) {

        OrderDetailResDto orderDetailResDto = orderService.getOrderDetail(orderId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                orderDetailResDto,
                                HttpStatus.OK,
                                "주문 상세 내역 조회 성공"
                        )
                );
    }

    @GetMapping("/{orderId}/info")
    @Operation(summary = "주문 정보 조회", description = "주문 정보를 조회한다.")
    public ResponseEntity<CommonSuccessDto<OrderInfoResDto>> getOrderInfo(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long orderId
    ) {
        OrderInfoResDto orderInfoResDto = orderService.getOrderInfo(user.getMemberId(), orderId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        CommonSuccessDto.of(
                                orderInfoResDto,
                                HttpStatus.OK,
                                "주문 정보 조회 성공"
                        )
                );
    }
}
