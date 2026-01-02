package com.example.BeGroom.auth.dto;

import com.example.BeGroom.member.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberLoginResDto {

    @NotEmpty
    @Schema(example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJob25nQG5hdmVyLmNvbSIsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzY2NzM5NjAzLCJleHAiOjE3NjY3OTk2MDN9.N4fVghRunMOYOHjTHPXnEPoPBvxymO0GPFvzd-ORot_ZZxGqaLKF1x2LIgeXdH8pwMqXlDqQI9_7A0w2FiV7pw")
    private String token;

    private Long memberId;
    private String name;
    private String email;
    private Long unreadNotisCount;
    private Role role;
}
