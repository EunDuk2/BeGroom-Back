package com.example.BeGroom.seller.dto.res;

import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.settlement.domain.SettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfoResDto {
    
    @Schema(example = "2")
    private int refundCnt;

    @Schema(example = "1")
    private int unsettledCnt;
}
