package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.settlement.domain.WeeklySettlement;
import com.example.BeGroom.settlement.domain.id.WeeklySettlementId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklySettlementRepository extends JpaRepository<WeeklySettlement, WeeklySettlementId> {
}
