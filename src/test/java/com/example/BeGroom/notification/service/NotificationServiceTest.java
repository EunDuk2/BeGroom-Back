package com.example.BeGroom.notification.service;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.domain.NotificationType;
import com.example.BeGroom.notification.dto.CreateNotificationReqDto;
import com.example.BeGroom.notification.dto.GetMemberNotificationResDto;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.notification.repository.NotificationRepository;
import com.example.BeGroom.notification.service.network.NotificationNetworkService;
import com.example.BeGroom.notification.service.network.SseNotificationNetworkService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class NotificationServiceTest extends IntegrationTestSupport {

    @MockitoSpyBean
    private NotificationNetworkService notificationNetworkService;

    @MockitoSpyBean
    private MemberNotificationRepository memberNotificationRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @AfterEach
    void tearDown() {
        memberNotificationRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("관리자가 알림을 생성한다.")
    @Test
    void createNotificationService(){
        // given
        CreateNotificationReqDto createNotificationReqDto = CreateNotificationReqDto.builder()
                .type(NotificationType.ORDER).title("테스트 제목").message("테스트 메시지").link("test url").build();

        // when
        Notification result = notificationService.createNotification(createNotificationReqDto);

        // then
        assertThat(result.getTitle()).isEqualTo("테스트 제목");
        assertThat(result.getMessage()).isEqualTo("테스트 메시지");
        assertThat(result.getType()).isEqualTo(NotificationType.ORDER);
    }

    @DisplayName("로그인한 사용자가 자신의 알림 리스트를 가져온다.")
    @Test
    void getMyNotifications(){
        // given
        Member member = creteMember();
        memberRepository.save(member);
        Notification notification = createNotification();
        notificationRepository.save(notification);
        MemberNotification memberNotification = createMemberNotification(member, notification);
        memberNotificationRepository.save(memberNotification);

        // when
        List<GetMemberNotificationResDto.NotificationInfo> response = notificationService.getMyNotifications(member.getId()).getNotifications();

        // then
        assertThat(response).hasSize(1).extracting("title", "isRead").containsExactlyInAnyOrder(
                tuple("테스트 제목", false)
        );
    }

    @DisplayName("멤버가 알림을 읽음 처리할 수 있다.")
    @Test
    void readNotification(){
        // given
        Member member = creteMember();
        memberRepository.save(member);
        Notification notification = createNotification();
        notificationRepository.save(notification);
        MemberNotification memberNotification = createMemberNotification(member, notification);
        memberNotificationRepository.save(memberNotification);
        Long memberNotificationId = 1L;

        // when
        notificationService.readNotification(memberNotificationId);

        // then

        assertThat(memberNotificationRepository.findAll()).hasSize(1).extracting("id").containsExactly(1L);
    }

    @DisplayName("멤버가 알림을 읽음 처리할 때, 해당 알림이 존재해야한다.")
    @Test
    void readNotificationWithoutMemberNotification(){
        // given
        Member member = creteMember();
        memberRepository.save(member);
        Notification notification = createNotification();
        notificationRepository.save(notification);

        Long memberNotificationId = 1L;

        // when, then
        assertThatThrownBy(()->notificationService.readNotification(memberNotificationId)).isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 알림이 존재하지 않습니다.");
    }

    @DisplayName("멤버가 알림을 읽음 처리할 때, 해당 알림이 존재해야한다.")
    @Test
    void readAllNotifications(){
        // given
        Member member = creteMember();
        memberRepository.save(member);
        Notification notification = createNotification();
        notificationRepository.save(notification);
        MemberNotification memberNotification1 = createMemberNotification(member, notification);
        MemberNotification memberNotification2 = createMemberNotification(member, notification);
        MemberNotification memberNotification3 = createMemberNotification(member, notification);
        memberNotificationRepository.saveAll(List.of(memberNotification1, memberNotification2, memberNotification3));

        // when
        notificationService.readAllNotifications(member.getId());

        // then
        assertThat(memberNotificationRepository.findAll()).hasSize(3).extracting("id", "isRead")
                .containsExactlyInAnyOrder(
                        tuple(memberNotification1.getId(), true),
                        tuple(memberNotification2.getId(), true),
                        tuple(memberNotification3.getId(), true)
                );
    }

    @DisplayName("특정 사용자에게 원하는 메시지를 담아서 DB에 기록하고 실시간 메시지를 전송한다.")
    @Test
    @Transactional
    void send() {
        // given
        Member member1 = creteMember();
        Member member2 = creteMember();
        memberRepository.saveAll(List.of(member1, member2));
        Notification notification = createNotification();
        notificationRepository.save(notification);
        List<Long> receiverIds = List.of(member1.getId(), member2.getId());
        Long templateId = notification.getId();
        Map<String, String> variables = new HashMap<>();

        // when
        notificationService.send(receiverIds, templateId, variables);

        // then
        assertThat(memberNotificationRepository.findAll()).hasSize(2)
                .extracting("member").containsExactly(member1, member2);
    }

    @DisplayName("특정 멤버에게 알림 전송 시 존재하지 않는 템플릿 ID로 요청하면 예외가 발생하고 저장 로직은 실행되지 않는다.")
    @Test
    void sendWithoutTemplate() {
        // given
        Member member1 = creteMember();
        memberRepository.save(member1);

        List<Long> receiverIds = List.of(1L);
        Long invalidTemplateId = 999L;
        Map<String, String> variables = new HashMap<>();

        // when , then
        assertThatThrownBy(() -> notificationService.send(receiverIds, invalidTemplateId, variables))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 타입의 알림 템플릿이 없습니다.");

        verify(memberNotificationRepository, never()).saveAll(any());
        verify(notificationNetworkService, never()).send(any(), any());
    }

    @DisplayName("모든 사용자에게 원하는 메시지를 담아서 DB에 기록하고 실시간 메시지를 전송한다.")
    @Test
    void sendToAllMembers(){
        // given
        Member member1 = creteMember();
        Member member2 = creteMember();
        memberRepository.saveAll(List.of(member1, member2));
        Notification notification = createNotification();
        notificationRepository.save(notification);

        Long templateId = notification.getId();
        Map<String, String> variables = new HashMap<>();

        // when
        notificationService.sendToAllMembers(templateId, variables);

        // then
        assertThat(memberNotificationRepository.findAll()).hasSize(2);
    }

    @DisplayName("모든 사용자에게 알림 전송 시 존재하지 않는 템플릿 ID로 요청하면 예외가 발생하고 저장 로직은 실행되지 않는다.")
    @Test
    void sendToAllMembersWithoutTemplate(){
        // given
        Member member1 = creteMember();
        Member member2 = creteMember();
        memberRepository.saveAll(List.of(member1, member2));

        Long invalidTemplateId = 999L;
        Map<String, String> variables = new HashMap<>();

        // when , then
        assertThatThrownBy(() -> notificationService.sendToAllMembers(invalidTemplateId, variables))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 타입의 알림 템플릿이 없습니다.");

        verify(memberNotificationRepository, never()).saveAll(any());
        verify(notificationNetworkService, never()).send(any(), any());
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

    private MemberNotification createMemberNotification(Member member, Notification notification){
        return MemberNotification.builder()
                .member(member)
                .notification(notification)
                .metaData("테스트입니다.")
                .isRead(false)
                .build();
    }
}