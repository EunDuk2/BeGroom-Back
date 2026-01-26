package com.example.BeGroom.notification.service;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.domain.NotificationType;
import com.example.BeGroom.notification.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;

class NotificationHistoryServiceTest extends IntegrationTestSupport {
    @Autowired
    private NotificationHistoryService notificationHistoryService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("알림 템플릿을 찾아서 특정 사용자에 대한 알림을 생성할 수 있다.")
    @Test
    void createMemberNotification() {
        // given
        Member member = createMember();
        memberRepository.save(member);
        Notification notification = createNotification();
        notificationRepository.save(notification);

        List<Long> receiverIds = List.of(member.getId());
        Map<String, String> variables = new HashMap<>();
        variables.put("orderId", "1");

        // when
        List<MemberNotification> result = notificationHistoryService.createMemberNotification(receiverIds, notification.getId(), variables);

        // then
        assertThat(result).hasSize(1)
                .extracting("member.id", "notification.id", "metaData")
                .containsExactlyInAnyOrder(
                        tuple(
                                member.getId(),
                                notification.getId(),
                                "{\"orderId\":\"1\"}"
                        )
                );
    }

    @DisplayName("알림 템플릿을 찾아서 특정 사용자에 대한 알림을 생성할 때, 탬플릿은 필수이다.")
    @Test
    void createMemberNotificationWithoutTemplate() {
        // given
        Member member = createMember();
        memberRepository.save(member);
        Notification notification = createNotification();
        notificationRepository.save(notification);

        List<Long> receiverIds = List.of(member.getId());
        Map<String, String> variables = new HashMap<>();
        variables.put("orderId", "1");

        notificationRepository.deleteById(notification.getId());

        // when, then
        assertThatThrownBy(() -> notificationHistoryService.createMemberNotification(receiverIds, notification.getId(), variables))
                .isInstanceOf(EntityNotFoundException.class).hasMessage("해당 타입의 알림 템플릿이 없습니다.");
    }

    @DisplayName("알림 템플릿을 찾아서 특정 사용자에 대한 알림을 생성할 때, 멤버는 필수이다.")
    @Test
    void createMemberNotificationWithoutMember() {
        // given
        Member member = createMember();
        Member invalidMember = memberRepository.save(member);
        Notification notification = createNotification();
        Notification validNotification = notificationRepository.save(notification);

        List<Long> receiverIds = List.of(invalidMember.getId());
        Map<String, String> variables = new HashMap<>();
        variables.put("orderId", "1");

        memberRepository.deleteById(invalidMember.getId());

        // when, then
        assertThatThrownBy(() -> notificationHistoryService.createMemberNotification(receiverIds, validNotification.getId(), variables))
                .isInstanceOf(EntityNotFoundException.class).hasMessage("해당하는 멤버가 없습니다.");
    }

    private Member createMember(){
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
}