package com.example.BeGroom.wallet.service;

import com.example.BeGroom.wallet.dto.WalletTransactionPageResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WalletTransactionService {
    Page<WalletTransactionPageResDto> getWalletTransactionPage(Long memberId, Pageable pageable);
}
