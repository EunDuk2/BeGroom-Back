package com.example.BeGroom.notification.repository;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.domain.NotificationType;
import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MemberNotificationRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private MemberNotificationRepository memberNotificationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @AfterEach
    void tearDown() {
        memberNotificationRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("유저 ID에 해당하는 알림을 생성일 기준 내림차순으로 조회한다.")
    @Test
    void findAllByMemberIdOrderByCreatedAtDesc(){
        // given
        Member member = creteMember();
        memberRepository.save(member);
        Notification notification = createNotification();
        notificationRepository.save(notification);
        MemberNotification memberNotification1 = createMemberNotification(member, notification, "notification 1", false);
        MemberNotification memberNotification2 = createMemberNotification(member, notification, "notification 2", false);
        MemberNotification memberNotification3 = createMemberNotification(member, notification, "notification 3", false);
        memberNotificationRepository.saveAll(List.of(memberNotification1, memberNotification2, memberNotification3));

        // when
        List<MemberNotification> result = memberNotificationRepository.findAllByMemberIdOrderByCreatedAtDesc(member.getId());

        // then
        assertThat(result).hasSize(3).extracting(MemberNotification::getCreatedAt).isSortedAccordingTo(Comparator.reverseOrder());
        assertThat(result).extracting(MemberNotification::getMetaData).containsExactly("notification 3", "notification 2", "notification 1");
    }

    @DisplayName("유저 ID에 해당하는 알림에서 읽지 않은 알림의 개수를 조회한다.")
    @Test
    void countByMemberIdAndIsReadFalse(){
        // given
        Member member = creteMember();
        memberRepository.save(member);
        Notification notification = createNotification();
        notificationRepository.save(notification);
        MemberNotification memberNotification1 = createMemberNotification(member, notification, "notification 1", false);
        MemberNotification memberNotification2 = createMemberNotification(member, notification, "notification 2", false);
        MemberNotification memberNotification3 = createMemberNotification(member, notification, "notification 3", true);
        memberNotificationRepository.saveAll(List.of(memberNotification1, memberNotification2, memberNotification3));

        // when
        Long result = memberNotificationRepository.countByMemberIdAndIsReadFalse(member.getId());

        // then
        assertThat(result).isEqualTo(2);
    }

    @DisplayName("유저 ID에 해당하는 알림을 전부 읽음 처리한다.")
    @Test
    @Transactional
    void bulkMarkAsRead(){
        // given
        Member member = creteMember();
        memberRepository.save(member);
        Notification notification = createNotification();
        notificationRepository.save(notification);
        MemberNotification memberNotification1 = createMemberNotification(member, notification, "notification 1", false);
        MemberNotification memberNotification2 = createMemberNotification(member, notification, "notification 2", false);
        MemberNotification memberNotification3 = createMemberNotification(member, notification, "notification 3", true);
        memberNotificationRepository.saveAll(List.of(memberNotification1, memberNotification2, memberNotification3));

        // when
        memberNotificationRepository.bulkMarkAsRead(member.getId());

        // then
        List<MemberNotification> result = memberNotificationRepository.findAll();
        assertThat(result).hasSize(3).extracting("isRead").containsExactly(true, true, true);
    }

    private Member creteMember(){
        return Member.builder()
                .email("user")
                .name("user")
                .password("1234")
                .phoneNumber("01012341234")
                .build();
    }

    private Notification createNotification(){
        return Notification.builder()
                .type(NotificationType.ORDER)
                .title("테스트 제목")
                .message("메세지")
                .link("url")
                .build();
    }

    private MemberNotification createMemberNotification(Member member, Notification notification, String metaData, boolean isRead){
        return MemberNotification.builder()
                .member(member)
                .notification(notification)
                .metaData(metaData)
                .isRead(isRead)
                .build();
    }

}