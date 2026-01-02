package com.example.BeGroom.order.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.dto.OrderCreateReqDto;
import com.example.BeGroom.order.dto.OrderCreateResDto;
import com.example.BeGroom.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
