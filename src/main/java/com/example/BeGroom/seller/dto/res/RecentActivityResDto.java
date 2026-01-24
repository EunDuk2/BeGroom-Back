package com.example.BeGroom.seller.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityResDto {

    @Schema(description = "새로운 주문")
    private  RecentPaymentResDto recentPaymentResDto;

    @Schema(description = "최근 환불")
    private RecentRefundResDto recentRefundResDto;

    @Schema(description = "최근 정산")
    private RecentSettlementResDto recentSettlementResDto;

}
