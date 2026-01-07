package com.example.BeGroom.settlement.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSettlementReqDto {

    @Schema(example = "2025-12-01", description = "조회 시작일")
    private LocalDate startDate;

    @Schema(example = "2025-12-01", description = "조회 종료일")
    private LocalDate endDate;

}
