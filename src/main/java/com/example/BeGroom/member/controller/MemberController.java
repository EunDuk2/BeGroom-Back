package com.example.BeGroom.member.controller;

import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.dto.MemberCreateReqDto;
import com.example.BeGroom.member.dto.MemberCreateResDto;
import com.example.BeGroom.member.service.MemberService;
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
@RequestMapping("/members")
@RequiredArgsConstructor
@Tag(name = "Member API", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "회원가입", description = "새로운 회원을 등록한다.")
    public ResponseEntity<CommonSuccessDto<MemberCreateResDto>> create(
            @Valid @RequestBody MemberCreateReqDto reqDto
    ) {
        Member member = memberService.create(reqDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                new MemberCreateResDto(member.getId()),
                                HttpStatus.CREATED,
                                "회원가입 성공"
                        )
                );
    }

}
