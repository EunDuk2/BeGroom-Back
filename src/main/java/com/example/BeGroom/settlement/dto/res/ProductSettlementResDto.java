package com.example.BeGroom.settlement.dto.res;

import com.example.BeGroom.settlement.domain.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSettlementResDto {

    @NotNull
    @Schema(example = "1")
    private Long id;

    @NotNull
    @Schema(example = "2025-01-01T14:30:00")
    private LocalDateTime paidAt;

    @Schema(example = "128000")
    private long paymentAmount;

    @NotNull
    @Schema(example = "9000")
    private Long refundAmount;

    @Schema(example = "12800")
    private long feeAmount;

    @Schema(example = "107100")
    private long settlementAmount;

    @NotNull
    @Schema(example = "SETTLED")
    private SettlementStatus settlementStatus;

}
