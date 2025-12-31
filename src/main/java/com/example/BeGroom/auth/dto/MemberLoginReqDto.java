package com.example.BeGroom.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
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

    // 구분: 사용자(1), 판매자(2)
    private int no;

}
