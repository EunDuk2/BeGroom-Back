package com.example.BeGroom.wallet.dto;

import com.example.BeGroom.wallet.domain.WalletTransaction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransactionPageResDto {

    @Schema(example = "결제")
    private String transactionType;

    @Schema(example = "20000")
    private Long balanceBefore;

    @Schema(example = "-10000")
    private Long amount;

    @Schema(example = "10000")
    private Long balanceAfter;

    @Schema(example = "2025-01-01T12:00:00")
    private LocalDateTime dateTime;

    public static WalletTransactionPageResDto fromEntity(WalletTransaction walletTransaction) {
        return WalletTransactionPageResDto.builder()
                .transactionType(walletTransaction
                .getTransactionType()
                .getDisplayName())
                .balanceBefore(walletTransaction.getBalanceBefore())
                .amount(walletTransaction.getAmount())
                .balanceAfter(walletTransaction.getBalanceAfter())
                .dateTime(walletTransaction.getCreatedAt())
                .build();
    }

}
