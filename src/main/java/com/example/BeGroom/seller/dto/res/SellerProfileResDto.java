package com.example.BeGroom.seller.dto.res;

import com.example.BeGroom.member.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfileResDto {

    @NotEmpty
    @Schema(example = "seller@groom.co.kr")
    private String email;
    @NotEmpty
    @Schema(example = "비구름")
    private String name;
    @NotEmpty
    @Schema(example = "01012345678")
    private String phoneNumber;
    @NotEmpty
    @Schema(example = "10.00")
    private BigDecimal feeRate;
    @NotEmpty
    @Schema(example = "10")
    private Integer payoutDay;
    @NotEmpty
    @Schema(example = "SELLER")
    private Role role;
    @NotEmpty
    @Schema(example = "2026-01-07T18:45:13.280824")
    private LocalDateTime createAt;

}
