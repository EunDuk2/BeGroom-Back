package com.example.BeGroom.notification.service;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.domain.NotificationType;
import com.example.BeGroom.notification.dto.GetMemberNotificationResDto;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class NotificationServiceTest extends IntegrationTestSupport {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberNotificationRepository memberNotificationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @DisplayName("로그인한 사용자가 자신의 알림 리스트를 가져온다.")
    @Test
    void getMyNotifications(){
        // given
        Member member = Member.builder()
                .email("user").name("user").password("1234").phoneNumber("01012341234").build();
        memberRepository.save(member);

        Notification notification = Notification.builder()
                .type(NotificationType.ORDER)
                .title("테스트 제목")
                .message("메세지")
                .link("url")
                .build();
        notificationRepository.save(notification);

        memberNotificationRepository.save(MemberNotification.builder()
                .member(member)
                .notification(notification)
                .metaData("테스트입니다.")
                .isRead(false)
                .build()
        );

        // when
        List<GetMemberNotificationResDto.NotificationInfo> response = notificationService.getMyNotifications(member.getId()).getNotifications();

        // then
        assertThat(response).hasSize(1).extracting("title", "isRead").containsExactlyInAnyOrder(
                tuple("테스트 제목", false)
        );
    }

    @DisplayName("로그인한 사용자가")
    @Test
    void test(){
        // given


        // when


        // then


    }

}