package com.example.BeGroom.member.dto;

import com.example.BeGroom.wallet.domain.Wallet;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class GetMemberWalletResDto {
    private WalletSummary wallet;
    private Page<TransactionSummary> transactions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WalletSummary {
        private Long id;
        private Long balance;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionSummary {
        private Long id;

        @JsonProperty("tx_type")
        private String txType;

        private Long amount;

        @JsonProperty("balance_after")
        private Long balanceAfter;

        @JsonProperty("created_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private LocalDateTime createdAt;

        private String description;
    }

    public static GetMemberWalletResDto of(WalletSummary wallet, Page<TransactionSummary> transactions) {
        return new GetMemberWalletResDto(wallet, transactions);
    }
}
