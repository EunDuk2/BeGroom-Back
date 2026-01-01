package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.settlement.domain.DailySettlement;
import com.example.BeGroom.settlement.domain.id.DailySettlementId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailySettlementRepository extends JpaRepository<DailySettlement, DailySettlementId> {
}
