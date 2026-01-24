package com.example.BeGroom.wallet.domain;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.example.BeGroom.wallet.domain.ReferenceType.ORDER;
import static com.example.BeGroom.wallet.domain.TransactionType.PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @DisplayName("결제 가능 여부를 확인한다.")
    @Test
    void canPay_true() {
        // given
        Member member = createMember();
        Wallet wallet = Wallet.create(member, 10000);

        // when
        boolean result = wallet.canPay(10000L);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("결제가 가능하면 포인트를 차감하고 원장을 기록한다.")
    @Test
    void pay_success() {
        // given
        Member member = createMember();
        Wallet wallet = Wallet.create(member, 10000);

        // when
        wallet.pay(10000L, ORDER, 1L);

        // then
        assertThat(wallet.getBalance()).isZero();
        assertThat(wallet.getTransactions()).hasSize(1)
                .extracting(
                        WalletTransaction::getBalanceBefore,
                        WalletTransaction::getAmount,
                        WalletTransaction::getBalanceAfter,
                        WalletTransaction::getTransactionType,
                        WalletTransaction::getReferenceType,
                        WalletTransaction::getReferenceId
                )
                .containsExactlyInAnyOrder(
                        tuple(10000L, -10000L, 0L, PAYMENT, ORDER, 1L)
                );
    }


    /* =========================
     *  테스트 내부 공용 메서드 (Given)
     * ========================= */

    private Member createMember() {
        return Member.createMember(
                "test@naver.com",
                "test",
                "1234",
                "010",
                Role.USER
        );
    }
}