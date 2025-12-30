package com.example.BeGroom.wallet.repository;

import com.example.BeGroom.wallet.domain.Wallet;
import com.example.BeGroom.wallet.domain.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Page<WalletTransaction> findByWalletOrderByCreatedAtDesc(
            Wallet wallet,
            Pageable pageable
    );
}
