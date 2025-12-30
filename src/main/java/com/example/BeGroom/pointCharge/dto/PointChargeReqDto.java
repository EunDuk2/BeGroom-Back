package com.example.BeGroom.pointCharge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointChargeReqDto {

    @NotNull
    @Schema(example = "10000")
    private Long amount;
}
