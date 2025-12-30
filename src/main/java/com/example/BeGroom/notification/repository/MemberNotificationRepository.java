package com.example.BeGroom.notification.repository;

import com.example.BeGroom.notification.domain.MemberNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberNotificationRepository extends JpaRepository<MemberNotification, Long> {
    List<MemberNotification> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
    long countByMemberIdAndIsReadFalse(Long memberId);
}
