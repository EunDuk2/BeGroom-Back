package com.example.BeGroom.member.dto;

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
public class MemberCreateResDto {
    @NotEmpty
    @Schema(example = "1")
    private Long memberId;
}
