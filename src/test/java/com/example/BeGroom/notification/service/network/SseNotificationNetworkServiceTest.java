package com.example.BeGroom.notification.service.network;

import com.example.BeGroom.IntegrationTestSupport;
import com.example.BeGroom.notification.repository.EmitterRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SseNotificationNetworkServiceTest extends IntegrationTestSupport {
    @MockitoSpyBean
    private EmitterRepository emitterRepository;

    @Autowired
    private SseNotificationNetworkService sseNotificationNetworkService;

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

    @DisplayName("등록된 SseEmitter 전체에게 이벤트를 발송한다.")
    @Test
    void sendToAll() throws IOException {
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

        Map<String, Object> message = new HashMap<>();
        message.put("message", "test message");

        // when
        sseNotificationNetworkService.sendToAll(message);

        // then
        verify(emitterRepository).findAll();
        verify(emitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter2, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @DisplayName("등록된 SseEmitter의 특정 Client에게 이벤트를 발송한다.")
    @Test
    void sendToMembers() throws IOException {
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

        Map<String, Object> message = new HashMap<>();
        message.put("message", "test message");

        // when
        sseNotificationNetworkService.sendToMembers(message, List.of(member1Id));

        // then
        verify(emitterRepository).findAllStartWithById(member1Id);
        verify(emitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter2, never()).send(any(SseEmitter.SseEventBuilder.class));
    }
}