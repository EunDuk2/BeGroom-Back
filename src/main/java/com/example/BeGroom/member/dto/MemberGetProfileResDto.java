package com.example.BeGroom.member.dto;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberGetProfileResDto {

    @NotEmpty
    @Schema(example = "example@groom.co.kr")
    private String email;

    @NotEmpty
    @Schema(example = "groomy")
    private String name;

    @NotEmpty
    @Schema(example = "010-1234-1234")
    private String phoneNumber;

    @NotEmpty
    @Schema(example = "USER")
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotEmpty
    @Schema(example = "2025.12.12")
    private LocalDateTime joinDate;

    public static MemberGetProfileResDto from(Member member){
        return MemberGetProfileResDto.builder()
                .email(member.getEmail())
                .name(member.getName())
                .phoneNumber(member.getPhoneNumber())
                .role(member.getRole())
                .joinDate(member.getCreatedAt())
                .build();
    }

}
