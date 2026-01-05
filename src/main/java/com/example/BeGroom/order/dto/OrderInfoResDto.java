package com.example.BeGroom.order.dto;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.wallet.domain.Wallet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderInfoResDto {
    @Schema(example = "김구름")
    private String memberName;

    @Schema(example = "01012341234")
    private String phoneNumber;

    @Schema(example = "25000")
    private Long balance;

    @Schema(example = "6990")
    private Long orderAmount;

    public static OrderInfoResDto of(Member member, Order order, Wallet wallet) {
        return OrderInfoResDto.builder()
                .memberName(member.getName())
                .phoneNumber(member.getPhoneNumber())
                .balance(wallet.getBalance())
                .orderAmount(order.getTotalAmount())
                .build();
    }
}
