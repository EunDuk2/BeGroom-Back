package com.example.BeGroom.seller.controller;

import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.dto.SellerCreateReqDto;
import com.example.BeGroom.seller.dto.SellerCreateResDto;
import com.example.BeGroom.seller.service.SellerService;
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
@RequestMapping("/seller")
@RequiredArgsConstructor
@Tag(name = "Seller API", description = "판매자 관련 API")
public class SellerController {

    private final SellerService sellerService;

    // API 1. 회원가입
    @PostMapping("/join")
    @Operation(summary = "회원가입", description = "새로운 판매자를 등록합니다.")
    public ResponseEntity<CommonSuccessDto<SellerCreateResDto>> create(
            @Valid @RequestBody SellerCreateReqDto sellerCreateReqDto
    ){
        Seller seller = sellerService.create(sellerCreateReqDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                new SellerCreateResDto(seller.getId()),
                                HttpStatus.CREATED,
                                "회원가입 성공"
                        )
                );
    }

}
