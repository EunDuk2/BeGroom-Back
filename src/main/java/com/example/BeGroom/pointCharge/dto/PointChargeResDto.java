package com.example.BeGroom.pointCharge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointChargeReqDto {
    @NotNull
    private Long amount;
}
