package com.example.BeGroom.settlement.dto.res;

import com.example.BeGroom.settlement.domain.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementManageResDto {

    @Schema(example = "2300000")
    private int totalPaymentAmount;

    @Schema(example = "66000")
    private int totalRefundAmount;

    @Schema(example = "223400")
    private int totalFeeAmount;

    @Schema(example = "2010600")
    private int totalSettlementAmount;

}
