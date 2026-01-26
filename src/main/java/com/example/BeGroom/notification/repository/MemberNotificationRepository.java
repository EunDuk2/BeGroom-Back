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

    // TCP Retry 시 누락된 개수와 그 중 가장 큰 ID를 한 번에 조회
    @Query("SELECT COUNT(n), MAX(n.id) FROM MemberNotification n WHERE n.member.id = :memberId AND n.id > :lastId")
    Object[] findLostSummary(@Param("memberId") Long memberId, @Param("lastId") Long lastId);

    // TCP Init connect 시 안 읽은 개수와 전체 중 가장 큰 ID를 한 번에 조회
    @Query("SELECT COUNT(n), MAX(n.id) FROM MemberNotification n WHERE n.member.id = :memberId AND n.isRead = false")
    Object[] findUnreadSummary(@Param("memberId") Long memberId);
}
