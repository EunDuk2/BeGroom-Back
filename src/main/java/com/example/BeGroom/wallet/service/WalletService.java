package com.example.BeGroom.wallet.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.wallet.domain.Wallet;

public interface WalletService {
    Wallet create(Member member);
}
