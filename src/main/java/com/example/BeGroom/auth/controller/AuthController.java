package com.example.BeGroom.auth.controller;

import com.example.BeGroom.auth.dto.MemberLoginReqDto;
import com.example.BeGroom.common.security.JwtTokenProvider;
import com.example.BeGroom.auth.service.AuthService;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.member.domain.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<?> login(@Valid @RequestBody MemberLoginReqDto reqDto) {
        Member member = authService.login(reqDto);
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());

        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result(jwtToken)
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("로그인 성공")
                        .build()
                , HttpStatus.CREATED);
    }

}
