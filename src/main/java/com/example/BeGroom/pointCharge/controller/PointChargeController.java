package com.example.BeGroom.pointCharge.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.pointCharge.dto.PointChargeReqDto;
import com.example.BeGroom.pointCharge.dto.PointChargeResDto;
import com.example.BeGroom.pointCharge.service.PointChargeService;
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
@RequestMapping("/point-charges")
@Tag(name = "PointCharge API", description = "포인트 충전 관련 API")
public class PointChargeController {

    private final PointChargeService pointChargeService;

    @PostMapping
    @Operation(summary = "포인트 충전", description = "특정 회원의 포인트를 충전하고, 기록한다.")
    public ResponseEntity<CommonSuccessDto<PointChargeResDto>> pointCharge(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody PointChargeReqDto reqDto
            ) {
        PointChargeResDto pointChargeResDto = pointChargeService.pointCharge(user.getMemberId(), reqDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                pointChargeResDto,
                                HttpStatus.CREATED,
                                "포인트 충전 성공 및 충전 기록 저장 성공"
                        )
                );
    }
}
