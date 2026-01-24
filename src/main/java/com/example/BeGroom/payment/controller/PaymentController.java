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
public class PaymentController {

}
