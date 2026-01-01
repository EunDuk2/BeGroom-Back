package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.settlement.domain.MonthlySettlement;
import com.example.BeGroom.settlement.domain.id.MonthlySettlementId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlySettlementRepository extends JpaRepository<MonthlySettlement, MonthlySettlementId> {
}
