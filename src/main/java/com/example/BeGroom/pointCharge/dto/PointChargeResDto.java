package com.example.BeGroom.pointCharge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointChargeResDto {
    @Schema(example = "1")
    private Long pointChargeId;
    @Schema(example = "25000")
    private Long balance;
}
