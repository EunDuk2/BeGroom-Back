package com.example.BeGroom.notification.service.network;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.notification.domain.MemberNotification;
import com.example.BeGroom.notification.domain.Notification;
import com.example.BeGroom.notification.domain.NotificationType;
import com.example.BeGroom.notification.dto.NetworkMessageDto;
import com.example.BeGroom.notification.dto.NotificationSendResult;
import com.example.BeGroom.notification.repository.EmitterRepository;
import com.example.BeGroom.notification.repository.MemberNotificationRepository;
import com.example.BeGroom.notification.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SseNotificationNetworkServiceTest extends IntegrationTestSupport {
    @MockitoSpyBean
    private EmitterRepository emitterRepository;

    @MockitoSpyBean
    private SseNotificationNetworkService sseNotificationNetworkService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MemberNotificationRepository memberNotificationRepository;

    @Value("${sse.timeout}")
    private Long defaultTimeout;

    @AfterEach
    void tearDown() {
        emitterRepository.deleteAll();
    }

    @DisplayName("멤버의 Client를 SSE Emitter에 등록한다.")
    @Test
    void connectBySse(){
        // given
        Long memberId = 1L;
        LocalDateTime connectTime = LocalDateTime.of(2026, 1, 21, 0, 0, 0);

        // when
        SseEmitter expectedEmitter = sseNotificationNetworkService.connect(memberId, connectTime);

        // then
        Map<String, SseEmitter> result = emitterRepository.findAll();
        String expectedEmitterId = "1_1768921200000";

        assertThat(result.size()).isEqualTo(1);
        assertThat(result)
                .containsEntry(expectedEmitterId, expectedEmitter);
    }

    @DisplayName("멤버의 Client를 SSE Emitter에 등록할 때, memberId는 양수이다.")
    @Test
    void connectBySseWithoutMemberId(){
        // given
        Long memberId = 0L;
        LocalDateTime connectTime = LocalDateTime.of(2026, 1, 21, 0, 0, 0);

        // when, then
        assertThatThrownBy(()->{ sseNotificationNetworkService.connect(memberId, connectTime); }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("멤버 ID는 양수여야 합니다.");
    }

    @DisplayName("멤버의 등록된 emitter를 연결 해제한다.")
    @Test
    void disconnectBySse(){
        // given
        Long member1Id = 1L;
        Long member2Id = 2L;
        String emitterId1 = member1Id + "_" + "1000000000000";
        String emitterId2 = member1Id + "_" + "1000000000001";
        String emitterId3 = member2Id + "_" + "1000000000000";
        SseEmitter emitter1 = new SseEmitter(defaultTimeout);
        SseEmitter emitter2 = new SseEmitter(defaultTimeout);
        SseEmitter emitter3 = new SseEmitter(defaultTimeout);

        Map<String, SseEmitter> emitters = new HashMap<>();
        emitters.put(emitterId1, emitter1);
        emitters.put(emitterId2, emitter2);
        emitters.put(emitterId3, emitter3);
        emitterRepository.saveAll(emitters);

        // when
        sseNotificationNetworkService.disconnect(member1Id);

        // then
        Map<String, SseEmitter> result = emitterRepository.findAll();
        String expectedEmitterId = "2_1000000000000";

        assertThat(result.size()).isEqualTo(1);
        assertThat(result)
                .containsEntry(expectedEmitterId, emitter3);
    }

    @DisplayName("멤버의 등록된 emitter를 연결 해제할 때, 멤버 Id는 양수이다.")
    @Test
    void disconnectBySseWithoutMemberId(){
        // given
        Long member1Id = 1L;
        Long member2Id = 2L;
        String emitterId1 = member1Id + "_" + "1000000000000";
        String emitterId2 = member1Id + "_" + "1000000000001";
        String emitterId3 = member2Id + "_" + "1000000000000";
        SseEmitter emitter1 = new SseEmitter(defaultTimeout);
        SseEmitter emitter2 = new SseEmitter(defaultTimeout);
        SseEmitter emitter3 = new SseEmitter(defaultTimeout);

        Map<String, SseEmitter> emitters = new HashMap<>();
        emitters.put(emitterId1, emitter1);
        emitters.put(emitterId2, emitter2);
        emitters.put(emitterId3, emitter3);
        emitterRepository.saveAll(emitters);

        // when, then
        assertThatThrownBy(()->{ sseNotificationNetworkService.disconnect(0L); }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("멤버 ID는 양수여야 합니다.");
    }

    @DisplayName("등록된 SseEmitter에게 이벤트를 발송한다.")
    @Test
    void send() throws IOException {
        // given
        Long member1Id = 1L;
        Long member2Id = 2L;
        String emitterId1 = member1Id + "_" + "1000000000000";
        String emitterId2 = member2Id + "_" + "1000000000000";
        SseEmitter emitter1 = mock(SseEmitter.class);
        SseEmitter emitter2 = mock(SseEmitter.class);

        Map<String, SseEmitter> emitters = new HashMap<>();
        emitters.put(emitterId1, emitter1);
        emitters.put(emitterId2, emitter2);
        emitterRepository.saveAll(emitters);

        NetworkMessageDto msg1 = createNetworkMessageDto(member1Id, "100");
        NetworkMessageDto msg2 = createNetworkMessageDto(member2Id, "101");
        List<NetworkMessageDto> message = List.of(msg1, msg2);

        // when
        sseNotificationNetworkService.send(message);

        // then
        verify(emitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter2, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @DisplayName("SSE 구독에 성공했을 때, Last-Event-Id가 있고 읽지 않은 알림이 있으면 개수와 함께 메시지를 보낸다.")
    @Test
    void sseConnectionMessageWithLastEventId(){
        // given
        Notification notification = createNotification();
        Member member = createMember();
        notificationRepository.save(notification);
        memberRepository.save(member);

        MemberNotification notification1 = createMemberNotification(member, notification);
        MemberNotification notification2 = createMemberNotification(member, notification);
        memberNotificationRepository.saveAll(List.of(notification1, notification2));

        Long member1Id = 1L;
        String lastEventId = "1";
        String emitterId1 = member1Id + "_" + "1000000000000";
        SseEmitter emitter1 = new SseEmitter(defaultTimeout);

        // when
        NotificationSendResult result = sseNotificationNetworkService.sseConnectionMessage(member1Id, lastEventId, emitterId1, emitter1);

        // then
        assertThat(result).extracting("emitterId", "eventId", "isSuccess")
                .containsExactly("1_1000000000000", "2", true);
        verify(sseNotificationNetworkService, times(1))
                .sendBySse(eq(emitter1), eq("1_1000000000000"), anyString(), any(), any());
    }

    @DisplayName("SSE 구독에 성공했을 때, Last-Event-Id가 있지만 유실된 알림의 개수가 0개라면 메시지를 보내지 않는다.")
    @Test
    void sseConnectionMessageWithLastEventIdAndZeroNotificationCount(){
        // given
        Notification notification = createNotification();
        Member member = createMember();
        notificationRepository.save(notification);
        memberRepository.save(member);

        MemberNotification notification1 = createMemberNotification(member, notification);
        MemberNotification notification2 = createMemberNotification(member, notification);
        memberNotificationRepository.saveAll(List.of(notification1, notification2));

        Long member1Id = 1L;
        String lastEventId = "2";
        String emitterId1 = member1Id + "_" + "1000000000000";
        SseEmitter emitter1 = new SseEmitter(defaultTimeout);

        // when
        NotificationSendResult result = sseNotificationNetworkService.sseConnectionMessage(member1Id, lastEventId, emitterId1, emitter1);

        // then
        assertThat(result).extracting("emitterId", "eventId", "isSuccess")
                .containsExactly("1_1000000000000", "", true);
        verify(sseNotificationNetworkService, never())
                .sendBySse(any(), anyString(), anyString(), any(), any());
    }

    @DisplayName("SSE 구독에 성공했을 때, Last-Event-Id가 비어있고 읽지 않은 알림이 있으면 개수와 함께 메시지를 보낸다.")
    @Test
    void sseConnectionMessageWithoutLastEventId(){
        // given
        Notification notification = createNotification();
        Member member = createMember();
        notificationRepository.save(notification);
        memberRepository.save(member);

        MemberNotification notification1 = createMemberNotification(member, notification);
        MemberNotification notification2 = createMemberNotification(member, notification);
        memberNotificationRepository.saveAll(List.of(notification1, notification2));

        Long member1Id = 1L;
        String lastEventId = "";
        String emitterId1 = member1Id + "_" + "1000000000000";
        SseEmitter emitter1 = new SseEmitter(defaultTimeout);

        // when
        NotificationSendResult result = sseNotificationNetworkService.sseConnectionMessage(member1Id, lastEventId, emitterId1, emitter1);

        // then
        assertThat(result).extracting("emitterId", "eventId", "isSuccess")
                .containsExactly("1_1000000000000", "2", true);
        verify(sseNotificationNetworkService, times(1))
                .sendBySse(eq(emitter1), eq("1_1000000000000"), anyString(), any(), any());
    }

    @DisplayName("SSE 구독에 성공했을 때, Last-Event-Id가 비어있고 읽지 않은 알림이 없으면 하트비트 메시지를 보낸다.")
    @Test
    void sseConnectionMessageWithoutLastEventIdAndZeroNotificationCount(){
        // given
        Notification notification = createNotification();
        Member member = createMember();
        notificationRepository.save(notification);
        memberRepository.save(member);

        MemberNotification notification1 = createMemberNotification(member, notification);
        MemberNotification notification2 = createMemberNotification(member, notification);
        memberNotificationRepository.saveAll(List.of(notification1, notification2));

        Long member1Id = 1L;
        String lastEventId = "";
        String emitterId1 = member1Id + "_" + "1000000000000";
        SseEmitter emitter1 = new SseEmitter(defaultTimeout);

        // when
        NotificationSendResult result = sseNotificationNetworkService.sseConnectionMessage(member1Id, lastEventId, emitterId1, emitter1);

        // then
        assertThat(result).extracting("emitterId", "eventId", "isSuccess")
                .containsExactly("1_1000000000000", "2", true);
        verify(sseNotificationNetworkService, times(1))
                .sendBySse(eq(emitter1), eq("1_1000000000000"), anyString(), any(), any());
    }

    @DisplayName("Sse emitter에게 메시지를 보낸다.")
    @Test
    void sendBySse(){
        // given
        SseEmitter emitter = new SseEmitter(defaultTimeout);
        String emitterId = "1" + "_" + "1000000000000";
        String eventId = "1";
        String eventName = "notification";
        String data = "{}";

        // when
        NotificationSendResult result = sseNotificationNetworkService.sendBySse(emitter, emitterId, eventId, eventName, data);

        // then
        assertThat(result).extracting("emitterId", "eventId", "isSuccess").containsExactly(emitterId, eventId, true);
    }

    @DisplayName("Sse emitter에게 메시지를 보낼 때, 연결이 끊긴 emitter로 메시지 송신을 시도하면 메모리에서도 연결 해제한다.")
    @Test
    void sendBySseWithNullEmitter(){
        // given
        SseEmitter emitter = new SseEmitter(defaultTimeout);
        String emitterId = "1" + "_" + "1000000000000";
        emitterRepository.save(emitterId, emitter);

        emitter.complete();
        String eventId = "1";
        String eventName = "notification";
        String data = "{}";

        // when
        NotificationSendResult result = sseNotificationNetworkService.sendBySse(emitter, emitterId, eventId, eventName, data);

        // then
        assertThat(result.isSuccess()).isFalse();
        verify(emitterRepository).deleteById(emitterId);
    }

    @DisplayName("Sse emitter에게 하트비트 메시지를 보낸다.")
    @Test
    void sendHeartBeat(){
        // given
        SseEmitter emitter = new SseEmitter(defaultTimeout);
        String emitterId = "1" + "_" + "1000000000000";

        // when
        NotificationSendResult result = sseNotificationNetworkService.sendHeartBeat(emitter, emitterId);

        // then
        assertThat(result).extracting("emitterId", "eventId", "isSuccess").containsExactly(emitterId, "", true);
    }

    @DisplayName("Sse emitter에게 메시지를 보낼 때, 연결이 끊긴 emitter로 메시지 송신을 시도하면 메모리에서도 연결 해제한다.")
    @Test
    void sendHeartBeatWithNullEmitter(){
        // given
        SseEmitter emitter = new SseEmitter(defaultTimeout);
        String emitterId = "1" + "_" + "1000000000000";
        emitterRepository.save(emitterId, emitter);

        emitter.complete();

        // when
        NotificationSendResult result = sseNotificationNetworkService.sendHeartBeat(emitter, emitterId);

        // then
        assertThat(result.isSuccess()).isFalse();
        verify(emitterRepository).deleteById(emitterId);
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

    private MemberNotification createMemberNotification(Member member, Notification notification){
        return MemberNotification.builder()
                .member(member)
                .notification(notification)
                .metaData("테스트입니다.")
                .isRead(false)
                .build();
    }

    private NetworkMessageDto createNetworkMessageDto(Long receiverId, String eventId){
        return NetworkMessageDto.builder()
                .receiverId(receiverId)
                .eventId(eventId)
                .eventName("notification")
                .message("test")
                .data("test")
                .build();
    }
}