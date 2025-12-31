package com.example.BeGroom.auth.dto;

import com.example.BeGroom.member.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberLoginReqDto {

    @NotEmpty
    @Schema(example = "hong@naver.com")
    private String email;

    @NotEmpty
    @Schema(example = "1234")
    private String password;

    // 사용자 판매자 구분
    @NotNull
    @Schema(example = "SELLER")
    private Role role;

}
