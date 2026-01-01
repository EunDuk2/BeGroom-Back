package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.settlement.domain.YearlySettlement;
import com.example.BeGroom.settlement.domain.id.YearlySettlementId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YearlySettlementRepository extends JpaRepository<YearlySettlement, YearlySettlementId> {
}
