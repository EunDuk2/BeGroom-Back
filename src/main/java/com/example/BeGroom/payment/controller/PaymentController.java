package com.example.BeGroom.payment.controller;

import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.dto.PaymentCreateReqDto;
import com.example.BeGroom.payment.dto.PaymentCreateResDto;
import com.example.BeGroom.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
@Tag(name = "Payment API", description = "결제 관련 API")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "결제 생성", description = "생성된 주문에 대한 결제를 생성한다.")
    public ResponseEntity<CommonSuccessDto<PaymentCreateResDto>> create(
            @Valid @RequestBody PaymentCreateReqDto reqDto
            ) {

        Payment payment = paymentService.create(reqDto.getOrderId(), reqDto.getPaymentMethod());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                new PaymentCreateResDto(payment.getId()),
                                HttpStatus.CREATED,
                                "결제 생성 성공"
                        )
                );
    }


}
