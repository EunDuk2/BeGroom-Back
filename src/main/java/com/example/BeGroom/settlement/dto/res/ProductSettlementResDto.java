package com.example.BeGroom.settlement.dto.res;

import com.example.BeGroom.settlement.domain.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSettlementResDto {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "2025-01-01T14:30:00")
    private LocalDate date;

    @Schema(example = "128000")
    private Long paymentAmount;

    @Schema(example = "9000")
    private BigDecimal refundAmount;

    @Schema(example = "12800")
    private BigDecimal feeAmount;

    @Schema(example = "107100")
    private BigDecimal settlementAmount;

    @Schema(example = "SETTLED")
    private SettlementStatus settlementStatus;

}
