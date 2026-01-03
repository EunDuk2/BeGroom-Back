package com.example.BeGroom.auth.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.auth.dto.MemberLoginReqDto;
import com.example.BeGroom.auth.dto.MemberLoginResDto;
import com.example.BeGroom.common.security.JwtTokenProvider;
import com.example.BeGroom.auth.service.AuthService;
import com.example.BeGroom.common.response.CommonSuccessDto;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.domain.Role;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.seller.domain.Seller;
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
    private final MemberNotificationRepository memberNotificationRepository;

    @PostMapping
    @Operation(summary = "로그인", description = "로그인에 성공하면 JWT 토큰을 반환한다.")
    public ResponseEntity<CommonSuccessDto<MemberLoginResDto>> login(
            @Valid @RequestBody MemberLoginReqDto reqDto
    ) {
        String jwtToken = "";
        Long userId = 0L;
        String name = "";
        String email = "";
        Long unreadNotisCount = 0L;
        Role role = null;

        if(reqDto.getRole() == Role.SELLER){
            Seller seller = authService.sellerLogin(reqDto);
            userId = seller.getId();
            name = seller.getName();
            email = seller.getEmail();
            role = seller.getRole();

            jwtToken = jwtTokenProvider.createToken(
                    seller.getId(),
                    seller.getEmail(),
                    seller.getRole().toString()
            );
        }else{
            Member member = authService.memberLogin(reqDto);
            userId = member.getId();
            name = member.getName();
            email = member.getEmail();
            role = member.getRole();

            jwtToken = jwtTokenProvider.createToken(
                    member.getId(),
                    member.getEmail(),
                    member.getRole().toString()
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonSuccessDto.of(
                                new MemberLoginResDto(jwtToken, userId, name, email, memberNotificationRepository.countByMemberIdAndIsReadFalse(userId), role),
                                HttpStatus.OK,
                                "로그인 성공"
                        )
                );
    }


}
