package com.example.BeGroom.wallet.repository;

import com.example.BeGroom.wallet.domain.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
}
