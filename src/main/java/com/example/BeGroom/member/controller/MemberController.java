package com.example.BeGroom.member.controller;

import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.dto.MemberCreateReqDto;
import com.example.BeGroom.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody MemberCreateReqDto reqDto) {
        Member member = memberService.create(reqDto);
        return new ResponseEntity<>(
                CommonSuccessDto.builder()
                        .result(member.getId())
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("회원가입 성공")
                        .build()
                , HttpStatus.CREATED);
    }

}
