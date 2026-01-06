package com.example.BeGroom.notification.repository;

import com.example.BeGroom.notification.domain.MemberNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberNotificationRepository extends JpaRepository<MemberNotification, Long> {
    List<MemberNotification> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
    long countByMemberIdAndIsReadFalse(Long memberId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE MemberNotification mn SET mn.isRead = true, mn.readAt = CURRENT_TIMESTAMP WHERE mn.member.id = :memberId AND mn.isRead = false")
    void bulkMarkAsRead(@Param("memberId") Long memberId);

}
