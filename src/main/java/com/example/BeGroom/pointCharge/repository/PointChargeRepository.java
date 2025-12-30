package com.example.BeGroom.pointCharge.repository;

import com.example.BeGroom.pointCharge.domain.PointCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointChargeRepository extends JpaRepository<PointCharge, Long> {
}
