package com.example.BeGroom.auth.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.auth.dto.MemberLoginReqDto;
import com.example.BeGroom.auth.dto.MemberLoginResDto;
import com.example.BeGroom.common.security.JwtTokenProvider;
import com.example.BeGroom.auth.service.AuthService;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.member.domain.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @Operation(summary = "로그인", description = "로그인에 성공하면 JWT 토큰을 반환한다.")
    public ResponseEntity<CommonSuccessDto<MemberLoginResDto>> login(
            @Valid @RequestBody MemberLoginReqDto reqDto
    ) {
        Member member = authService.login(reqDto);
        String jwtToken = jwtTokenProvider.createToken(
                member.getId(),
                member.getEmail(),
                member.getRole().toString()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                new MemberLoginResDto(jwtToken),
                                HttpStatus.CREATED,
                                "로그인 성공"
                        )
                );
    }


}
