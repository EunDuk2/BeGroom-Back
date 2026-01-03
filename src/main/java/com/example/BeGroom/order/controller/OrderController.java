package com.example.BeGroom.order.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.dto.OrderCreateReqDto;
import com.example.BeGroom.order.dto.OrderCreateResDto;
import com.example.BeGroom.order.dto.OrderSummaryDto;
import com.example.BeGroom.order.service.OrderService;
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
}
